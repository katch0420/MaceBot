package net.katch0420.macebot.client.gui.bodies;

import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.macebot.control.Difficulty;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.client.ClientSideSettingsSyncHelper;
import net.katch0420.macebot.main.settings.main.SettingsCategory;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


/**
 * Settings body: MaceBot / Player / Mod render as side-by-side, independently
 * scrollable columns. When all 3 can't fit at a readable width, the
 * least-recently-used column(s) collapse to a thin labeled strip; clicking a
 * strip brings it back and bumps whichever column hasn't been touched
 * recently into collapse instead.
 */
public class SettingsBody extends Body {

    private static final SettingsCategory[] CATEGORIES = {
            SettingsCategory.MACEBOT, SettingsCategory.PLAYER, SettingsCategory.MOD
    };

    private final ThemeEditorBody themeEditorBody = new ThemeEditorBody();

    private int margin;
    private int textHeight;

    private int panelX, panelY, panelWidth, panelHeight;
    private int statusBarHeight;
    private int columnsY, columnsHeight;
    private int headerHeight;
    private int rowHeight;
    private int controlWidth;
    private int controlHeight;
    private int collapsedWidth;
    private int minExpandedWidth;

    private Button themeButton;
    private final LinkedList<SettingsCategory> recency = new LinkedList<>(List.of(CATEGORIES));
    private final List<ColumnState> columns = new ArrayList<>();

    @Override
    public Text getLabel() {
        return Text.of("Settings");
    }

    @Override
    public void init() {
        super.init();
        margin = s(6, 4);
        textHeight = getTextRenderer().fontHeight;

        panelX = x;
        panelY = y;
        panelWidth = availableWidth;
        panelHeight = availableHeight;

        statusBarHeight = s(14, 11) + 2 * margin;
        headerHeight = s(16, 13);
        rowHeight = s(18, 15);
        controlHeight = s(13, 11);
        controlWidth = s(56, 44);
        collapsedWidth = s(34, 26);
        minExpandedWidth = s(150, 115);

        columnsY = panelY + statusBarHeight + margin;
        columnsHeight = Math.max(rowHeight, panelHeight - statusBarHeight - 2 * margin);

        int themeBtnW = s(54, 42);
        int themeBtnH = s(14, 11);
        themeButton = Button.builder()
                .position(panelX + panelWidth - margin - themeBtnW, panelY + margin)
                .size(themeBtnW, themeBtnH)
                .baseLabel(Text.of("Theme"))
                .backgroundColor(theme.accent())
                .foregroundColor(0xFFFFFFFF)
                .borderColor(-1)
                .hoverColor(theme.accent() + 0xFF101010)
                .holdColor(theme.accent() + 0xFF202020)
                .onClick(b -> parentScreen.navigateTo(themeEditorBody))
                .build();
        addDrawableChild(themeButton);

        rebuildColumns();
    }

    // ------------------------------------------------------------------
    // Column layout
    // ------------------------------------------------------------------

    private void rebuildColumns() {
        for (ColumnState col : columns) {
            for (Button b : col.controls) remove(b);
            if (col.headerButton != null) remove(col.headerButton);
        }
        columns.clear();

        int available = panelWidth - 2 * margin - (CATEGORIES.length - 1) * margin;
        int maxExpanded = CATEGORIES.length;
        while (maxExpanded > 1) {
            int collapsedCount = CATEGORIES.length - maxExpanded;
            int required = maxExpanded * minExpandedWidth + collapsedCount * collapsedWidth;
            if (required <= available) break;
            maxExpanded--;
        }

        var expandedSet = new HashSet<>(recency.subList(0, Math.min(maxExpanded, recency.size())));
        int collapsedCount = (int) Arrays.stream(CATEGORIES).filter(c -> !expandedSet.contains(c)).count();
        int expandedTotalWidth = available - collapsedCount * collapsedWidth;
        int expandedCount = CATEGORIES.length - collapsedCount;
        int eachExpandedWidth = expandedCount > 0 ? expandedTotalWidth / expandedCount : available;

        int cx = panelX + margin;
        for (SettingsCategory category : CATEGORIES) {
            boolean expanded = expandedSet.contains(category);
            int colWidth = expanded ? eachExpandedWidth : collapsedWidth;
            ColumnState col = new ColumnState(category, expanded, cx, columnsY, colWidth, columnsHeight);
            buildColumn(col);
            columns.add(col);
            cx += colWidth + margin;
        }
    }

