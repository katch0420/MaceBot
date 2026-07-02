package net.katch0420.macebot.client.gui.bodies.popup;

import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitViewPopup extends PopupScreen {

    private final Kit    kit;
    private final String displayName;
    private final String iconItemId;

    private final Map<Integer, ItemStack> slots = new HashMap<>();

    // Layout
    private int margin;
    private int titleH;
    private int slotSize;
    private int slotPad;
    private int mainGridX;
    private int mainGridY;
    private int armorX;
    private int hotbarY;

    private int hoveredSlot = -1;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Full constructor — displayName and iconItemId shown in title bar. */
    public KitViewPopup(Screen parent, Kit kit, String displayName, String iconItemId) {
        super(parent);
        this.kit         = kit;
        this.displayName = displayName;
        this.iconItemId  = iconItemId;
        this.title       = Text.literal("Kit Viewer");
    }

    /** Convenience — reads name/icon from the kit itself. */
    public KitViewPopup(Screen parent, Kit kit) {
        this(parent, kit, kit.getDisplayName(), kit.getIconId().getPath());
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        margin = 8;

        loadSlots();
        calculateLayout();
    }

    private void loadSlots() {
        slots.clear();
        if (kit == null) return;
        Kit src = KitRegistry.get(kit.getId());
        if (src == null) src = kit;
        src.getItems().forEach((slot, ks) -> {
            ItemStack s = ks.toStack();
            if (s != null && !s.isEmpty()) slots.put(slot, s);
        });
    }

    private void calculateLayout() {
        // Size slots to fit within a reasonable popup
        // 9 main cols + 1 armor col + labels
        int availW = Math.min(width * 3 / 5, 420) - margin * 4;
        slotSize = Math.max(14, Math.min(20, availW / 11));
        slotPad  = Math.max(2, slotSize / 8);

        titleH = textRenderer.fontHeight + margin * 2;

        int armorColW    = slotSize + margin;
        int mainGridW    = 9 * (slotSize + slotPad) - slotPad;
        int totalGridW   = armorColW + mainGridW;

        // 3 inv rows + hotbar gap + hotbar + armor (5 slots)
        int invH    = 3 * (slotSize + slotPad);
        int hotbarGap = slotPad * 3;
        int gridH   = invH + hotbarGap + slotSize;
        int armorH  = 5 * (slotSize + slotPad);
        int totalGridH = Math.max(gridH, armorH);

        // Summary line below grid
        int summaryH = textRenderer.fontHeight + margin;

        popupW = totalGridW + margin * 2;
        popupH = titleH + margin + totalGridH + summaryH + margin;

        // Clamp to screen
        popupW = Math.min(popupW, width  - margin * 4);
        popupH = Math.min(popupH, height - margin * 4);

        popupX = (width  - popupW) / 2;
        popupY = (height - popupH) / 2;

        // Grid origins
        armorX    = popupX + margin;
        mainGridX = armorX + slotSize + margin;
        mainGridY = popupY + titleH + margin;
        hotbarY   = mainGridY + 3 * (slotSize + slotPad) + hotbarGap;
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    // View-only — no slot interactions, just tooltips on hover (handled in render)

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void renderPopupScreen(DrawContext c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());

        // Icon in title bar
        if (iconItemId != null && !iconItemId.isEmpty()) {
            try {
                Item item = net.minecraft.registry.Registries.ITEM.get(
                        net.minecraft.util.Identifier.of(iconItemId));
                c.drawItem(new ItemStack(item), popupX + margin, popupY + (titleH - 16) / 2);
            } catch (Exception ignored) {}
        }

        int titleTextX = popupX + margin + (iconItemId != null && !iconItemId.isEmpty() ? 20 : 0);
        c.drawText(textRenderer,
                LegacyText.parse(displayName != null ? displayName : "Kit"),
                titleTextX,
                popupY + (titleH - textRenderer.fontHeight) / 2,
                theme.header_foreground(), false);

        // Slot count badge right side of title
        String badge = slots.size() + " / 41";
        c.drawText(textRenderer, Text.of(badge),
                popupX + popupW - margin - textRenderer.getWidth(badge),
                popupY + (titleH - textRenderer.fontHeight) / 2,
                theme.body_value(), false);

        // Body background
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        hoveredSlot = -1;
        ItemStack tooltipStack = null;

        // Armor slots (36–39, 40)
        int[] armorSlots = {39, 38, 37, 36, 40};
        for (int i = 0; i < armorSlots.length; i++) {
            int sy = mainGridY + i * (slotSize + slotPad) + (i == 4 ? slotPad * 3 : 0);
            ItemStack stack = slots.getOrDefault(armorSlots[i], ItemStack.EMPTY);
            boolean hov = isOver(mx, my, armorX, sy);
            if (hov) { hoveredSlot = armorSlots[i]; if (!stack.isEmpty()) tooltipStack = stack; }
            renderSlot(c, stack, armorX, sy, hov);
        }

        // Main inventory (9–35)
        for (int slot = 9; slot <= 35; slot++) {
            int col = (slot - 9) % 9;
            int row = (slot - 9) / 9;
            int sx  = mainGridX + col * (slotSize + slotPad);
            int sy  = mainGridY + row * (slotSize + slotPad);
            ItemStack stack = slots.getOrDefault(slot, ItemStack.EMPTY);
            boolean hov = isOver(mx, my, sx, sy);
            if (hov) { hoveredSlot = slot; if (!stack.isEmpty()) tooltipStack = stack; }
            renderSlot(c, stack, sx, sy, hov);
        }

        // Hotbar (0–8)
        for (int slot = 0; slot <= 8; slot++) {
            int sx = mainGridX + slot * (slotSize + slotPad);
            ItemStack stack = slots.getOrDefault(slot, ItemStack.EMPTY);
            boolean hov = isOver(mx, my, sx, hotbarY);
            if (hov) { hoveredSlot = slot; if (!stack.isEmpty()) tooltipStack = stack; }
            renderSlot(c, stack, sx, hotbarY, hov);
        }

        // Summary line
        int summaryY = hotbarY + slotSize + margin;
        String summary = slots.size() + " / 41 slots filled";
        c.drawCenteredTextWithShadow(textRenderer, Text.of(summary),
                popupX + popupW / 2, summaryY, theme.body_value());

        // Tooltip
        if (tooltipStack != null) {
            List<Text> tip = tooltipStack.getTooltip(
                    Item.TooltipContext.DEFAULT, null, TooltipType.Default.BASIC);
            c.drawTooltip(textRenderer, tip, mx, my);
        }

        drawPopupBorder(c, theme.accent());
    }


    private boolean isOver(int mx, int my, int sx, int sy) {
        return mx >= sx && mx < sx + slotSize && my >= sy && my < sy + slotSize;
    }

    private void renderSlot(DrawContext c, ItemStack stack, int sx, int sy, boolean hov) {
        int bg = hov
                ? (theme.accent() & 0x00FFFFFF | 0x33000000)
                : (theme.body_background() & 0x00FFFFFF | 0x55000000);
        c.fill(sx, sy, sx + slotSize, sy + slotSize, bg);
        drawBorder(c, sx, sy, slotSize, slotSize, hov ? theme.accent() : theme.body_border());

        if (stack == null || stack.isEmpty()) return;

        int offset = (slotSize - 16) / 2;
        c.drawItem(stack, sx + offset, sy + offset);
        c.drawItemInSlot(textRenderer, stack, sx + offset, sy + offset);
    }


}