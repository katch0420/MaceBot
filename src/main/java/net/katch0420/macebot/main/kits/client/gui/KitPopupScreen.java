package net.katch0420.macebot.main.kits.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.client.gui.handled.KitEditorScreen;
import net.katch0420.macebot.main.kits.client.gui.handled.KitViewScreen;
import net.katch0420.macebot.main.kits.main.KitSyncManager;
import net.katch0420.macebot.main.networking.packets.c2s.RequestKitDuplicationC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class KitPopupScreen extends Screen {

    private static KitPopupScreen instance = null;

    private static final int POP_W = 280;
    private static final int POP_H = 330; // slightly taller so status never overlaps buttons

    private final Screen parent;
    private final KitData kit;
    private final boolean isCustom;

    private int px, py;

    private TextFieldWidget nameField;
    private TextFieldWidget iconField;

    private String statusMsg   = "";
    private int    statusTimer = 0;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public KitPopupScreen(Screen parent, KitData kit) {
        super(Text.literal("Kit"));
        this.parent   = parent;
        this.kit      = kit;
        this.isCustom = kit.isCustom();

        instance = this;
    }

    @Override
    protected void init() {
        px = (mc.getWindow().getScaledWidth()  - POP_W) / 2;
        py = (mc.getWindow().getScaledHeight() - POP_H) / 2;
        initFields();
        initButtons();
    }

    // ── Layout constants (relative to py) ────────────────────────────────────
    // Title bar:  py+0  → py+22
    // Name label: py+28
    // Name field: py+38
    // Icon label: py+60
    // Icon field: py+70
    // Separator:  py+92
    // Buttons:    py+98 downward
    // Status:     py+POP_H-46   ← always above the Close button
    // Close btn:  py+POP_H-26

    private void initFields() {
        nameField = new TextFieldWidget(textRenderer, px + 10, py + 38, POP_W - 20, 16,
                Text.literal("Name"));
        nameField.setText(kit.getDisplayName().replace('§', '&')); // show & codes to user
        nameField.setMaxLength(64);
        nameField.setEditable(isCustom);
        nameField.active = isCustom;
        // Preview colour codes live while typing
        nameField.setRenderTextProvider((raw, firstChar) ->
                Text.literal(raw.replace('&', '§')).asOrderedText());
        addDrawableChild(nameField);

        iconField = new TextFieldWidget(textRenderer, px + 10, py + 70, POP_W - 20, 16,
                Text.literal("Icon (e.g. minecraft:diamond_sword)"));
        iconField.setText(kit.getIconItem() != null ? kit.getIconItem() : "");
        iconField.setMaxLength(64);
        iconField.setEditable(isCustom);
        iconField.active = isCustom;
        addDrawableChild(iconField);
    }

    private void initButtons() {
        int btnW = 120;
        int btnH = 18;
        int gap  = 6;
        int col1 = px + 10;
        int col2 = px + POP_W - 10 - btnW;
        int rowY = py + 98;

        // Row 1: View | Load
        addBtn("👁 View",     col1, rowY, btnW, btnH, this::viewKit);
        addBtn("⬇ Load Kit", col2, rowY, btnW, btnH, this::loadKit);
        rowY += btnH + gap;

        // Row 2: Duplicate | Save (custom) / nothing (builtin)
        addBtn("⧉ Duplicate", col1, rowY, btnW, btnH, this::duplicateKit);
        if (isCustom) {
            addBtn("💾 Save", col2, rowY, btnW, btnH, this::saveKit);
            rowY += btnH + gap;

            // Row 3 (custom only): Edit icon/name | Delete
            addBtn("✎ Edit", col1, rowY, btnW, btnH, this::openEditMode);
            addBtn("🗑 Delete", col2, rowY, btnW, btnH, this::deleteKit);
        }

        // Close — pinned to bottom. Status message renders above it, not over it.
        int closeY = py + POP_H - btnH - 8;
        addBtn("✕ Close", col2, closeY, btnW, btnH, this::closePop);
    }

    private void addBtn(String label, int x, int y, int w, int h, Runnable action) {
        addDrawableChild(new SimpleActionButton(x, y, w, h, label, action));
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Backdrop
        ctx.fill(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 0xAA000000);

        // Panel
        ctx.fill(px, py, px + POP_W, py + POP_H, 0xFF1E1E2E);
        ctx.drawBorder(px, py, POP_W, POP_H, 0xFF5566AA);

        // Title bar
        ctx.fill(px, py, px + POP_W, py + 22, 0xFF2A2A4A);
        ctx.fill(px, py + 21, px + POP_W, py + 22, 0xFF5566AA);

        boolean custom = kit.isCustom();
        String badge = custom ? "§aCustom" : "§9Built-in";
        // Show name with colour codes rendered
        ctx.drawText(textRenderer,
                Text.literal(kit.getDisplayName().replace('&', '§')),
                px + 8, py + 7, 0xFFFFFF, false);
        ctx.drawText(textRenderer, badge,
                px + POP_W - 8 - textRenderer.getWidth(badge.substring(2)),
                py + 7, 0xFFFFFF, false);

        // Field labels
        ctx.drawText(textRenderer, "§7Name:", px + 10, py + 28, 0xFFFFFF, false);
        if (isCustom)
            ctx.drawText(textRenderer, "§8use &a &b &c for colour",
                    px + 52, py + 28, 0xFFFFFF, false);
        else
            ctx.drawText(textRenderer, "§8read-only", px + 52, py + 28, 0xFFFFFF, false);

        ctx.drawText(textRenderer, "§7Icon:", px + 10, py + 60, 0xFFFFFF, false);

        // Separator
        ctx.fill(px + 10, py + 92, px + POP_W - 10, py + 93, 0xFF333355);

        // Status message — rendered in the gap above the Close button
        if (statusTimer > 0) {
            statusTimer--;
            int statusY = py + POP_H - 44; // well above Close button at POP_H-26
            ctx.drawText(textRenderer,
                    Text.literal("§a" + statusMsg),
                    px + (POP_W - textRenderer.getWidth(statusMsg)) / 2,
                    statusY, 0xFFFFFF, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void saveKit() {
        if (!isCustom) return;
        kit.setDisplayName(nameField.getText().replace('&', '§'));
        kit.setIconItem(iconField.getText().trim());
        KitSyncManager.syncKitDataToServer(kit);
        showStatus("Saved!");
    }

    private void loadKit() {
        assert client != null;
        client.setScreen(new KitLoadScreen(this, kit));
    }

    private void viewKit() {
        KitViewScreen.open(kit,this);
    }

    private void openEditMode() {
        KitEditorScreen.open(kit, this);
    }

    private void duplicateKit() {
        ClientPlayNetworking.send(new RequestKitDuplicationC2SPacket(kit.getId()));
    }

    public static void showStatusIfCan(String status){
        if(instance != null){
            instance.showStatus(status);
        }
    }

    private void deleteKit() {
        if (!isCustom) return;
        KitSyncManager.requestKitDelete(kit);
        closePop();
    }

    private void closePop() {
        assert client != null;
        client.setScreen(parent);
        instance = null;
    }

    private void showStatus(String msg) {
        statusMsg  = msg;
        statusTimer = 80;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()) {
            close();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    // ── SimpleActionButton ────────────────────────────────────────────────────

    static class SimpleActionButton extends net.minecraft.client.gui.widget.ClickableWidget {
        private final Runnable action;
        private final String label;

        SimpleActionButton(int x, int y, int w, int h, String label, Runnable action) {
            super(x, y, w, h, Text.literal(label));
            this.action = action;
            this.label  = label;
        }

        @Override
        public void onClick(double mouseX, double mouseY) { action.run(); }

        @Override
        protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int bg = isHovered() ? 0xFF3A3A5A : 0xFF252535;
            ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            ctx.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFF4455AA);
            MinecraftClient mc = MinecraftClient.getInstance();
            String plain = label.replaceAll("§.", "");
            int tx = getX() + (getWidth()  - mc.textRenderer.getWidth(plain)) / 2;
            int ty = getY() + (getHeight() - mc.textRenderer.fontHeight) / 2;
            ctx.drawText(mc.textRenderer, label, tx, ty, 0xFFFFFF, false);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder b) {}
    }
}