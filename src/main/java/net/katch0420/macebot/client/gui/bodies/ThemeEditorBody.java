package net.katch0420.macebot.client.gui.bodies;

import net.katch0420.macebot.client.gui.themes.*;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.*;

import static net.katch0420.macebot.client.MaceBotClient.mainFrame;

public class ThemeEditorBody extends Body {

    private int margin;
    private int textHeight;

    private int panelX, panelY, panelWidth, panelHeight;

    // Left: theme browser
    private int browserX, browserY, browserWidth, browserHeight;
    private int browserHeaderHeight;
    private int profileRowHeight;
    private int profileBtnHeight, profileBtnWidth;

    // Right: color tree
    private int treeX, treeY, treeWidth, treeHeight;
    private int rowHeight;
    private int swatchWidth, swatchHeight;
    private int indentPerDepth;
    private int treeScroll = 0, treeMaxScroll = 0;

    private final Set<String> expandedPaths = new HashSet<>();
    private final List<TreeRow> treeRows = new ArrayList<>();

    private Button newButton, importButton;
    private final List<Button> profileButtons = new ArrayList<>();
    private final List<Button> treeButtons = new ArrayList<>();

    private String statusMessage;
    private long statusExpiryMs;

    @Override
    public Text getLabel() {
        return Text.of("Theme Editor");
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

        browserHeaderHeight = s(18, 14);
        profileRowHeight = s(18, 15);
        profileBtnHeight = s(13, 11);
        profileBtnWidth = s(42, 34);

        rowHeight = s(16, 13);
        swatchWidth = s(30, 22);
        swatchHeight = s(12, 10);
        indentPerDepth = s(11, 8);

        browserX = panelX + margin;
        browserY = panelY + margin;
        browserWidth = Math.max(s(150, 110), panelWidth * 3 / 10);
        browserHeight = panelHeight - 2 * margin;

        treeX = browserX + browserWidth + margin;
        treeY = browserY;
        treeWidth = panelWidth - margin - treeX + panelX;
        treeHeight = browserHeight;

        buildBrowser();
        rebuildTreeRows();
    }

    // ------------------------------------------------------------------
    // Left column: theme browser
    // ------------------------------------------------------------------

    private void buildBrowser() {
        for (Button b : profileButtons) remove(b);
        profileButtons.clear();
        if (newButton != null) remove(newButton);
        if (importButton != null) remove(importButton);

        int btnW = (browserWidth - margin) / 2;
        newButton = Button.builder()
                .position(browserX, browserY)
                .size(btnW, browserHeaderHeight)
                .baseLabel(Text.of("+ New"))
                .backgroundColor(theme.accent())
                .foregroundColor(0xFFFFFFFF)
                .borderColor(-1)
                .hoverColor(theme.accent() + 0xFF101010)
                .holdColor(theme.accent() + 0xFF202020)
                .onClick(b -> promptNewTheme())
                .build();
        addDrawableChild(newButton);

        importButton = Button.builder()
                .position(browserX + btnW + margin, browserY)
                .size(btnW, browserHeaderHeight)
                .baseLabel(Text.of("Import"))
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> importFromClipboard())
                .build();
        addDrawableChild(importButton);

