package net.katch0420.macebot.main.kits.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.networking.packets.c2s.KitLoadRequestC2SPacket;
import net.katch0420.macebot.main.networking.packets.c2s.KitLoadRequestC2SPacket.Target;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class KitLoadScreen extends Screen {

    private static final int POP_W = 300;
    private static final int POP_H = 290;

    private final Screen parent;
    private final KitData kit;

    private int px, py;

    // ── Options ───────────────────────────────────────────────────────────────
    private Target target = Target.MACEBOT;
    private boolean unbreaking  = false;
    private boolean mending     = false;
    private boolean unbreakable = false;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public KitLoadScreen(Screen parent, KitData kit) {
        super(Text.literal("Load Kit"));
        this.parent = parent;
        this.kit    = kit;
    }

    @Override
    protected void init() {
        px = (mc.getWindow().getScaledWidth()  - POP_W) / 2;
        py = (mc.getWindow().getScaledHeight() - POP_H) / 2;
        initButtons();
    }

    private void initButtons() {
        int btnW = 120, btnH = 18, gap = 6;
        int col1 = px + 10;
        int col2 = px + POP_W - 10 - btnW;
        int closeY = py + POP_H - btnH - 8;

        // Load confirm
        addBtn("✔ Load", col1, closeY, btnW, btnH, this::confirmLoad);
        // Cancel
        addBtn("✕ Cancel", col2, closeY, btnW, btnH, this::cancel);
    }

    private void addBtn(String label, int x, int y, int w, int h, Runnable action) {
        addDrawableChild(new KitPopupScreen.SimpleActionButton(x, y, w, h, label, action));
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
        ctx.drawText(textRenderer, "Load Kit: §b" + kit.getDisplayName().replace('&', '§'),
                px + 8, py + 7, 0xFFFFFF, false);

        int y = py + 30;

        // ── Target ────────────────────────────────────────────────────────────
        ctx.drawText(textRenderer, "§7Load to:", px + 10, y, 0xFFFFFF, false);
        y += 14;

        int btnW = 82, btnH = 16, gap = 4;
        int x = px + 10;
        renderToggleBtn(ctx, mouseX, mouseY, "MaceBot",     x,           y, btnW, btnH, target == Target.MACEBOT);
        renderToggleBtn(ctx, mouseX, mouseY, "Myself",      x + btnW + gap, y, btnW, btnH, target == Target.MYSELF);
        renderToggleBtn(ctx, mouseX, mouseY, "All Players", x + (btnW + gap) * 2, y, btnW, btnH, target == Target.ALL_PLAYERS);
        y += btnH + 12;

        // ── Separator ─────────────────────────────────────────────────────────
        ctx.fill(px + 10, y, px + POP_W - 10, y + 1, 0xFF333355);
        y += 8;

        // ── Boolean toggles ───────────────────────────────────────────────────
        ctx.drawText(textRenderer, "§7Modifiers:", px + 10, y, 0xFFFFFF, false);
        y += 14;

        renderCheckbox(ctx, mouseX, mouseY, "Unbreaking III",  px + 10, y, unbreaking);
        y += 20;
        renderCheckbox(ctx, mouseX, mouseY, "Mending",         px + 10, y, mending);
        y += 20;
        renderCheckbox(ctx, mouseX, mouseY, "Unbreakable Tag", px + 10, y, unbreakable);
        y += 24;

        // ── Info note ─────────────────────────────────────────────────────────
        ctx.fill(px + 10, y, px + POP_W - 10, y + 1, 0xFF333355);
        y += 8;
        ctx.drawText(textRenderer, "§8Modifiers apply to all items in the kit.",
                px + 10, y, 0xFFFFFF, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderToggleBtn(DrawContext ctx, int mx, int my,
                                 String label, int x, int y, int w, int h, boolean active) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;
        int bg;
        if      (active) bg = 0xFF3A6BC4;
        else if (hov)    bg = 0xFF2A3A4A;
        else             bg = 0xFF252535;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.drawBorder(x, y, w, h, active ? 0xFF6688FF : 0xFF444466);
        ctx.drawText(textRenderer, label,
                x + (w - textRenderer.getWidth(label)) / 2,
                y + (h - textRenderer.fontHeight) / 2, 0xFFFFFF, false);
    }

    private void renderCheckbox(DrawContext ctx, int mx, int my,
                                String label, int x, int y, boolean checked) {
        int boxSize = 11;
        boolean hov = mx >= x && mx <= x + boxSize && my >= y && my <= y + boxSize;
        ctx.fill(x, y, x + boxSize, y + boxSize, hov ? 0xFF2A3A5A : 0xFF1E1E2E);
        ctx.drawBorder(x, y, boxSize, boxSize, checked ? 0xFF6688FF : 0xFF444466);
        if (checked) {
            // Draw a simple tick as two filled rectangles
            ctx.fill(x + 2, y + 5, x + 5,  y + 8,  0xFF88AAFF);
            ctx.fill(x + 4, y + 3, x + 10, y + 6,  0xFF88AAFF);
        }
        ctx.drawText(textRenderer, label, x + boxSize + 5,
                y + (boxSize - textRenderer.fontHeight) / 2, 0xCCCCCC, false);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int imx = (int) mx, imy = (int) my;

        // Target row
        int tY  = py + 30 + 14;
        int btnW = 82, btnH = 16, gap = 4;
        int tX  = px + 10;
        if (imy >= tY && imy <= tY + btnH) {
            if (imx >= tX && imx <= tX + btnW)                       { target = Target.MACEBOT;     return true; }
            if (imx >= tX + btnW + gap && imx <= tX + btnW * 2 + gap){ target = Target.MYSELF;      return true; }
            if (imx >= tX + (btnW + gap) * 2)                        { target = Target.ALL_PLAYERS; return true; }
        }

        // Checkboxes — y positions mirror render
        int boxSize = 11;
        int cY = py + 30 + 14 + btnH + 12 + 1 + 8 + 14;
        if (imx >= px + 10 && imx <= px + 10 + boxSize) {
            if (imy >= cY && imy <= cY + boxSize)        { unbreaking  = !unbreaking;  return true; }
            if (imy >= cY + 20 && imy <= cY + 31)        { mending     = !mending;     return true; }
            if (imy >= cY + 40 && imy <= cY + 51)        { unbreakable = !unbreakable; return true; }
        }

        return super.mouseClicked(mx, my, button);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void confirmLoad() {
        ClientPlayNetworking.send(new KitLoadRequestC2SPacket(target, kit.getId(), unbreaking, mending, unbreakable));
        cancel();
    }

    private void cancel() {
        client.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()) {
            close();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}