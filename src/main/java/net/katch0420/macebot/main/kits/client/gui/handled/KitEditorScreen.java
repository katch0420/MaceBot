package net.katch0420.macebot.main.kits.client.gui.handled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.ControlPanelScreen;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.client.gui.ItemBrowserWidget;
import net.katch0420.macebot.main.kits.client.gui.ItemEditorWidget;
import net.katch0420.macebot.main.kits.client.gui.KitsScreen;
import net.katch0420.macebot.main.kits.server.Kit;
import net.katch0420.macebot.main.kits.server.KitInventoryWrapper;
import net.katch0420.macebot.main.networking.packets.c2s.OpenKitEditorC2SPacket;
import net.katch0420.macebot.main.networking.packets.c2s.SaveKitC2SPacket;
import net.katch0420.macebot.main.networking.packets.c2s.UpdateCursorStackC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;

public class KitEditorScreen extends HandledScreen<KitEditorScreen.KitEditorScreenHandler> {

    public static Screen parent;
    public static ScreenHandlerType<KitEditorScreenHandler> KIT_EDITOR_SCREEN_HANDLER;

    private ItemBrowserWidget browser;
    private ItemEditorWidget itemEditor;

    // Layout constants
    private static final int BROWSER_MARGIN = 9;
    private static final int EDITOR_MARGIN  = 9;

    // ── Title bar ─────────────────────────────────────────────────────────────
    private static final int TITLE_H = 24;

    // ── Bottom bar (Save + Close buttons) ────────────────────────────────────
    private static final int BOTTOM_H  = 28;
    private static final int BTN_W     = 70;
    private static final int BTN_H     = 16;

    // ── Colours — same palette as KitPopupScreen / KitViewScreen ─────────────
    private static final int COL_BG           = 0xFF111118; // full-screen background
    private static final int COL_TITLE_BAR    = 0xFF1E1E2E; // title bar fill
    private static final int COL_TITLE_INNER  = 0xFF2A2A4A; // title bar inner highlight
    private static final int COL_TITLE_ACCENT = 0xFF5566AA; // title bar bottom line / border
    private static final int COL_SEPARATOR    = 0xFF333355; // divider lines
    private static final int COL_BTN_NORMAL   = 0xFF252535; // idle button background
    private static final int COL_BTN_HOVER    = 0xFF3A3A5A; // hovered button background
    private static final int COL_BTN_SAVE_N   = 0xFF1A3A1A; // save button idle (dark green tint)
    private static final int COL_BTN_SAVE_H   = 0xFF2A5A2A; // save button hover (brighter green)
    private static final int COL_BTN_BORDER   = 0xFF4455AA; // button border (popup btn colour)
    private static final int COL_BTN_SAVE_BRD = 0xFF44AA44; // save button border (green accent)
    private static final int COL_TEXT         = 0xFFFFFFFF;
    private static final int COL_HINT         = 0xFF999999;

    // ── Save-feedback flash ───────────────────────────────────────────────────
    private String statusMsg   = "";
    private int    statusTimer = 0;

    // ── Kit reference (needed for save) ──────────────────────────────────────
    private static KitData currentKit = new KitData("","","", true,0);

    private static final Identifier KIT_EDITOR_BACKGROUND_TEXTURE =
            Identifier.of("macebot", "textures/gui/kit_editor/background/kit_inventory_background.png");

    public KitEditorScreen(KitEditorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = 27 + 11 * 18;
        this.backgroundHeight = 27 + 4  * 18;
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        initItemBrowser();
        initItemEditor();
    }

    private void initItemBrowser() {
        browser = new ItemBrowserWidget(0, 0, this::setCursorStack);
        browser.resize(this.width, this.height - BOTTOM_H - TITLE_H, this.backgroundWidth, BROWSER_MARGIN);
        addDrawableChild(browser);
    }