        int rowY = browserY + browserHeaderHeight + margin;
        for (ThemeProfile profile : ThemeManager.allThemes()) {
            boolean isActive = profile.name().equals(ThemeManager.activeName) && profile.builtin() == ThemeManager.activeIsBuiltin;
            int rightEdge = browserX + browserWidth;
            int bx = rightEdge - profileBtnWidth;

            if (!profile.builtin()) {
                Button delete = smallButton(bx, rowY, "Del", theme.danger(), () -> deleteTheme(profile));
                profileButtons.add(delete);
                bx -= profileBtnWidth + margin / 2;
            }
            Button export = smallButton(bx, rowY, "Exp", theme.body_button_background(), () -> exportTheme(profile));
            profileButtons.add(export);

            if (!isActive) {
                Button use = smallButton(bx - profileBtnWidth - margin / 2, rowY, "", theme.body_button_background(), () -> useTheme(profile));
                use.getDisplayData().setLabel(Text.of("Use"));
                profileButtons.add(use);
            }
            rowY += profileRowHeight;
        }
        for (Button b : profileButtons) addDrawableChild(b);
    }

    private Button smallButton(int x, int y, String label, int bg, Runnable onClick) {
        return Button.builder()
                .position(x, y + (profileRowHeight - profileBtnHeight) / 2)
                .size(profileBtnWidth, profileBtnHeight)
                .baseLabel(Text.of(label))
                .backgroundColor(bg)
                .foregroundColor(0xFFFFFFFF)
                .borderColor(-1)
                .hoverColor(bg + 0xFF101010)
                .holdColor(bg + 0xFF202020)
                .onClick(b -> onClick.run())
                .build();
    }

    private void useTheme(ThemeProfile profile) {
        ThemeManager.setActive(profile);
        showStatus("Switched to \"" + profile.name() + "\"");
        mainFrame.applyTheme();
    }

    private void deleteTheme(ThemeProfile profile) {
        ThemeManager.deleteCustom(profile.name());
        showStatus("Deleted \"" + profile.name() + "\"");
        clearAndInit();
    }

    private void exportTheme(ThemeProfile profile) {
        String code = ThemeCodec.encode(profile.name(), profile.theme());
        MinecraftClient.getInstance().keyboard.setClipboard(code);
        showStatus("Copied \"" + profile.name() + "\" to clipboard");
    }

    private void promptNewTheme() {
        String suggested = ThemeManager.uniqueName(ThemeManager.activeName + " Copy");
        MinecraftClient.getInstance().setScreen(new ThemeNamePromptScreen(
                parentScreen, Text.literal("New Theme"), suggested,
                name -> {
                    String unique = ThemeManager.uniqueName(name);
                    ThemeProfile created = new ThemeProfile(unique, false, ThemeManager.copy(Themes.CURRENT));
                    ThemeManager.saveCustom(created);
                    ThemeManager.setActive(created);
                    showStatus("Created \"" + unique + "\"");
                }
        ));
    }

    private void importFromClipboard() {
        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        var decoded = ThemeCodec.decode(clipboard);
        if (decoded.isEmpty()) {
            showStatus("Clipboard doesn't contain a valid theme code");
            return;
        }
        ThemeProfile imported = decoded.get();
        MinecraftClient.getInstance().setScreen(new ThemeNamePromptScreen(
                parentScreen, Text.literal("Import Theme"), ThemeManager.uniqueName(imported.name()),
                name -> {
                    String unique = ThemeManager.uniqueName(name);
                    ThemeProfile saved = new ThemeProfile(unique, false, imported.theme());
                    ThemeManager.saveCustom(saved);
                    ThemeManager.setActive(saved);
                    showStatus("Imported \"" + unique + "\"");
                }
        ));
    }

    private void showStatus(String message) {
        statusMessage = message;
        statusExpiryMs = System.currentTimeMillis() + 3000;
    }

    // ------------------------------------------------------------------
    // Right column: color tree (lightweight clickable text, not big buttons)
    // ------------------------------------------------------------------

    private void rebuildTreeRows() {
        for (Button b : treeButtons) remove(b);
        treeButtons.clear();
        treeRows.clear();

        ThemeGroup root = ThemeTree.build(Themes.CURRENT);
        flatten(root, 0, "", treeRows);

        int i = 0;
        for (TreeRow row : treeRows) {
            row.index = i++;
            if (!row.isHeader) {
                row.control = buildSwatch(row);
                addDrawableChild(row.control);
                treeButtons.add(row.control);
            }
            // Header rows get NO button - they're plain clickable text, hit-tested manually (see mouseClicked).
        }

        int totalHeight = treeRows.size() * rowHeight;
        treeMaxScroll = Math.max(0, totalHeight - treeHeight);
        treeScroll = Math.max(0, Math.min(treeScroll, treeMaxScroll));
        relayoutTree();
    }

    private void flatten(ThemeGroup node, int depth, String pathPrefix, List<TreeRow> out) {
        for (ThemeGroup child : node.children) {
            String path = pathPrefix.isEmpty() ? child.name : pathPrefix + "/" + child.name;
            boolean expanded = expandedPaths.contains(path);
            out.add(TreeRow.header(child, depth, path, expanded));
            if (expanded) {
                for (ThemeGroup.ColorField field : child.fields) {
                    out.add(TreeRow.field(field, depth + 1, path));
                }
                flatten(child, depth + 1, path, out);
            }
        }
    }

    private Button buildSwatch(TreeRow row) {
        int color = row.field.getter().getAsInt();
        return Button.builder()
                .size(swatchWidth, swatchHeight)
                .backgroundColor(color)
                .borderColor(0xFFFFFFFF)
                .hoverColor(color)
                .holdColor(color)
                .onClick(b -> openPicker(row))
                .build();
    }

    /**
     * Header rows are plain text, not Buttons - hit-tested here instead of via the widget tree.
     */
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && mx >= treeX && mx <= treeX + treeWidth && my >= treeY && my <= treeY + treeHeight) {
            for (TreeRow row : treeRows) {
                if (!row.isHeader) continue;
                int rowTop = treeY + row.index * rowHeight - treeScroll;
                if (rowTop + rowHeight < treeY || rowTop > treeY + treeHeight) continue;
                int depthIndent = row.depth * indentPerDepth;
                int textWidth = getTextRenderer().getWidth(headerLabel(row));
                if (mx >= treeX + depthIndent && mx <= treeX + depthIndent + textWidth + 6 && my >= rowTop && my <= rowTop + rowHeight) {
                    toggleExpand(row.path);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private String headerLabel(TreeRow row) {
        return (row.expanded ? "\u25BC " : "\u25B6 ") + row.group.name;
    }

    private void toggleExpand(String path) {
        if (expandedPaths.contains(path)) expandedPaths.remove(path);
        else expandedPaths.add(path);
        rebuildTreeRows();
    }

    private void openPicker(TreeRow row) {
        boolean forked = ThemeManager.ensureEditableActive();
        ThemeGroup.ColorField field = row.field;

        if (forked) {
            // The field captured in `row` is bound to the now-discarded
            // built-in Theme instance - editing through it would corrupt the
            // built-in constant for the rest of the session. Re-resolve the
            // equivalent field on the freshly forked Theme instead, then
            // refresh the whole panel so subsequent clicks use fresh bindings.
            field = resolveField(row.path, row.field.name());
            buildBrowser();
            rebuildTreeRows();
        }
        if (field == null) return;

        int initial = field.getter().getAsInt();
        ThemeGroup.ColorField finalField = field;
        MinecraftClient.getInstance().setScreen(new ColorPickerScreen(
                parentScreen, Text.literal(field.name()), initial,
                finalField.setter(), ThemeManager::saveActive
        ));
    }

    private ThemeGroup.ColorField resolveField(String groupPath, String fieldName) {
        ThemeGroup node = ThemeTree.build(Themes.CURRENT);
        for (String segment : groupPath.split("/")) {
            node = node.children.stream().filter(c -> c.name.equals(segment)).findFirst().orElse(node);
        }
        return node.fields.stream().filter(f -> f.name().equals(fieldName)).findFirst().orElse(null);
    }

    private void relayoutTree() {
        for (TreeRow row : treeRows) {
            if (row.control == null) continue;
            int rowTop = treeY + row.index * rowHeight - treeScroll;
            int swatchX = treeX + treeWidth - swatchWidth - margin;
            int swatchY = rowTop + (rowHeight - swatchHeight) / 2;
            row.control.setPosition(swatchX, swatchY);
            boolean visible = rowTop + rowHeight > treeY && rowTop < treeY + treeHeight;
            row.control.setVisible(visible);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < treeX || mouseX > treeX + treeWidth || mouseY < treeY || mouseY > treeY + treeHeight) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (treeMaxScroll <= 0) return true;
        treeScroll = Math.max(0, Math.min(treeMaxScroll, treeScroll - (int) (verticalAmount * rowHeight / 2)));
        relayoutTree();
        return true;
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        c.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, theme.body_background());

        // Browser border
        c.fill(browserX - 2, browserY - 2, browserX + browserWidth + 2, browserY + browserHeight + 2, 0);
        drawBorder(c, browserX - 2, browserY - 2, browserWidth + 4, browserHeight + 4);
        renderBrowserText(c);

        // Tree border
        drawBorder(c, treeX - 2, treeY - 2, treeWidth + 4, treeHeight + 4);

        newButton.render(c, mx, my, d);
        importButton.render(c, mx, my, d);
        for (Button b : profileButtons) b.render(c, mx, my, d);

        c.enableScissor(treeX, treeY, treeX + treeWidth, treeY + treeHeight);
        renderTreeText(c);
        for (Button b : treeButtons) {
            if (b.isVisible()) b.render(c, mx, my, d);
        }
        c.disableScissor();

        renderTreeScrollbar(c);
        renderStatusMessage(c);
    }

    private void drawBorder(DrawContext c, int x, int y, int w, int h) {
        c.fill(x, y, x + w, y + 1, theme.body_border());
        c.fill(x, y + h - 1, x + w, y + h, theme.body_border());
        c.fill(x, y, x + 1, y + h, theme.body_border());
        c.fill(x + w - 1, y, x + w, y + h, theme.body_border());
    }

    private void renderBrowserText(DrawContext c) {
        int rowY = browserY + browserHeaderHeight + margin;
        for (ThemeProfile profile : ThemeManager.allThemes()) {
            boolean isActive = profile.name().equals(ThemeManager.activeName) && profile.builtin() == ThemeManager.activeIsBuiltin;
            int textY = rowY + (profileRowHeight - textHeight) / 2;
            int color = isActive ? theme.accent() : theme.body_value();
            String label = (isActive ? "\u25CF " : "") + profile.name();
            c.drawText(getTextRenderer(), Text.of(label), browserX, textY, color, true);
            rowY += profileRowHeight;
        }
    }

    private void renderTreeText(DrawContext c) {
        for (TreeRow row : treeRows) {
            int rowTop = treeY + row.index * rowHeight - treeScroll;
            if (rowTop + rowHeight < treeY || rowTop > treeY + treeHeight) continue;
            int depthIndent = row.depth * indentPerDepth;
            int textY = rowTop + (rowHeight - textHeight) / 2;

            if (row.isHeader) {
                c.drawText(getTextRenderer(), Text.of(headerLabel(row)), treeX + depthIndent, textY, theme.body_label(), true);
            } else {
                c.drawText(getTextRenderer(), Text.of(row.field.name()), treeX + depthIndent, textY, theme.body_value(), true);
            }
            if (row.index > 0) {
                c.drawHorizontalLine(treeX, treeX + treeWidth, rowTop, theme.panel_separator() & 0x22FFFFFF);
            }
        }
    }

    private void renderTreeScrollbar(DrawContext c) {
        if (treeMaxScroll <= 0) return;
        int trackX = treeX + treeWidth - 3;
        c.fill(trackX, treeY, trackX + 2, treeY + treeHeight, theme.panel_separator());
        int thumbH = Math.max(10, treeHeight * treeHeight / Math.max(1, treeHeight + treeMaxScroll));
        int thumbY = treeY + (treeHeight - thumbH) * treeScroll / Math.max(1, treeMaxScroll);
        c.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, theme.accent());
    }

    private void renderStatusMessage(DrawContext c) {
        if (statusMessage == null || System.currentTimeMillis() > statusExpiryMs) return;
        int tw = getTextRenderer().getWidth(statusMessage);
        int sx = panelX + (panelWidth - tw) / 2;
        int sy = panelY + panelHeight - textHeight - margin;
        c.fill(sx - 6, sy - 2, sx + tw + 6, sy + textHeight + 2, 0xCC101010);
        c.drawText(getTextRenderer(), Text.of(statusMessage), sx, sy, theme.accent(), true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    private static final class TreeRow {
        final boolean isHeader;
        final ThemeGroup group;
        final ThemeGroup.ColorField field;
        final int depth;
        final String path;
        final boolean expanded;
        int index;
        Button control;

        private TreeRow(boolean isHeader, ThemeGroup group, ThemeGroup.ColorField field, int depth, String path, boolean expanded) {
            this.isHeader = isHeader;
            this.group = group;
            this.field = field;
            this.depth = depth;
            this.path = path;
            this.expanded = expanded;
        }

        static TreeRow header(ThemeGroup group, int depth, String path, boolean expanded) {
            return new TreeRow(true, group, null, depth, path, expanded);
        }

        static TreeRow field(ThemeGroup.ColorField field, int depth, String groupPath) {
            return new TreeRow(false, null, field, depth, groupPath, false);
        }
    }
}