    private void buildColumn(ColumnState col) {
        col.headerButton = Button.builder()
                .position(col.x, col.y)
                .size(col.width, headerHeight)
                .baseLabel(Text.of(col.expanded ? col.category.displayName() : abbreviate(col.category)))
                .backgroundColor(col.expanded ? theme.accent() : theme.body_button_background())
                .foregroundColor(col.expanded ? 0xFFFFFFFF : theme.body_label())
                .borderColor(-1)
                .hoverColor((col.expanded ? theme.accent() : theme.body_button_background()) + 0xFF101010)
                .holdColor((col.expanded ? theme.accent() : theme.body_button_background()) + 0xFF202020)
                .onClick(b -> focusColumn(col.category))
                .build();
        addDrawableChild(col.headerButton);

        if (!col.expanded) return;

        col.contentY = col.y + headerHeight + margin / 2;
        col.contentHeight = Math.max(rowHeight, col.height - headerHeight - margin / 2);

        List<SettingsKey> keys = SettingsKey.byCategory(col.category);
        int i = 0;
        for (SettingsKey key : keys) {
            Button control = buildControlFor(col, key);
            if (control != null) {
                control.setSize(Math.min(controlWidth, col.width - 2 * margin), controlHeight);
                addDrawableChild(control);
                col.controls.add(control);
            }
            col.rows.add(new RowEntry(key, control, i));
            i++;
        }

        int totalContentHeight = col.rows.size() * rowHeight;
        col.maxScroll = Math.max(0, totalContentHeight - col.contentHeight);
        col.scrollOffset = Math.max(0, Math.min(col.scrollOffset, col.maxScroll));
        relayoutColumn(col);
    }

    private String abbreviate(SettingsCategory category) {
        String name = category.displayName();
        return name.length() <= 3 ? name.toUpperCase() : name.substring(0, 3).toUpperCase();
    }

    private void focusColumn(SettingsCategory category) {
        recency.remove(category);
        recency.addFirst(category);
        rebuildColumns();
    }

    private void relayoutColumn(ColumnState col) {
        for (RowEntry row : col.rows) {
            if (row.control == null) continue;
            int rowTop = col.contentY + row.index * rowHeight - col.scrollOffset;
            int controlY = rowTop + (rowHeight - controlHeight) / 2;
            int controlX = col.x + col.width - margin - row.control.getWidth();
            row.control.setPosition(controlX, controlY);
            boolean visible = rowTop + rowHeight > col.contentY && rowTop < col.contentY + col.contentHeight;
            row.control.setVisible(visible);
        }
    }

    private Button buildControlFor(ColumnState col, SettingsKey key) {
        if (!key.isEditable()) return null;
        if (key.isBoolean()) return buildToggle(col, key);
        if (key.isEnum()) return buildCycle(col, key);
        return Button.builder()
                .baseLabel(Text.of(String.valueOf(key.getClientValue())))
                .backgroundColor(theme.body_button_background())
                .borderColor(theme.body_button_border())
                .foregroundColor(theme.body_button_foreground())
                .disabled()
                .build();
    }