    private void initItemEditor() {
        itemEditor = new ItemEditorWidget(
                (width + backgroundWidth) / 2 + EDITOR_MARGIN,
                (width - backgroundWidth) / 2 - 2 * EDITOR_MARGIN,
                height,
                backgroundHeight
        );
        addDrawableChild(itemEditor);
    }

    // ── Resize ────────────────────────────────────────────────────────────────

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        browser.resize(this.width, this.height - BOTTOM_H - TITLE_H, this.backgroundWidth, BROWSER_MARGIN);
        itemEditor.resize(
                (this.width + backgroundWidth) / 2 + EDITOR_MARGIN,
                (this.width - backgroundWidth) / 2 - 2 * EDITOR_MARGIN,
                height
        );
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // If a sub-editor popup is open, only render that
        if (itemEditor.getCurrentMode() != ItemEditorWidget.Mode.NORMAL) {
            itemEditor.render(ctx, mouseX, mouseY, delta);
            return;
        }

        // Full-screen background
        ctx.fill(0, 0, width, height, COL_BG);

        super.render(ctx, mouseX, mouseY, delta);

        renderTitleBar(ctx);
        renderBottomBar(ctx, mouseX, mouseY);

        // Inventory grid (via HandledScreen)


        highlightEditedSlot(ctx);
        this.drawMouseoverTooltip(ctx, mouseX, mouseY);

