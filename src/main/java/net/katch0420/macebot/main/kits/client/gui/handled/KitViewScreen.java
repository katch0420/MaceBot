package net.katch0420.macebot.main.kits.client.gui.handled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.server.Kit;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.networking.packets.c2s.OpenKitEditorC2SPacket;
import net.katch0420.macebot.main.networking.packets.c2s.OpenKitViewerC2SPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class KitViewScreen extends HandledScreen<KitViewScreen.KitViewScreenHandler> {

    public static ScreenHandlerType<KitViewScreenHandler> KIT_VIEW_SCREEN_HANDLER;
    private static Screen parent;

    private int hoveredSlot = -1;

    /** The kit's actual display name (with § colour codes), passed from the server packet. */
    private final String kitDisplayName;
    /** Whether this kit is custom — drives the badge in the title bar. */
    private final boolean isCustomKit;

    // ── Fixed UI constants ────────────────────────────────────────────────────
    private static final int TITLE_H       = 24;
    private static final int BOTTOM_BAR_H  = 28;
    private static final int LABEL_H       = 10;
    private static final int ARMOR_LABEL_W = 30;
    private static final int COL_GAP       = 12;

    // ── Colours — mirrors KitPopupScreen exactly ──────────────────────────────
    private static final int COL_BG           = 0xFF111118; // full-screen background
    private static final int COL_TITLE_BAR    = 0xFF1E1E2E; // title bar fill  (same as popup panel)
    private static final int COL_TITLE_ACCENT = 0xFF5566AA; // bottom line of title bar
    private static final int COL_TITLE_INNER  = 0xFF2A2A4A; // inner title highlight (popup uses this)
    private static final int COL_PANEL_BORDER = 0xFF5566AA; // border accent
    private static final int COL_SLOT_NORMAL  = 0xFF252530; // idle slot bg
    private static final int COL_SLOT_HOVER   = 0xFF3A3A5A; // hovered slot bg  (popup btn hover)
    private static final int COL_SLOT_BORDER  = 0xFF333345; // idle slot border
    private static final int COL_SLOT_BORD_HV = 0xFF4455AA; // hovered slot border (popup btn border)
    private static final int COL_SEPARATOR    = 0xFF333355; // divider lines
    private static final int COL_LABEL        = 0xFF888888; // section labels
    private static final int COL_ARMOR_LABEL  = 0xFFAAAAAA; // armor row labels
    private static final int COL_SUMMARY      = 0xFF666666; // footer summary text
    private static final int COL_BTN_NORMAL   = 0xFF252535; // back-button idle
    private static final int COL_BTN_HOVER    = 0xFF3A3A5A; // back-button hover

    /**
     * @param kitDisplayName the kit's §-coded display name (e.g. "§6Warrior Kit")
     * @param isCustomKit    true if this is a custom kit (shows "Custom" badge, else "Built-in")
     */

    /** Convenience constructor when extra meta isn't yet available on the client side. */
    public KitViewScreen(KitViewScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.kitDisplayName = title.getString();
        this.isCustomKit    = false;
        this.backgroundWidth  = 0;
        this.backgroundHeight = 0;
    }

    @Override
    protected void init() { super.init(); }

    // ── Layout helpers — everything derives from slotSize() and centerX() ─────

    /** Slot size, clamped so the whole layout fits the window. */
    private int slotSize() {
        // Available width: window minus armor-label col, gap, and some outer padding
        int availW = width  - ARMOR_LABEL_W - COL_GAP - 32;
        // Available height: window minus title, bottom bar, two label rows, hotbar gap
        int availH = height - TITLE_H - BOTTOM_BAR_H - LABEL_H * 2 - 8;

        int maxByW = availW / 10;          // 1 armor col + 9 main cols
        int maxByH = availH / 6;           // 5 armor rows OR 4 inv+hotbar rows (use the taller)
        return Math.max(10, Math.min(20, Math.min(maxByW, maxByH)));
    }

    /** Padding between slots, proportional to slot size. */
    private int pad() { return Math.max(2, slotSize() / 5); }

    // Total pixel width of the whole block we want to center
    private int totalBlockW() {
        int s = slotSize(), p = pad();
        int armorColW = ARMOR_LABEL_W + s;                  // label text + one slot
        int mainGridW = 9 * (s + p) - p;                    // 9 slots with inter-slot padding
        return armorColW + COL_GAP + mainGridW;
    }

    /** X origin of the entire centered block. */
    private int blockX() { return (width - totalBlockW()) / 2; }

    /** X where the armor slots start (after the label text area). */
    private int armorSlotX() { return blockX() + ARMOR_LABEL_W; }

    /** X where the main 9-wide grid starts. */
    private int mainGridX()  { return armorSlotX() + slotSize() + COL_GAP; }

    /** Total available vertical space for slots (between title bar, labels, and bottom bar). */
    private int availSlotH() {
        return height - TITLE_H - BOTTOM_BAR_H - LABEL_H * 2;
    }

    /**
     * Y where the slot rows start.
     * We need 5 armor rows OR 4 grid rows (3 inv + 1 hotbar) — whichever is taller.
     */
    private int topY() {
        int s = slotSize(), p = pad();
        int armorH    = 5 * (s + p) - p;
        int invH      = 3 * (s + p) - p;
        int hotbarGap = p * 3;
        int gridH     = invH + hotbarGap + s;   // 3 inv rows + gap + 1 hotbar row
        int totalH    = Math.max(armorH, gridH);
        // Center this block in the available slot area, shifted down by title + label row
        return TITLE_H + LABEL_H + (availSlotH() - totalH) / 2;
    }

    private int invRowsY()  { return topY(); }
    private int hotbarY()   {
        int s = slotSize(), p = pad();
        return invRowsY() + 3 * (s + p) - p + p * 3;   // after 3 rows + a visible gap
    }
    private int armorTopY() { return topY(); }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, COL_BG);
        renderTitleBar(ctx);
        renderInventory(ctx, mouseX, mouseY);
        renderSummary(ctx);
        renderBackBtn(ctx, mouseX, mouseY);
        // No super.render() — we don't want vanilla container chrome
    }

    private void renderTitleBar(DrawContext ctx) {
        // Outer bar — same dark navy as popup panel bg
        ctx.fill(0, 0, width, TITLE_H, COL_TITLE_BAR);
        // Inner highlight strip — same as popup title bar inner fill
        ctx.fill(0, 0, width, TITLE_H - 3, COL_TITLE_INNER);
        // Bottom accent line — same purple-blue as popup border
        ctx.fill(0, TITLE_H - 1, width, TITLE_H, COL_TITLE_ACCENT);

        int ty = (TITLE_H - textRenderer.fontHeight) / 2;

        // Left: "Viewing: <kit name with colour codes rendered>"
        Text nameText = Text.literal("§fViewing: ").append(Text.literal(kitDisplayName));
        ctx.drawText(textRenderer, nameText, 8, ty, 0xFFFFFF, false);

        // Right badge — "Custom" (green) or "Built-in" (blue), same as KitPopupScreen
        String badge      = isCustomKit ? "§aCustom" : "§9Built-in";
        String badgePlain = isCustomKit ? "Custom"   : "Built-in";
        ctx.drawText(textRenderer, badge,
                width - 8 - textRenderer.getWidth(badgePlain), ty, 0xFFFFFF, false);
    }

    private void renderInventory(DrawContext ctx, int mouseX, int mouseY) {
        int s  = slotSize(), p = pad();
        int aX = armorSlotX();
        int mX = mainGridX();
        int aY = armorTopY();
        int mY = invRowsY();
        int hY = hotbarY();

        hoveredSlot = -1;
        ItemStack tooltipStack = ItemStack.EMPTY;

        // ── Section labels ────────────────────────────────────────────────────
        // Labels sit one LABEL_H above the slot rows
        int labelY = topY() - LABEL_H;
        // "Armor" label centered over the armor slot column
        int armorLabelX = aX + (s - textRenderer.getWidth("Armor")) / 2;
        ctx.drawText(textRenderer, "§7Armor",     armorLabelX, labelY, COL_LABEL, false);
        ctx.drawText(textRenderer, "§7Inventory", mX,          labelY, COL_LABEL, false);

        // ── Main inventory rows (slots 9-35, i.e. rows 1-3 of player inv) ────
        for (int slot = 9; slot <= 35; slot++) {
            int col = (slot - 9) % 9;
            int row = (slot - 9) / 9;
            int sx  = mX + col * (s + p);
            int sy  = mY + row * (s + p);
            boolean hov = hovered(mouseX, mouseY, sx, sy, s);
            if (hov) hoveredSlot = slot;
            ItemStack stack = getStack(slot);
            renderSlot(ctx, stack, sx, sy, s, hov);
            if (hov && !stack.isEmpty()) tooltipStack = stack;
        }

        // ── Hotbar separator + label ──────────────────────────────────────────
        int sepY = hY - (p * 3) / 2;   // halfway through the gap
        ctx.fill(mX, sepY, mX + 9 * (s + p) - p, sepY + 1, COL_SEPARATOR);
        String hbLabel = "§7Hotbar";
        ctx.drawText(textRenderer, hbLabel, mX + (9 * (s + p) - p - textRenderer.getWidth("Hotbar")) / 2,
                sepY + 2, COL_SUMMARY, false);

        // ── Hotbar (slots 0-8) ────────────────────────────────────────────────
        for (int slot = 0; slot <= 8; slot++) {
            int sx = mX + slot * (s + p);
            boolean hov = hovered(mouseX, mouseY, sx, hY, s);
            if (hov) hoveredSlot = slot;
            ItemStack stack = getStack(slot);
            renderSlot(ctx, stack, sx, hY, s, hov);
            if (hov && !stack.isEmpty()) tooltipStack = stack;
        }

        // ── Armor + offhand (slots 36-40) ─────────────────────────────────────
        int[] armorSlots     = { 39, 38, 37, 36, 40 };
        String[] armorLabels = { "Head", "Chest", "Legs", "Feet", "Off" };
        for (int i = 0; i < armorSlots.length; i++) {
            int slot = armorSlots[i];
            int sy   = aY + i * (s + p);
            boolean hov = hovered(mouseX, mouseY, aX, sy, s);
            if (hov) hoveredSlot = slot;
            ItemStack stack = getStack(slot);
            renderSlot(ctx, stack, aX, sy, s, hov);

            // Label is right-aligned inside the ARMOR_LABEL_W column, vertically centered
            String lbl = armorLabels[i];
            int lx = blockX() + ARMOR_LABEL_W - textRenderer.getWidth(lbl) - 3;
            int ly = sy + (s - textRenderer.fontHeight) / 2;
            ctx.drawText(textRenderer, "§8" + lbl, lx, ly, COL_ARMOR_LABEL, false);

            // Divider between feet and offhand
            if (i == 3) {
                int divY = sy + s + (p * 3) / 2;
                ctx.fill(blockX(), divY, aX + s, divY + 1, COL_SEPARATOR);
            }
            if (hov && !stack.isEmpty()) tooltipStack = stack;
        }

        // ── Tooltip ───────────────────────────────────────────────────────────
        if (hoveredSlot >= 0 && !tooltipStack.isEmpty()) {
            List<Text> lines = tooltipStack.getTooltip(
                    net.minecraft.item.Item.TooltipContext.DEFAULT,
                    null,
                    net.minecraft.item.tooltip.TooltipType.Default.BASIC);
            ctx.drawTooltip(textRenderer, lines, mouseX, mouseY);
        }
    }

    private ItemStack getStack(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return ItemStack.EMPTY;
        return handler.getSlot(slotIndex).getStack();
    }

    private void renderSlot(DrawContext ctx, ItemStack stack, int x, int y, int s, boolean hov) {
        ctx.fill(x, y, x + s, y + s, hov ? COL_SLOT_HOVER : COL_SLOT_NORMAL);
        ctx.drawBorder(x, y, s, s, hov ? COL_SLOT_BORD_HV : COL_SLOT_BORDER);

        if (stack == null || stack.isEmpty()) return;

        if (s < 18) {
            float scale = (s - 2) / 16.0f;
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x + 1, y + 1, 0);
            ctx.getMatrices().scale(scale, scale, 1f);
            ctx.drawItem(stack, 0, 0);
            ctx.drawStackOverlay(textRenderer, stack, 0, 0);
            ctx.getMatrices().pop();
        } else {
            ctx.drawItem(stack, x + 2, y + 2);
            ctx.drawStackOverlay(textRenderer, stack, x + 2, y + 2);
        }
    }

    private void renderSummary(DrawContext ctx) {
        long filled = handler.slots.stream()
                .limit(41)
                .filter(s -> !s.getStack().isEmpty())
                .count();
        String summary = filled + " / 41 slots filled";
        ctx.drawText(textRenderer, summary,
                (width - textRenderer.getWidth(summary)) / 2,
                height - BOTTOM_BAR_H + 6, COL_SUMMARY, false);
    }

    private void renderBackBtn(DrawContext ctx, int mx, int my) {
        int w = 60, h = 16;
        int x = width  - w - 8;
        int y = height - h - 6;
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;
        ctx.fill(x, y, x + w, y + h, hov ? COL_BTN_HOVER : COL_BTN_NORMAL);
        ctx.drawBorder(x, y, w, h, COL_PANEL_BORDER);
        ctx.drawText(textRenderer, "← Back",
                x + (w - textRenderer.getWidth("← Back")) / 2,
                y + (h - textRenderer.fontHeight) / 2, 0xFFFFFF, false);
    }

    private boolean hovered(int mx, int my, int x, int y, int s) {
        return mx >= x && mx < x + s && my >= y && my < y + s;
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int w = 60, h = 16;
        int x = width  - w - 8;
        int y = height - h - 6;
        if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
            this.close();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()) {
            close();
        }
        return false;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {}

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    public static void open(KitData kit, Screen parent) {
        KitViewScreen.parent = parent;
        ClientPlayNetworking.send(new OpenKitViewerC2SPacket(kit.getId()));
    }

    // ── Screen Handler ────────────────────────────────────────────────────────

    public static class KitViewScreenHandler extends ScreenHandler {

        private static final int SLOT_COUNT = 41;
        private final SimpleInventory kitInventory = new SimpleInventory(SLOT_COUNT);

        public KitViewScreenHandler(int syncId, PlayerInventory playerInv, String kitId) {
            super(KIT_VIEW_SCREEN_HANDLER, syncId);
            Kit kit = KitRegistry.get(kitId);
            if (kit != null) {
                kit.getItems().forEach((slot, ks) -> {
                    if (slot >= 0 && slot < SLOT_COUNT) {
                        ItemStack stack = ks.toStack(playerInv.player);
                        kitInventory.setStack(slot, stack);
                    }
                });
            }
            for (int i = 0; i < SLOT_COUNT; i++) {
                addSlot(new ReadOnlySlot(kitInventory, i, 0, 0));
            }
        }

        public KitViewScreenHandler(int syncId, PlayerInventory playerInv) {
            super(KIT_VIEW_SCREEN_HANDLER, syncId);
            for (int i = 0; i < SLOT_COUNT; i++) {
                addSlot(new ReadOnlySlot(kitInventory, i, 0, 0));
            }
        }

        public static void register() {
            KIT_VIEW_SCREEN_HANDLER = Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of("macebot", "kit_viewer"),
                    new ScreenHandlerType<>(
                            KitViewScreenHandler::create,
                            FeatureFlags.VANILLA_FEATURES
                    )
            );
        }

        private static KitViewScreen.KitViewScreenHandler create(int syncId, PlayerInventory playerInventory) {
            return new KitViewScreenHandler(syncId, playerInventory);
        }

        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {}

        @Override
        public boolean canUse(PlayerEntity player) { return true; }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }

        private static class ReadOnlySlot extends Slot {
            ReadOnlySlot(SimpleInventory inv, int index, int x, int y) {
                super(inv, index, x, y);
            }
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public boolean canTakeItems(PlayerEntity player) { return false; }
        }
    }
}