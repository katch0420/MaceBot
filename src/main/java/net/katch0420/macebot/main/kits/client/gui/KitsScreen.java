package net.katch0420.macebot.main.kits.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.ControlPanelScreen;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.client.gui.handled.KitViewScreen;
import net.katch0420.macebot.main.networking.packets.c2s.OpenKitViewerC2SPacket;
import net.katch0420.macebot.main.networking.packets.c2s.CreateNewKitC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class KitsScreen extends Screen {

    Screen parent;

    private static final int TOOLBAR_H = 32;
    private static final int SIDEBAR_W = 140;
    private static final int ROW_H     = 28;
    private static final int HEADER_H  = 16;

    private enum Section { ALL, BUILTIN, CUSTOM }
    private Section activeSection = Section.ALL;

    private KitData selectedKit  = null;
    private int scrollOffset = 0;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public KitsScreen(Screen parent) { super(Text.literal("Kits")); this.parent = parent; }

    @Override
    protected void init() { scrollOffset = 0; }

    // ── Geometry ──────────────────────────────────────────────────────────────

    private int winW()         { return mc.getWindow().getScaledWidth(); }
    private int winH()         { return mc.getWindow().getScaledHeight(); }
    private int sidebarLeft()  { return 0; }
    private int sidebarRight() { return SIDEBAR_W; }
    private int sidebarTop()   { return TOOLBAR_H; }
    private int contentLeft()  { return SIDEBAR_W; }
    private int contentRight() { return winW(); }
    private int contentTop()   { return TOOLBAR_H; }
    private int listTop()      { return contentTop() + HEADER_H; }
    private int listViewH()    { return winH() - listTop(); }

    private int totalContentH(List<KitData> kits) { return kits.size() * ROW_H; }
    private int maxScroll(List<KitData> kits)      { return Math.max(0, totalContentH(kits) - listViewH()); }

    // ── Data ─────────────────────────────────────────────────────────────────

    private List<KitData> getFilteredKits() {
        List<KitData> result = new ArrayList<>();
        for (KitData k : ClientKitRegistry.all()) {
            boolean custom = k.isCustom();
            if      (activeSection == Section.ALL)                result.add(k);
            else if (activeSection == Section.BUILTIN && !custom) result.add(k);
            else if (activeSection == Section.CUSTOM  &&  custom) result.add(k);
        }
        return result;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, winW(), winH(), 0xFF1E1E1E);
        renderToolbar(ctx, mouseX, mouseY);
        renderSidebar(ctx, mouseX, mouseY);
        renderHeader(ctx);
        renderListWithScissor(ctx, mouseX, mouseY);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderToolbar(DrawContext ctx, int mx, int my) {
        ctx.fill(0, 0, winW(), TOOLBAR_H, 0xFF2D2D2D);
        ctx.fill(0, TOOLBAR_H - 1, winW(), TOOLBAR_H, 0xFF444444);
        ctx.drawText(textRenderer, "§fKits", 10,
                (TOOLBAR_H - textRenderer.fontHeight) / 2, 0xFFFFFF, false);
        int btnW = 70, btnH = 18;
        int btnX = winW() - btnW - 8;
        int btnY = (TOOLBAR_H - btnH) / 2;
        boolean hov = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
        ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xFF3A6BC4 : 0xFF2A5BB4);
        ctx.drawBorder(btnX, btnY, btnW, btnH, 0xFF5588DD);
        ctx.drawText(textRenderer, "+ New Kit",
                btnX + (btnW - textRenderer.getWidth("+ New Kit")) / 2,
                btnY + (btnH - textRenderer.fontHeight) / 2, 0xFFFFFF, false);
    }

    private void renderSidebar(DrawContext ctx, int mx, int my) {
        ctx.fill(sidebarLeft(), sidebarTop(), sidebarRight(), winH(), 0xFF252525);
        ctx.fill(sidebarRight() - 1, sidebarTop(), sidebarRight(), winH(), 0xFF3A3A3A);
        ctx.drawText(textRenderer, "§7LIBRARY", sidebarLeft() + 10, sidebarTop() + 10, 0x888888, false);
        renderSidebarEntry(ctx, mx, my, "  All Kits", Section.ALL,     sidebarTop() + 26);
        renderSidebarEntry(ctx, mx, my, "  Built-in", Section.BUILTIN, sidebarTop() + 44);
        renderSidebarEntry(ctx, mx, my, "  My Kits",  Section.CUSTOM,  sidebarTop() + 62);
    }

    private void renderSidebarEntry(DrawContext ctx, int mx, int my,
                                    String label, Section section, int y) {
        int h = 16;
        boolean hov    = mx >= sidebarLeft() && mx < sidebarRight() && my >= y && my < y + h;
        boolean active = activeSection == section;
        if      (active) ctx.fill(sidebarLeft(), y, sidebarRight() - 1, y + h, 0xFF3A3A6A);
        else if (hov)    ctx.fill(sidebarLeft(), y, sidebarRight() - 1, y + h, 0xFF2F2F2F);
        if (active)      ctx.fill(sidebarLeft(), y, sidebarLeft() + 3,  y + h, 0xFF6688FF);
        ctx.drawText(textRenderer, label, sidebarLeft() + 6,
                y + (h - textRenderer.fontHeight) / 2,
                active ? 0xFFFFFF : 0xAAAAAA, false);
    }

    private void renderHeader(DrawContext ctx) {
        int y = contentTop();
        ctx.fill(contentLeft(), y, contentRight(), y + HEADER_H, 0xFF2A2A2A);
        ctx.fill(contentLeft(), y + HEADER_H - 1, contentRight(), y + HEADER_H, 0xFF3A3A3A);
        ctx.drawText(textRenderer, "Name",  contentLeft() + 46, y + 3, 0x888888, false);
        ctx.drawText(textRenderer, "Type",  contentLeft() + 200, y + 3, 0x888888, false);
        ctx.drawText(textRenderer, "Slots", contentLeft() + 300, y + 3, 0x888888, false);
    }

    private void renderListWithScissor(DrawContext ctx, int mx, int my) {
        List<KitData> kits = getFilteredKits();

        if (kits.isEmpty()) {
            String msg = "No kits here. Click '+ New Kit' to create one.";
            ctx.drawText(textRenderer, msg,
                    contentLeft() + (contentRight() - contentLeft() - textRenderer.getWidth(msg)) / 2,
                    listTop() + 40, 0x666666, false);
            return;
        }

        ctx.enableScissor(contentLeft(), listTop(), contentRight(), winH());

        int y = listTop() - scrollOffset;
        for (int i = 0; i < kits.size(); i++) {
            KitData kit = kits.get(i);
            boolean visible = (y + ROW_H > listTop()) && (y < winH());
            boolean hov = mx >= contentLeft() && mx < contentRight()
                    && my >= y && my < y + ROW_H && my >= listTop();
            boolean sel = kit == selectedKit;

            if (visible) {
                if      (sel)        ctx.fill(contentLeft(), y, contentRight(), y + ROW_H, 0xFF2A3A6A);
                else if (hov)        ctx.fill(contentLeft(), y, contentRight(), y + ROW_H, 0xFF2D2D2D);
                else if (i % 2 == 0) ctx.fill(contentLeft(), y, contentRight(), y + ROW_H, 0xFF212121);
                else                 ctx.fill(contentLeft(), y, contentRight(), y + ROW_H, 0xFF1E1E1E);
                ctx.fill(contentLeft(), y + ROW_H - 1, contentRight(), y + ROW_H, 0xFF2A2A2A);

                renderKitIcon(ctx, kit, contentLeft() + 8, y + (ROW_H - 16) / 2);

                // Name: convert & colour codes → § so they render coloured
                ctx.drawText(textRenderer,
                        ampersandToFormatted(kit.getDisplayName()),
                        contentLeft() + 30,
                        y + (ROW_H - textRenderer.fontHeight) / 2,
                        sel ? 0xFFFFFF : 0xDDDDDD, false);

                boolean custom = kit.isCustom();
                ctx.drawText(textRenderer, custom ? "§aCustom" : "§9Built-in",
                        contentLeft() + 200,
                        y + (ROW_H - textRenderer.fontHeight) / 2, 0xFFFFFF, false);
                ctx.drawText(textRenderer, kit.getCount() + " items",
                        contentLeft() + 300,
                        y + (ROW_H - textRenderer.fontHeight) / 2, 0x888888, false);

                if (hov || sel) renderRowActions(ctx, mx, my, kit, y, ROW_H);
            }
            y += ROW_H;
        }

        ctx.disableScissor();

        if (totalContentH(kits) > listViewH()) renderScrollbar(ctx, kits);
    }

    private void renderScrollbar(DrawContext ctx, List<KitData> kits) {
        int trackX  = contentRight() - 6;
        int trackY  = listTop();
        int trackH  = listViewH();
        ctx.fill(trackX, trackY, trackX + 4, trackY + trackH, 0xFF333333);
        float ratio     = (float) listViewH() / totalContentH(kits);
        int thumbH      = Math.max(16, (int)(trackH * ratio));
        int thumbTravel = trackH - thumbH;
        int thumbY      = trackY + (maxScroll(kits) > 0
                ? (int)(thumbTravel * ((float) scrollOffset / maxScroll(kits))) : 0);
        ctx.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFF777799);
    }

    /** Row buttons: Load | View | Open  (Edit moved to popup) */
    private void renderRowActions(DrawContext ctx, int mx, int my, KitData kit, int rowY, int rowH) {
        int btnH = 14;
        int btnY = rowY + (rowH - btnH) / 2;
        int x    = contentRight() - 8;

        x -= 40; renderMiniBtn(ctx, mx, my, "Open", x, btnY, 38, btnH, 0xFF2A5BB4);
        x -= 36; renderMiniBtn(ctx, mx, my, "View", x, btnY, 34, btnH, 0xFF2A4A2A);
        x -= 36; renderMiniBtn(ctx, mx, my, "Load", x, btnY, 34, btnH, 0xFF4A3A0A);
    }

    private void renderMiniBtn(DrawContext ctx, int mx, int my,
                               String label, int x, int y, int w, int h, int bg) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;
        ctx.fill(x, y, x + w, y + h, hov ? brighten(bg) : bg);
        ctx.drawBorder(x, y, w, h, 0xFF555555);
        ctx.drawText(textRenderer, label,
                x + (w - textRenderer.getWidth(label)) / 2,
                y + (h - textRenderer.fontHeight) / 2, 0xFFFFFF, false);
    }

    private void renderKitIcon(DrawContext ctx, KitData kit, int x, int y) {
        try {
            if (kit.getIconItem() != null && !kit.getIconItem().isEmpty()) {
                Identifier id = Identifier.of(kit.getIconItem());
                Item item = Registries.ITEM.get(id);
                ctx.drawItem(new ItemStack(item), x, y);
                return;
            }
        } catch (Exception ignored) {}
        ctx.fill(x, y, x + 16, y + 16,
                kit.isCustom() ? 0xFF3A6A3A : 0xFF3A3A8A);
        ctx.drawText(textRenderer, "K", x + 4, y + 4, 0xFFFFFF, false);
    }

    private int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + 30);
        int g = Math.min(255, ((color >> 8)  & 0xFF) + 30);
        int b = Math.min(255, ( color        & 0xFF) + 30);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Replace & with § so Minecraft colour codes render (&a→§a, &c→§c, etc.) */
    private Text ampersandToFormatted(String raw) {
        return Text.literal(raw.replace('&', '§'));
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int imx = (int) mx, imy = (int) my;

        // New Kit toolbar button
        int btnW = 70, btnH = 18;
        int btnX = winW() - btnW - 8;
        int btnY = (TOOLBAR_H - btnH) / 2;
        if (imx >= btnX && imx <= btnX + btnW && imy >= btnY && imy <= btnY + btnH) {
            openCreateKit(); return true;
        }

        // Sidebar
        if (imx >= sidebarLeft() && imx < sidebarRight()) {
            int st = sidebarTop();
            if      (imy >= st + 26 && imy < st + 42) { activeSection = Section.ALL;     scrollOffset = 0; return true; }
            else if (imy >= st + 44 && imy < st + 60) { activeSection = Section.BUILTIN; scrollOffset = 0; return true; }
            else if (imy >= st + 62 && imy < st + 78) { activeSection = Section.CUSTOM;  scrollOffset = 0; return true; }
        }

        // List rows
        if (imx >= contentLeft() && imx < contentRight() && imy >= listTop()) {
            List<KitData> kits = getFilteredKits();
            int relY = imy - listTop() + scrollOffset;
            int idx  = relY / ROW_H;
            if (idx >= 0 && idx < kits.size()) {
                KitData kit  = kits.get(idx);
                int rowY = listTop() + idx * ROW_H - scrollOffset;
                int bH   = 14;
                int bY   = rowY + (ROW_H - bH) / 2;
                int x    = contentRight() - 8;

                // Open
                x -= 40;
                if (imx >= x && imx <= x + 38 && imy >= bY && imy <= bY + bH) { openKitPopup(kit); return true; }
                // View
                x -= 36;
                if (imx >= x && imx <= x + 34 && imy >= bY && imy <= bY + bH) { openKitView(kit);  return true; }
                // Load
                x -= 36;
                if (imx >= x && imx <= x + 34 && imy >= bY && imy <= bY + bH) { openKitLoad(kit);  return true; }

                selectedKit = kit;
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        if ((int) mx < contentLeft()) return false;
        List<KitData> kits = getFilteredKits();
        int max = maxScroll(kits);
        if (max <= 0) return false;
        scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int)(vAmt * ROW_H)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()) {
            close();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void openKitPopup(KitData kit) {
        assert client != null;
        client.setScreen(new KitPopupScreen(this, kit));
    }

    private void openKitView(KitData kit) {
        KitViewScreen.open(kit,this);
    }

    private void openKitLoad(KitData kit) {
        assert client != null;
        client.setScreen(new KitLoadScreen(this, kit));
    }

    private void openCreateKit() {
        ClientPlayNetworking.send(new CreateNewKitC2SPacket("new_kit"));
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}