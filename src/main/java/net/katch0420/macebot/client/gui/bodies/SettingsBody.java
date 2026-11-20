package net.katch0420.macebot.client.gui.bodies;

import net.katch0420.macebot.client.gui.widgets.builder.SettingControlRegistry;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.main.macebot.ai.Mode;
import net.katch0420.macebot.main.settings.Flags;
import net.katch0420.macebot.main.settings.Setting;
import net.katch0420.macebot.main.settings.SettingManager;
import net.katch0420.macebot.main.settings.Settings;
import net.katch0420.macebot.main.settings.SettingsCategories;
import net.katch0420.macebot.main.settings.SettingsCategories.SettingCategory;
import net.katch0420.macebot.main.utils.LegacyText;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.Minecraft;
//? if <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingsBody extends Body {
    private final ThemeEditorBody themeEditorBody = new ThemeEditorBody();
    private final List<SettingCategory> categories = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<RowEntry> rows = new ArrayList<>();
    private final List<Button> controls = new ArrayList<>();

    private SettingCategory selectedCategory;
    private Button themeButton;
    private int margin;
    private int panelX, panelY, panelWidth, panelHeight;
    private int statusHeight, tabHeight, tabsHeight, contentY, contentHeight, rowHeight, controlHeight;
    private int scrollOffset, maxScroll;

    @Override
    public Component getLabel() {
        return Component.literal("Settings");
    }

    @Override
    public void init() {
        super.init();
        margin = s(6, 3);
        panelX = x;
        panelY = y;
        panelWidth = Math.max(1, availableWidth);
        panelHeight = Math.max(1, availableHeight);
        rowHeight = s(21, 14);
        controlHeight = Math.max(s(15, 11), getFont().lineHeight + 2);
        statusHeight = controlHeight + margin;
        tabHeight = controlHeight;

        categories.clear();
        categories.addAll(SettingsCategories.getCategories().stream()
                .filter(category -> !category.hasFlag(Flags.CATEGORY_DO_NOT_DISPLAY))
                .sorted(Comparator.comparingInt(SettingsBody::categoryOrder)
                        .thenComparing(category -> category.getDisplayName().getString()))
                .toList());
        if (selectedCategory == null || !categories.contains(selectedCategory)) {
            selectedCategory = categories.stream()
                    .filter(category -> !SettingManager.getSettings(category).isEmpty())
                    .findFirst()
                    .orElse(categories.isEmpty() ? null : categories.getFirst());
        }

        int themeButtonWidth = Math.min(s(56, 38), Math.max(1, panelWidth / 3));
        themeButton = Button.builder()
                .position(panelX + panelWidth - margin - themeButtonWidth, panelY)
                .size(themeButtonWidth, controlHeight)
                .baseLabel(Component.literal("Theme"))
                .backgroundColor(theme.accent())
                .foregroundColor(0xFFFFFFFF)
                .borderColor(theme.body_button_border())
                .hoverColor(theme.accent_hover())
                .holdColor(theme.accent())
                .onClick(button -> parentScreen.navigateTo(themeEditorBody))
                .build();
        addDrawableChild(themeButton);

        buildTabs();
        contentY = panelY + statusHeight + tabsHeight + margin;
        contentHeight = Math.max(1, panelHeight - statusHeight - tabsHeight - margin);
        buildRows();
    }

    private static int categoryOrder(SettingCategory category) {
        return switch (category.getId()) {
            case "bot" -> 0;
            case "player" -> 1;
            case "mod" -> 2;
            case "status" -> 3;
            default -> 4;
        };
    }

    private void buildTabs() {
        for (Button button : tabButtons) remove(button);
        tabButtons.clear();
        if (categories.isEmpty()) return;

        int gap = Math.min(margin / 2, 2);
        int tabsPerRow = Math.max(1, (panelWidth + gap) / (s(52, 30) + gap));
        int rows = (categories.size() + tabsPerRow - 1) / tabsPerRow;
        tabsHeight = rows * tabHeight + Math.max(0, rows - 1) * gap;
        for (int row = 0; row < rows; row++) {
            int start = row * tabsPerRow;
            int count = Math.min(tabsPerRow, categories.size() - start);
            int available = Math.max(count, panelWidth - (count - 1) * gap);
            int baseWidth = available / count;
            int remainder = available % count;
            int tabX = panelX;
            int tabY = panelY + statusHeight + row * (tabHeight + gap);
            for (int column = 0; column < count; column++) {
                SettingCategory category = categories.get(start + column);
                int width = baseWidth + (column < remainder ? 1 : 0);
                boolean selected = category == selectedCategory;
                int background = selected ? theme.accent() : theme.body_button_background();
                Button button = Button.builder()
                        .position(tabX, tabY)
                        .size(width, tabHeight)
                        .baseLabel(category.getDisplayName())
                        .backgroundColor(background)
                        .foregroundColor(selected ? 0xFFFFFFFF : theme.body_button_foreground())
                        .borderColor(theme.body_button_border())
                        .hoverColor(selected ? theme.accent_hover() : theme.body_button_background())
                        .holdColor(theme.accent())
                        .onClick(clicked -> selectCategory(category))
                        .build();
                tabButtons.add(button);
                addDrawableChild(button);
                tabX += width + gap;
            }
        }
    }

    private void selectCategory(SettingCategory category) {
        if (selectedCategory == category) return;
        selectedCategory = category;
        scrollOffset = 0;
        buildTabs();
        buildRows();
    }

    public void focusColumn(SettingCategory category) {
        if (!categories.contains(category)) return;
        selectedCategory = category;
        scrollOffset = 0;
        buildTabs();
        buildRows();
    }

    private void buildRows() {
        for (Button button : controls) remove(button);
        controls.clear();
        rows.clear();

        if (selectedCategory == null) {
            maxScroll = 0;
            scrollOffset = 0;
            return;
        }

        List<Setting<?>> settings = SettingManager.getSettings(selectedCategory).stream()
                .filter(setting -> !setting.hasFlag(Flags.CATEGORY_DO_NOT_DISPLAY))
                .toList();
        int width = Math.max(1, panelWidth - 2 * margin);
        int controlWidth = Math.max(1, Math.min(Math.max(s(120, 52), width / 3), width / 2));
        for (int i = 0; i < settings.size(); i++) {
            Setting<?> setting = settings.get(i);
            Button control = buildControl(setting);
            if (control != null) {
                control.setSize(controlWidth, controlHeight);
                controls.add(control);
                addDrawableChild(control);
            }
            rows.add(new RowEntry(setting, control, i));
        }
        maxScroll = Math.max(0, rows.size() * rowHeight - contentHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        relayoutRows();
    }

    private Button buildControl(Setting<?> setting) {
        if (setting.hasFlag(Flags.SETTING_CAN_NOT_MODIFY)) return null;
        if (setting.isEnum()) return buildEnumControl(setting);

        Button control = SettingControlRegistry.create(setting, theme, this::buildRows);
        if (control != null && setting == Settings.PRACTICE_MODE) {
            control.setActiveSupplier(() -> Settings.MODE.getValue() == Mode.PRACTICE);
        }
        return control;
    }

    private Button buildEnumControl(Setting<?> setting) {
        return Button.builder()
                .baseLabel(getEnumText(setting.getValue()))
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .borderColor(theme.body_button_border())
                .hoverColor(theme.accent_hover())
                .holdColor(theme.accent())
                .onClick(button -> button.setLabelIfClickButton(getEnumText(setting.rollAndGet())))
                .build();
    }

    private Component getEnumText(Object value) {
        if (value instanceof Mode mode) return mode.displayText;
        return Component.literal(LegacyText.getEnumValueAsString(String.valueOf(value)));
    }

    private void relayoutRows() {
        for (RowEntry row : rows) {
            if (row.control() == null) continue;
            int rowTop = contentY + row.index() * rowHeight - scrollOffset;
            row.control().setPosition(panelX + panelWidth - margin - row.control().getWidth(),
                    rowTop + (rowHeight - controlHeight) / 2);
            row.control().setVisible(rowTop + rowHeight > contentY && rowTop < contentY + contentHeight);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < panelX || mouseX > panelX + panelWidth || mouseY < contentY || mouseY > contentY + contentHeight) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (maxScroll <= 0) return true;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * rowHeight / 2)));
        relayoutRows();
        return true;
    }

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, theme.body_background());
        renderStatus(graphics);
        for (Button button : tabButtons) button.render(graphics, mouseX, mouseY, delta);

        graphics.fill(panelX, contentY, panelX + panelWidth, contentY + contentHeight, theme.body_background());
        drawBorder(graphics, panelX, contentY, panelWidth, contentHeight);
        graphics.enableScissor(panelX + 1, contentY + 1, panelX + panelWidth - 1, contentY + contentHeight - 1);
        for (RowEntry row : rows) renderRow(graphics, row);
        graphics.disableScissor();
        renderScrollbar(graphics);
        themeButton.render(graphics, mouseX, mouseY, delta);
    }

    private void renderStatus(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        boolean online = Settings.MACEBOT_ONLINE.getValue();
        var opponent = client.level == null ? null : client.level.getEntity(Settings.OPPONENT_ID.getValue());
        String opponentName = opponent == null ? String.valueOf(Settings.OPPONENT_ID.getValue()) : opponent.getDisplayName().getString();
        String status = "MaceBot: " + (online ? "Online" : "Offline") + " | Type: "
                + Settings.MACEBOT_ID.getValue() + " | Opponent: " + opponentName;
        int textWidth = Math.max(1, panelWidth - themeButton.getWidth() - 3 * margin);
        drawFitted(graphics, Component.literal(status), panelX + margin,
                panelY + (statusHeight - getFont().lineHeight) / 2, textWidth, getFont().lineHeight,
                online ? theme.success() : theme.danger());
    }

    private void renderRow(GuiGraphics graphics, RowEntry row) {
        int rowTop = contentY + row.index() * rowHeight - scrollOffset;
        if (rowTop + rowHeight <= contentY || rowTop >= contentY + contentHeight) return;
        if (row.index() > 0) {
            graphics.hLine(panelX + margin, panelX + panelWidth - margin, rowTop,
                    theme.panel_separator() & 0x33FFFFFF);
        }

        int textY = rowTop + (rowHeight - getFont().lineHeight) / 2;
        String value = row.control() == null ? String.valueOf(row.setting().getValue()) : null;
        int valueX = value == null ? panelX + panelWidth - margin
                : panelX + panelWidth - margin - getFont().width(value);
        int rightEdge = row.control() == null ? valueX - margin : row.control().getX() - margin;
        int markerWidth = row.setting().hasFlag(Flags.SETTING_EXPERIMENTAL) ? getFont().width("!") + margin / 2 : 0;
        int labelWidth = Math.max(1, rightEdge - panelX - margin - markerWidth);
        drawFitted(graphics, row.setting().getDisplayName(), panelX + margin, textY,
                labelWidth, getFont().lineHeight, theme.body_label());

        if (row.control() == null) {
            drawFitted(graphics, Component.literal(value == null ? "" : value), valueX, textY,
                    Math.max(1, panelX + panelWidth - margin - valueX), getFont().lineHeight, theme.body_value());
        } else {
            row.control().render(graphics, -1, -1, 0);
        }

        if (row.setting().hasFlag(Flags.SETTING_EXPERIMENTAL)) {
            int markerX = row.control() == null ? rightEdge : row.control().getX() - margin / 2 - getFont().width("!");
            graphics.drawString(getFont(), "!", markerX, textY,
                    theme.warning(), false);
        }
    }

    private void drawFitted(GuiGraphics graphics, Component text, int x, int y, int maxWidth,
                            int maxHeight, int color) {
        float widthScale = maxWidth <= 0 ? 1.0f : maxWidth / (float) Math.max(1, getFont().width(text));
        float heightScale = maxHeight <= 0 ? 1.0f : maxHeight / (float) getFont().lineHeight;
        float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
        YarnHelpers.pushMatrix(graphics);
        //? if >=1.21.6 {
        graphics.pose().translate(x, y);
        //?} else
        //graphics.pose().translate(x, y, 0);
        //? if >=1.21.6 {
        graphics.pose().scale(scale, scale);
        //?} else
        //graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(getFont(), text, 0, 0, color, true);
        YarnHelpers.popMatrix(graphics);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, theme.body_border());
        graphics.fill(x, y + height - 1, x + width, y + height, theme.body_border());
        graphics.fill(x, y, x + 1, y + height, theme.body_border());
        graphics.fill(x + width - 1, y, x + width, y + height, theme.body_border());
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (maxScroll <= 0) return;
        int trackX = panelX + panelWidth - 3;
        graphics.fill(trackX, contentY + 1, trackX + 2, contentY + contentHeight - 1, theme.panel_separator());
        int thumbHeight = Math.max(8, contentHeight * contentHeight / Math.max(1, contentHeight + maxScroll));
        int thumbY = contentY + 1 + (contentHeight - thumbHeight) * scrollOffset / maxScroll;
        graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, theme.accent());
    }

    private record RowEntry(Setting<?> setting, Button control, int index) {}
}