        renderStatus(ctx);
    }

    private void renderTitleBar(DrawContext ctx) {
        ctx.fill(0, 0, width, TITLE_H, COL_TITLE_BAR);
        ctx.fill(0, 0, width, TITLE_H - 3, COL_TITLE_INNER);
        ctx.fill(0, TITLE_H - 1, width, TITLE_H, COL_TITLE_ACCENT);

        int ty = (TITLE_H - textRenderer.fontHeight) / 2;

        // Left: kit name (with § colour codes)
        String kitName = currentKit != null ? currentKit.getDisplayName() : title.getString();
        Text leftText = Text.literal("§fEditing: ").append(Text.literal(kitName));
        ctx.drawText(textRenderer, leftText, 8, ty, COL_TEXT, false);

        // Right: "Kit Editor" label
        String rightLabel = "Kit Editor";
        ctx.drawText(textRenderer, "§9" + rightLabel,
                width - 8 - textRenderer.getWidth(rightLabel), ty, COL_TEXT, false);
    }

    private void renderBottomBar(DrawContext ctx, int mx, int my) {
        // Background strip
        ctx.fill(0, height - BOTTOM_H, width, height, COL_TITLE_BAR);
        ctx.fill(0, height - BOTTOM_H, width, height - BOTTOM_H + 1, COL_TITLE_ACCENT);

        // ── Save button (left side of bottom bar) ────────────────────────────
        int saveX = 8;
        int btnY  = height - BOTTOM_H + (BOTTOM_H - BTN_H) / 2;
        boolean saveHov = mx >= saveX && mx <= saveX + BTN_W && my >= btnY && my <= btnY + BTN_H;
        ctx.fill(saveX, btnY, saveX + BTN_W, btnY + BTN_H,
                saveHov ? COL_BTN_SAVE_H : COL_BTN_SAVE_N);
        ctx.drawBorder(saveX, btnY, BTN_W, BTN_H, COL_BTN_SAVE_BRD);
        ctx.drawText(textRenderer, "💾 Save",
                saveX + (BTN_W - textRenderer.getWidth("Save")) / 2 - 5,
                btnY + (BTN_H - textRenderer.fontHeight) / 2,
                COL_TEXT, false);

        // ── Close button (right side of bottom bar) ───────────────────────────
        int closeX = width - BTN_W - 8;
        boolean closeHov = mx >= closeX && mx <= closeX + BTN_W && my >= btnY && my <= btnY + BTN_H;
        ctx.fill(closeX, btnY, closeX + BTN_W, btnY + BTN_H,
                closeHov ? COL_BTN_HOVER : COL_BTN_NORMAL);
        ctx.drawBorder(closeX, btnY, BTN_W, BTN_H, COL_BTN_BORDER);
        ctx.drawText(textRenderer, "✕ Close",
                closeX + (BTN_W - textRenderer.getWidth("Close")) / 2 - 5,
                btnY + (BTN_H - textRenderer.fontHeight) / 2,
                COL_TEXT, false);

        // ── Hint text (center) ────────────────────────────────────────────────
        String hint = "Ctrl+S to save";
        ctx.drawText(textRenderer, hint,
                (width - textRenderer.getWidth(hint)) / 2,
                btnY + (BTN_H - textRenderer.fontHeight) / 2,
                COL_HINT, false);
    }

    /** Temporary save-feedback message rendered just above the bottom bar. */
    private void renderStatus(DrawContext ctx) {
        if (statusTimer <= 0) return;
        statusTimer--;
        String msg = statusMsg;
        ctx.drawText(textRenderer, "§a" + msg,
                (width - textRenderer.getWidth(msg)) / 2,
                height - BOTTOM_H - textRenderer.fontHeight - 4,
                COL_TEXT, false);
    }

    private void highlightEditedSlot(DrawContext ctx) {
        int editedSlot = itemEditor.getSelectedSlot();
        if (editedSlot < 0 || editedSlot > 40) return;

        for (Slot slot : this.handler.slots) {
            if (slot.getIndex() == editedSlot && slot instanceof KitEditorScreenHandler.KitSlot) {
                int sx = this.x + slot.x;
                int sy = this.y + slot.y;
                ctx.drawBorder(sx - 1, sy - 1, 18, 18, 0xFF00FF00);
                ctx.drawBorder(sx - 2, sy - 2, 20, 20, 0x8000FF00);
                break;
            }
        }
    }

    // ── Background (inventory grid texture) ───────────────────────────────────

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.drawTexture(RenderLayer::getGuiTextured, KIT_EDITOR_BACKGROUND_TEXTURE,
                this.x - 1, this.y - 1, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        // Intentionally empty — title drawn by renderTitleBar()
    }
    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // ── Bottom bar buttons ────────────────────────────────────────────────
        int btnY   = height - BOTTOM_H + (BOTTOM_H - BTN_H) / 2;
        int saveX  = 8;
        int closeX = width - BTN_W - 8;

        if (my >= btnY && my <= btnY + BTN_H) {
            if (mx >= saveX && mx <= saveX + BTN_W) {
                saveKit();
                return true;
            }
            if (mx >= closeX && mx <= closeX + BTN_W) {
                close();
                return true;
            }
        }

        // ── Slot edit clicks ─────────────────────────────────────────────────
        boolean isMiddleClick = button == 2;
        boolean isCtrlClick   = button == 0 && Screen.hasControlDown();

        if (isMiddleClick || isCtrlClick) {
            Slot slot = getSlotAt(mx, my);
            if (slot instanceof KitEditorScreenHandler.KitSlot kitSlot) {
                int idx = kitSlot.getIndex();
                if (idx >= 0 && idx <= 40) {
                    itemEditor.setSelectedStack(slot.getStack(), idx);
                    return true;
                }
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        for (Element e : children()) {
            if (e.mouseReleased(mx, my, button)) return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (Element e : children()) e.mouseDragged(mx, my, button, dx, dy);
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        for (Element e : children()) {
            if (e.mouseScrolled(mx, my, hAmount, vAmount)) return true;
        }
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()) {
            close();
        }
        // Escape → close
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        // Ctrl+S → save
        if (keyCode == GLFW.GLFW_KEY_S && Screen.hasControlDown()) {
            saveKit();
            return true;
        }

        if (itemEditor.getCurrentMode() != ItemEditorWidget.Mode.NORMAL) {
            itemEditor.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        for (Element e : children()) {
            if (e.keyPressed(keyCode, scanCode, modifiers)) return true;
        }

        return true; // swallow everything else
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (itemEditor.getCurrentMode() != ItemEditorWidget.Mode.NORMAL) {
            return itemEditor.charTyped(chr, modifiers);
        }
        for (Element e : children()) {
            if (e.charTyped(chr, modifiers)) return true;
        }
        return true;
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot == null && slotId == -999
                && (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE)
                && (button == 0 || button == 1)) {
            setCursorStack(ItemStack.EMPTY);
        }
        super.onMouseClick(slot, slotId, button, actionType);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Sends the current kit contents to the server to be persisted.
     * Uses SaveKitC2SPacket — wire this up the same way as your other C2S packets.
     */
    private void saveKit() {
        if (currentKit == null) return;
        // Sync the last-edited slot first (in case user didn't click away)
        itemEditor.syncState();
        // Tell the server to save
        ClientPlayNetworking.send(new SaveKitC2SPacket());
        showStatus("Saved!");
    }

    public static void open(KitData kit, Screen parent) {
        currentKit = kit;
        KitEditorScreen.parent = parent;
        ClientPlayNetworking.send(new OpenKitEditorC2SPacket(kit.getId()));
    }

    private void setCursorStack(ItemStack stack) {
        UpdateCursorStackC2SPacket.updateCursorStack(stack);
    }

    private void showStatus(String msg) {
        statusMsg   = msg;
        statusTimer = 80; // ~4 seconds at 20 tps
    }

    private Slot getSlotAt(double mx, double my) {
        for (Slot slot : this.handler.slots) {
            int sx = this.x + slot.x;
            int sy = this.y + slot.y;
            if (mx >= sx && mx < sx + 16 && my >= sy && my < sy + 16) return slot;
        }
        return null;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    // ── Screen Handler ────────────────────────────────────────────────────────

    public static class KitEditorScreenHandler extends ScreenHandler {

        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final KitInventoryWrapper INVENTORY;

        public KitEditorScreenHandler(int syncId, PlayerEntity player, Kit kit) {
            super(KIT_EDITOR_SCREEN_HANDLER, syncId);
            INVENTORY = new KitInventoryWrapper(kit, player);
            addSlots();
        }

        public void addSlots() {
            // Armor slots (39 = head → 36 = boots)
            for (int i = 0; i < 2; i++) addSlot(new KitSlot(INVENTORY, 39 - i,  9, 18 + i * 18));
            for (int i = 0; i < 2; i++) addSlot(new KitSlot(INVENTORY, 37 - i, 27, 18 + i * 18));

            // Offhand (40)
            addSlot(new KitSlot(INVENTORY, 40, 18, 9 + 3 * 18));

            // Main inventory (9-35)
            int idx = 9;
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 9; c++)
                    addSlot(new KitSlot(INVENTORY, idx++, 54 + c * 18, 9 + r * 18));

            // Hotbar (0-8)
            idx = 0;
            for (int c = 0; c < 9; c++)
                addSlot(new KitSlot(INVENTORY, idx++, 54 + c * 18, 18 + 3 * 18));
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {
            this.slots.get(slot).setStack(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }


        @Override
        public boolean canUse(PlayerEntity player) { return true; }

        public static void register() {
            KIT_EDITOR_SCREEN_HANDLER = Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of("macebot", "kit_editor"),
                    new ScreenHandlerType<>(KitEditorScreenHandler::create, FeatureFlags.VANILLA_FEATURES)
            );
        }

        public KitInventoryWrapper getInventoryWrapper() { return INVENTORY; }

        private static KitEditorScreenHandler create(int syncId, PlayerInventory playerInventory) {
            Kit kit = new Kit("temp", "Temporary Kit", "diamond_sword", true);
            return new KitEditorScreenHandler(syncId, playerInventory.player, kit);
        }

        public void saveKit() {
            INVENTORY.save();
        }

        public static class KitSlot extends Slot {
            public KitSlot(Inventory inventory, int index, int x, int y) {
                super(inventory, index, x, y);
            }
        }
    }
}