    private Button buildToggle(ColumnState col, SettingsKey key) {
        boolean current = (Boolean) key.getClientValue();
        return Button.builder()
                .baseLabel(Text.of(current ? "ON" : "OFF"))
                .backgroundColor(current ? theme.success() : theme.danger())
                .foregroundColor(0xFFFFFFFF)
                .borderColor(-1)
                .hoverColor((current ? theme.success() : theme.danger()) + 0xFF101010)
                .holdColor((current ? theme.success() : theme.danger()) + 0xFF202020)
                .onClick(b -> {
                    Object next = key.nextBooleanClientValue();
                    ClientSideSettingsSyncHelper.setAndSend(key, next);
                    rebuildSingleRow(col, key);
                })
                .build();
    }

    private Button buildCycle(ColumnState col, SettingsKey key) {
        Object current = key.getClientValue();
        Button.Builder bu = Button.builder()
                .baseLabel(getEnumText(current))
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> {
                    Object next = key.nextEnumClientValue();
                    ClientSideSettingsSyncHelper.setAndSend(key, next);
                    b.setLabelIfClickButton(getEnumText(next));
                });
        return key == SettingsKey.PRACTICE_MODE ? bu.activeSupplier(() -> ClientSideSettings.getMode() == Controller.Mode.PRACTICE).build() : bu.build();
    }

    private Text getEnumText(Object current){
        Text l;
        if(current instanceof Controller.Mode m){
            l = m.displayText;
        } else if(current instanceof Difficulty d){
            l = d.displayText;
        } else l = Text.of(LegacyText.getEnumValueAsString(String.valueOf(current)));
        return l;
    }

    private void rebuildSingleRow(ColumnState col, SettingsKey key) {
        for (int i = 0; i < col.rows.size(); i++) {
            RowEntry row = col.rows.get(i);
            if (row.key != key) continue;
            if (row.control != null) {
                remove(row.control);
                col.controls.remove(row.control);
            }
            Button fresh = buildControlFor(col, key);
            if (fresh != null) {
                fresh.setSize(Math.min(controlWidth, col.width - 2 * margin), controlHeight);
                addDrawableChild(fresh);
                col.controls.add(fresh);
            }
            col.rows.set(i, new RowEntry(key, fresh, row.index));
            break;
        }
        relayoutColumn(col);
    }

    // ------------------------------------------------------------------
    // Scroll
    // ------------------------------------------------------------------

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (ColumnState col : columns) {
            if (!col.expanded) continue;
            if (mouseX < col.x || mouseX > col.x + col.width || mouseY < col.contentY || mouseY > col.contentY + col.contentHeight) continue;
            if (col.maxScroll <= 0) return true;
            col.scrollOffset = Math.max(0, Math.min(col.maxScroll, col.scrollOffset - (int) (verticalAmount * rowHeight / 2)));
            relayoutColumn(col);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ------------------------------------------------------------------
    // Render - manual, so content rows can be properly clipped per column
    // (this is what was causing scrolled rows to pop over the header)
    // ------------------------------------------------------------------

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        c.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, theme.body_background());
        renderStatusBar(c);

        for (ColumnState col : columns) {
            renderColumnFrame(c, col);
        }

        themeButton.render(c, mx, my, d);
        for (ColumnState col : columns) {
            col.headerButton.render(c, mx, my, d);
        }

        for (ColumnState col : columns) {
            if (!col.expanded) continue;
            c.enableScissor(col.x, col.contentY, col.x + col.width, col.contentY + col.contentHeight);
            renderColumnLabels(c, col);
            for (RowEntry row : col.rows) {
                if (row.control != null) {
                    row.control.render(c, mx, my, d);
                    if(row.key.isExperimental()) c.drawText(getTextRenderer(),"⚠",row.control.getX() - margin / 2 - getTextRenderer().getWidth("⚠"), row.control.getY() + (row.control.getHeight() - textHeight) / 2, theme.warning(),false);
                }
            }
            c.disableScissor();
            renderColumnScrollbar(c, col);
        }
    }

    private void renderStatusBar(DrawContext c) {
        boolean online = ClientSideSettings.isMacebotOnline();
        String status = "MaceBot: " + (online ? "Online" : "Offline") + "   |   ID: " + ClientSideSettings.getMacebotId()
                + "   |   Opponent: " + ClientSideSettings.getOpponentId();
        c.drawText(getTextRenderer(), Text.of(status), panelX + margin, panelY + margin, online ? theme.success() : theme.danger(), true);
    }

    private void renderColumnFrame(DrawContext c, ColumnState col) {
        c.fill(col.x, col.y, col.x + col.width, col.y + col.height, theme.body_background());
        // Border around the whole column, not just a left line.
        c.fill(col.x, col.y, col.x + col.width, col.y + 1, theme.body_border());
        c.fill(col.x, col.y + col.height - 1, col.x + col.width, col.y + col.height, theme.body_border());
        c.fill(col.x, col.y, col.x + 1, col.y + col.height, theme.body_border());
        c.fill(col.x + col.width - 1, col.y, col.x + col.width, col.y + col.height, theme.body_border());

        if (!col.expanded) {
            // Collapsed: a slim accent strip + chevron, not stacked letters.
            int chevronY = col.y + col.height - headerHeight - 4;
            c.drawCenteredTextWithShadow(getTextRenderer(), Text.of("›"), col.x + col.width / 2, chevronY, theme.accent());
        }
    }

    private void renderColumnLabels(DrawContext c, ColumnState col) {
        for (RowEntry row : col.rows) {
            int rowTop = col.contentY + row.index * rowHeight - col.scrollOffset;
            if (rowTop + rowHeight < col.contentY || rowTop > col.contentY + col.contentHeight) continue;

            int textY = rowTop + (rowHeight - textHeight) / 2;
            int maxLabelWidth = col.width - 2 * margin - (row.control != null ? row.control.getWidth() + margin / 2 : 0);
            String label = trimToWidth(row.key.getDisplayName(), maxLabelWidth);
            c.drawText(getTextRenderer(), Text.of(label), col.x + margin, textY, theme.body_label(), true);

            if (!row.key.isEditable()) {
                String value = String.valueOf(row.key.getClientValue());
                int valueX = col.x + col.width - margin - getTextRenderer().getWidth(value);
                c.drawText(getTextRenderer(), Text.of(value), valueX, textY, theme.body_value(), true);
            }

            if (row.index > 0) {
                c.drawHorizontalLine(col.x + margin, col.x + col.width - margin, rowTop, theme.panel_separator() & 0x33FFFFFF);
            }
        }
    }

    private String trimToWidth(String text, int maxWidth) {
        if (getTextRenderer().getWidth(text) <= maxWidth) return text;
        String trimmed = text;
        while (trimmed.length() > 1 && getTextRenderer().getWidth(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "...";
    }

    private void renderColumnScrollbar(DrawContext c, ColumnState col) {
        if (col.maxScroll <= 0) return;
        int trackX = col.x + col.width - 3;
        c.fill(trackX, col.contentY, trackX + 2, col.contentY + col.contentHeight, theme.panel_separator());
        int thumbH = Math.max(10, col.contentHeight * col.contentHeight / Math.max(1, col.contentHeight + col.maxScroll));
        int thumbY = col.contentY + (col.contentHeight - thumbH) * col.scrollOffset / Math.max(1, col.maxScroll);
        c.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, theme.accent());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    private static final class ColumnState {
        final SettingsCategory category;
        final boolean expanded;
        final int x, y, width, height;
        int contentY, contentHeight;
        int scrollOffset, maxScroll;
        final List<RowEntry> rows = new ArrayList<>();
        final List<Button> controls = new ArrayList<>();
        Button headerButton;

        ColumnState(SettingsCategory category, boolean expanded, int x, int y, int width, int height) {
            this.category = category;
            this.expanded = expanded;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private record RowEntry(SettingsKey key, Button control, int index) {
    }
}