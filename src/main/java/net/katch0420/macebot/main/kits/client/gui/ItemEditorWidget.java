package net.katch0420.macebot.main.kits.client.gui;

import net.katch0420.macebot.client.gui.widgets.core.BaseWidget;
import net.katch0420.macebot.main.networking.packets.c2s.UpdateKitSlotC2SPacket;
import net.katch0420.macebot.main.utils.MatricesHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ItemEditorWidget extends BaseWidget {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;

    private final int minHeight;
    private MatricesHelper matrices;
    private int selectedSlot, count;
    private ItemStack selectedStack;
    private Mode currentMode = Mode.NORMAL;

    // UI State
    private boolean enchantExpanded = false;
    private final List<EnchantEntry> enchList = new ArrayList<>();

    // ── Colours — same palette as KitPopupScreen / KitViewScreen / KitEditorScreen ──
    private static final int COL_BG           = 0xEE1E1E2E; // panel fill (navy, semi-opaque)
    private static final int COL_BORDER       = 0xFF5566AA; // panel border
    private static final int COL_TITLE_BAR    = 0xFF2A2A4A; // section title bar
    private static final int COL_TITLE_ACCENT = 0xFF5566AA; // title bar bottom line
    private static final int COL_SEPARATOR    = 0xFF333355; // divider lines
    private static final int COL_TEXT         = 0xFFFFFFFF; // primary text
    private static final int COL_LABEL        = 0xFFAAAAAA; // label text ("Name:", "Count:")
    private static final int COL_DIM          = 0xFF888888; // hint / secondary text
    private static final int COL_ENCHANT_HDR  = 0xFF88FF88; // enchantment header (green accent)
    private static final int COL_BTN_NORMAL   = 0xFF252535; // idle button bg
    private static final int COL_BTN_HOVER    = 0xFF3A3A5A; // hovered button bg
    private static final int COL_BTN_BORDER   = 0xFF4455AA; // button border
    private static final int COL_BTN_DEL_N    = 0xFF3A1A1A; // delete button idle (dark red)
    private static final int COL_BTN_DEL_H    = 0xFF5A2A2A; // delete button hover
    private static final int COL_BTN_DEL_BRD  = 0xFFAA4444; // delete button border

    // Layout constants
    private static final int PADDING     = 8;
    private static final int LINE        = 16;
    private static final int BUTTON_SIZE = 14;
    private static final int GAP         = 5;
    private static final int TITLE_H     = 20; // inner title bar height
    private static final int MIN_MARGIN  = 36; // minimum vertical margin from screen edge

    // Widgets
    private TextFieldWidget countField;
    private SimpleButton nameEditButton;
    private SimpleButton enchantToggleButton;
    private SimpleButton enchantAddButton;
    private final List<SimpleButton> enchantDeleteButtons = new ArrayList<>();

    // Popups
    private NameEditorPopup nameEditorPopup;
    private EnchantmentEditorPopup enchantmentEditorPopup;

    // ── Construction ──────────────────────────────────────────────────────────

    public ItemEditorWidget(int x, int width, int screenHeight, int minHeight) {
        this.minHeight = minHeight;
        initWidgets();
        updateBounds(x, width, screenHeight);
    }

    private void initWidgets() {
        countField = new TextFieldWidget(textRenderer, 0, 0, 40, LINE, Text.literal("Count"));
        countField.setMaxLength(2);
        countField.setChangedListener(this::onCountChange);
        countField.setFocusUnlocked(true);
        countField.setEditableColor(COL_TEXT);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public Mode getCurrentMode() { return currentMode; }

    public void setSelectedStack(ItemStack stack, int slot) {
        this.selectedStack = stack;
        this.selectedSlot  = slot;
        updateFromStack();
        recalculateLayout();
    }

    public ItemStack getSelectedStack() { return selectedStack; }
    public int       getSelectedSlot()  { return selectedSlot;  }

    public void syncState() {
        if (selectedStack == null || selectedStack.isEmpty()) return;
        UpdateKitSlotC2SPacket.updateKitSlot(selectedSlot, selectedStack);
    }

    public void resize(int x, int width, int screenHeight) {
        updateBounds(x, width, screenHeight);
        if (nameEditorPopup        != null) nameEditorPopup.resize();
        if (enchantmentEditorPopup != null) enchantmentEditorPopup.resize();
    }

    public void recalculateLayout() {
        updateBounds(getX(), getWidth(), client.getWindow().getScaledHeight());
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private int calculateRequiredHeight() {
        if (selectedStack == null || selectedStack.isEmpty()) return minHeight;

        int h = PADDING;
        h += TITLE_H + GAP;       // inner title bar
        h += LINE + GAP;          // "Name:" label
        h += LINE + GAP;          // item name row
        h += 1 + GAP;             // separator
        h += LINE + GAP;          // "Count:" row
        h += 1 + GAP;             // separator
        h += LINE;                // enchantments header

        if (enchantExpanded) {
            int shown = Math.min(enchList.size(), 8);
            h += LINE;                      // badge row
            h += shown * (LINE + 3);        // entries
            if (enchList.size() > 8) h += LINE;
        }

        h += GAP + PADDING;
        return h;
    }

    private void updateBounds(int x, int width, int screenHeight) {
        setX(x);
        setWidth(width);

        int maxH      = screenHeight - (MIN_MARGIN * 2);
        int needed    = calculateRequiredHeight();
        int newHeight = Math.max(minHeight, Math.min(needed, maxH));

        setHeight(newHeight);
        setY((screenHeight - newHeight) / 2);
    }

    // ── Data sync ────────────────────────────────────────────────────────────

    private void onCountChange(String s) {
        try {
            int v = Integer.parseInt(s);
            if (v >= 1 && v <= 99 && selectedStack != null) {
                this.count = v;
                selectedStack.setCount(v);
                syncState();
            }
        } catch (NumberFormatException ignored) {}
    }

    private void updateFromStack() {
        enchList.clear();
        enchantDeleteButtons.clear();

        if (selectedStack == null || selectedStack.isEmpty()) {
            countField.setText("1");
            return;
        }

        countField.setText(String.valueOf(selectedStack.getCount()));

        ItemEnchantmentsComponent ec = selectedStack.getOrDefault(
                DataComponentTypes.ENCHANTMENTS,
                ItemEnchantmentsComponent.DEFAULT
        );
        ec.getEnchantments().forEach(e -> enchList.add(new EnchantEntry(e, ec.getLevel(e))));
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (nameEditorPopup        != null) return nameEditorPopup.mouseClicked(mx, my, btn);
        if (enchantmentEditorPopup != null) return enchantmentEditorPopup.mouseClicked(mx, my, btn);
        if (currentMode != Mode.NORMAL) return false;
        // Count field
        if (mx >= countField.getX() && mx < countField.getX() + countField.getWidth() &&
                my >= countField.getY() && my < countField.getY() + countField.getHeight()) {
            countField.mouseClicked(mx, my, btn);
            countField.setFocused(true);
            return true;
        } else {
            countField.setFocused(false);
        }

        if (nameEditButton     != null && nameEditButton.mouseClicked(mx, my, btn))   return true;
        if (enchantAddButton   != null && enchantAddButton.mouseClicked(mx, my, btn)) return true;

        if (enchantToggleButton != null && enchantToggleButton.mouseClicked(mx, my, btn)) {
            enchantExpanded = !enchantExpanded;
            recalculateLayout();
            return true;
        }

        for (SimpleButton b : enchantDeleteButtons) {
            if (b.mouseClicked(mx, my, btn)) return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(double mx, double my, int btn) {
        if (enchantmentEditorPopup != null) return enchantmentEditorPopup.mouseReleased(mx, my, btn);
        return false;
    }

    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (enchantmentEditorPopup != null) return enchantmentEditorPopup.mouseDragged(mx, my, btn, dx, dy);
        return false;
    }

    @Override public boolean mouseScrolled(double mx, double my, double hA, double vA) {
        if (enchantmentEditorPopup != null) return enchantmentEditorPopup.mouseScrolled(mx, my, hA, vA);
        return false;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (nameEditorPopup        != null) return nameEditorPopup.keyPressed(keyCode, scanCode, mods);
        if (enchantmentEditorPopup != null) return enchantmentEditorPopup.keyPressed(keyCode, scanCode, mods);
        if (countField.isFocused())         return countField.keyPressed(keyCode, scanCode, mods);
        return false;
    }

    @Override public boolean charTyped(char chr, int mods) {
        if (nameEditorPopup        != null) return nameEditorPopup.charTyped(chr, mods);
        if (enchantmentEditorPopup != null) return enchantmentEditorPopup.charTyped(chr, mods);
        if (countField.isFocused())         return countField.charTyped(chr, mods);
        return false;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        matrices = new MatricesHelper(ctx, textRenderer);

        // Popups take over the whole render when active
        if (currentMode != Mode.NORMAL) {
            if (nameEditorPopup        != null) nameEditorPopup.render(ctx, mx, my, delta);
            if (enchantmentEditorPopup != null) enchantmentEditorPopup.render(ctx, mx, my, delta);
            return;
        }

        renderPanel(ctx);

        if (selectedStack == null || selectedStack.isEmpty()) {
            renderEmpty(ctx);
        } else {
            renderEditor(ctx, mx, my, delta);
        }
    }

    // Navy panel + themed border
    private void renderPanel(DrawContext ctx) {
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COL_BG);
        ctx.drawBorder(getX(), getY(), getWidth(), getHeight(), COL_BORDER);
    }

    // Centered hint when no item is selected
    private void renderEmpty(DrawContext ctx) {
        int cx = getX() + getWidth() / 2;
        int y  = getY() + getHeight() / 2 - LINE;

        List<Text> lines = List.of(
                Text.literal("Select an item slot"),
                Text.literal("Ctrl+Click or Middle-Click a slot")
        );

        int maxW = lines.stream().mapToInt(textRenderer::getWidth).max().orElse(0);
        matrices.calculateScale(maxW, getWidth() - 2 * PADDING);

        matrices.drawScaledCenteredText(lines.get(0), cx, y,        COL_LABEL);
        matrices.drawScaledCenteredText(lines.get(1), cx, y + LINE, COL_DIM);
    }

    // Full item editor
    private void renderEditor(DrawContext ctx, int mx, int my, float delta) {
        enchantDeleteButtons.clear();

        int x     = getX();
        int w     = getWidth();
        int cx    = x + w / 2;
        int rEdge = x + w - PADDING; // right-aligned elements anchor

        // cursor y starts at top of widget
        int y = getY();

        // ── Inner title bar ───────────────────────────────────────────────────
        ctx.fill(x, y, x + w, y + TITLE_H, COL_TITLE_BAR);
        ctx.fill(x, y + TITLE_H - 1, x + w, y + TITLE_H, COL_TITLE_ACCENT);

        Text titleText = Text.literal("Slot " + selectedSlot);
        matrices.calculateScale(textRenderer.getWidth(titleText), w - 2 * PADDING);
        matrices.drawScaledCenteredText(titleText, cx,
                y + (TITLE_H - textRenderer.fontHeight) / 2, COL_TEXT);
        y += TITLE_H + GAP;

        // ── Name label ────────────────────────────────────────────────────────
        ctx.drawText(textRenderer, "§7Name:", x + PADDING, y, COL_LABEL, false);
        y += LINE;

        // Item name + edit button
        Text itemName = selectedStack.getName();
        int  nameMaxW = w - 3 * PADDING - BUTTON_SIZE - 2;
        matrices.calculateScale(textRenderer.getWidth(itemName), nameMaxW);
        matrices.drawScaledText(itemName, x + PADDING, y, COL_TEXT);

        nameEditButton = new SimpleButton(rEdge - BUTTON_SIZE, y, BUTTON_SIZE, BUTTON_SIZE,
                "✎", this::openNameEditor);
        nameEditButton.render(ctx, mx, my);
        y += LINE + GAP;

        // ── Separator ─────────────────────────────────────────────────────────
        ctx.fill(x + PADDING, y, x + w - PADDING, y + 1, COL_SEPARATOR);
        y += 1 + GAP;

        // ── Count row ─────────────────────────────────────────────────────────
        ctx.drawText(textRenderer, "§7Count:",
                x + PADDING,
                y + (LINE - textRenderer.fontHeight) / 2,
                COL_LABEL, false);

        countField.setX(rEdge - 40);
        countField.setY(y);
        countField.render(ctx, mx, my, delta);
        y += LINE + GAP;

        // ── Separator ─────────────────────────────────────────────────────────
        ctx.fill(x + PADDING, y, x + w - PADDING, y + 1, COL_SEPARATOR);
        y += 1 + GAP;

        // ── Enchantments section ──────────────────────────────────────────────
        renderEnchantSection(ctx, y, mx, my);
    }

    private void renderEnchantSection(DrawContext ctx, int y, int mx, int my) {
        int x     = getX();
        int w     = getWidth();
        int rEdge = x + w - PADDING;

        // Toggle header (invisible button over the text)
        String header  = (enchantExpanded ? "▼" : "▶") + " Enchantments";
        int    hdrW    = textRenderer.getWidth(header);
        matrices.calculateScale(hdrW, w - 3 * PADDING - BUTTON_SIZE - 2);
        int    scaledW = (int)(hdrW * matrices.getScale());

        enchantToggleButton = new SimpleButton(x + PADDING, y, scaledW, LINE, "", () -> {});
        matrices.drawScaledText(header, x + PADDING, y, COL_ENCHANT_HDR);

        // "+" add enchantment button
        enchantAddButton = new SimpleButton(rEdge - BUTTON_SIZE, y, BUTTON_SIZE, BUTTON_SIZE,
                "+", this::openEnchantPopup);
        enchantAddButton.render(ctx, mx, my);
        y += LINE;

        if (!enchantExpanded) return;

        // Badge showing count
        String badge = enchList.size() + " enchantment" + (enchList.size() == 1 ? "" : "s");
        ctx.drawText(textRenderer, badge, x + PADDING + 4, y + 2, COL_DIM, false);
        y += LINE;

        int maxShow = 8;
        for (int i = 0; i < Math.min(enchList.size(), maxShow); i++) {
            y = renderEnchantRow(ctx, y, mx, my, i);
        }

        if (enchList.size() > maxShow) {
            ctx.drawText(textRenderer,
                    "... +" + (enchList.size() - maxShow) + " more",
                    x + PADDING + GAP, y, COL_DIM, false);
        }
    }

    private int renderEnchantRow(DrawContext ctx, int y, int mx, int my, int idx) {
        int x     = getX();
        int w     = getWidth();
        int rEdge = x + w - PADDING;
        int rowH  = LINE + 2;

        // Alternating row tint
        ctx.fill(x + PADDING, y, x + w - PADDING, y + rowH,
                (idx % 2 == 0) ? 0x22FFFFFF : 0x11FFFFFF);

        EnchantEntry e    = enchList.get(idx);
        String       name = getEnchantmentName(e.ench);
        String       lvl  = String.valueOf(e.lvl);

        // Name (white) + level (grey), sized to fit
        int maxW = w - 4 * PADDING - BUTTON_SIZE - 2;
        matrices.calculateScale(textRenderer.getWidth(name + " " + lvl), maxW);
        matrices.drawScaledText(name, x + 2 * PADDING,
                y + (rowH - textRenderer.fontHeight) / 2, COL_TEXT);

        // Level right-aligned before delete button
        int lvlX = rEdge - BUTTON_SIZE - 4 - textRenderer.getWidth(lvl);
        ctx.drawText(textRenderer, lvl, lvlX, y + (rowH - textRenderer.fontHeight) / 2,
                COL_TEXT, false);

        // Red delete button
        SimpleButton delBtn = new ThemedButton(
                rEdge - BUTTON_SIZE, y + (rowH - BUTTON_SIZE) / 2,
                BUTTON_SIZE, BUTTON_SIZE, "×",
                () -> removeEnchant(idx),
                COL_BTN_DEL_N, COL_BTN_DEL_H, COL_BTN_DEL_BRD
        );
        delBtn.render(ctx, mx, my);
        enchantDeleteButtons.add(delBtn);

        return y + rowH + 1;
    }

    // ── Popup actions ─────────────────────────────────────────────────────────

    private void openNameEditor() {
        currentMode = Mode.NAME;
        nameEditorPopup = new NameEditorPopup(
                selectedStack.getName().getString(),
                this::handleNameSave,
                this::closeNameEditor
        );
    }

    private void handleNameSave(String newName) {
        selectedStack.set(DataComponentTypes.ITEM_NAME, parseColorCodes(newName));
        syncState();
        closeNameEditor();
    }

    private void closeNameEditor() {
        currentMode = Mode.NORMAL;
        nameEditorPopup = null;
    }

    private void openEnchantPopup() {
        currentMode = Mode.ENCHANTMENT;
        enchantmentEditorPopup = new EnchantmentEditorPopup(
                this::handleEnchantAdd,
                this::closeEnchantPopup
        );
    }

    private void handleEnchantAdd(RegistryEntry<Enchantment> ench, int level) {
        for (int i = 0; i < enchList.size(); i++) {
            if (enchList.get(i).ench.equals(ench)) {
                enchList.set(i, new EnchantEntry(ench, level));
                finalizeEnchantChange();
                return;
            }
        }
        enchList.add(new EnchantEntry(ench, level));
        finalizeEnchantChange();
    }

    private void finalizeEnchantChange() {
        rebuildEnchantments();
        syncState();
        recalculateLayout();
        closeEnchantPopup();
    }

    private void closeEnchantPopup() {
        currentMode = Mode.NORMAL;
        enchantmentEditorPopup = null;
    }

    private void removeEnchant(int index) {
        if (index < 0 || index >= enchList.size()) return;
        enchList.remove(index);
        rebuildEnchantments();
        syncState();
        recalculateLayout();
    }

    private void rebuildEnchantments() {
        ItemEnchantmentsComponent.Builder b =
                new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        for (EnchantEntry e : enchList) b.add(e.ench, e.lvl);
        selectedStack.set(DataComponentTypes.ENCHANTMENTS, b.build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Text parseColorCodes(String input) {
        if (input == null || input.isEmpty()) return Text.empty();
        MutableText result = Text.empty();
        StringBuilder cur  = new StringBuilder();
        Style style        = Style.EMPTY;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < input.length()) {
                if (!cur.isEmpty()) {
                    result.append(Text.literal(cur.toString()).setStyle(style));
                    cur = new StringBuilder();
                }
                char code = input.charAt(++i);
                Formatting fmt = Formatting.byCode(code);
                if (fmt != null) {
                    style = fmt.isColor() ? Style.EMPTY.withFormatting(fmt) : style.withFormatting(fmt);
                }
            } else {
                cur.append(c);
            }
        }
        if (!cur.isEmpty()) result.append(Text.literal(cur.toString()).setStyle(style));
        return result;
    }

    private String getEnchantmentName(RegistryEntry<Enchantment> entry) {
        return entry.getKey()
                .map(k -> k.getValue().getPath())
                .map(s -> s.replace("_", " "))
                .map(ItemEditorWidget::capitalizeWords)
                .orElse("Unknown");
    }

    private static String capitalizeWords(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char c : s.toCharArray()) {
            if (c == ' ' || c == '_') { cap = true; sb.append(' '); }
            else if (cap)             { sb.append(Character.toUpperCase(c)); cap = false; }
            else                      { sb.append(c); }
        }
        return sb.toString();
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    public enum Mode { NORMAL, ENCHANTMENT, PROPERTY, NAME }

    public record EnchantEntry(RegistryEntry<Enchantment> ench, int lvl) {}

    /**
     * SimpleButton variant with custom idle/hover/border colours.
     * Keeps SimpleButton unchanged; just overrides paint.
     */
    private static class ThemedButton extends SimpleButton {
        private final int bgNormal, bgHover, border;

        ThemedButton(int x, int y, int w, int h, String label, Runnable action,
                     int bgNormal, int bgHover, int border) {
            super(x, y, w, h, label, action);
            this.bgNormal = bgNormal;
            this.bgHover  = bgHover;
            this.border   = border;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my) {
            boolean hov = mx >= getX() && mx < getX() + getWidth()
                    && my >= getY() && my < getY() + getHeight();
            ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                    hov ? bgHover : bgNormal);
            ctx.drawBorder(getX(), getY(), getWidth(), getHeight(), border);
            MinecraftClient mc = MinecraftClient.getInstance();
            ctx.drawText(mc.textRenderer, label,
                    getX() + (getWidth()  - mc.textRenderer.getWidth(label)) / 2,
                    getY() + (getHeight() - mc.textRenderer.fontHeight)    / 2,
                    0xFFFFFF, false);
        }
    }
}