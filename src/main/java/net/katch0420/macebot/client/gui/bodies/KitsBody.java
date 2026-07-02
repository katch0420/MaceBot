package net.katch0420.macebot.client.gui.bodies;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.bodies.popup.KitMenuPopup;
import net.katch0420.macebot.client.gui.screens.popup.StyledTextEditor;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.Scroller;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.networking.packets.c2s.KitSyncC2SPacket;
import net.katch0420.macebot.main.networking.packets.s2c.KitSyncS2CPacket;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class KitsBody extends Body {

    // ── Layout ────────────────────────────────────────────────────────────────
    private int margin;
    private int panelW, panelH;
    private int categoryX, categoryY, categoryW;
    private int kitListX, kitListY, kitListW, kitListH;
    private int entryH;
    private int textHeight;
    private static final int SCROLLBAR_W = 4;

    // ── State ─────────────────────────────────────────────────────────────────
    private Filter filter = Filter.ALL;
    private final List<KitEntry>  kitEntries      = new ArrayList<>();
    private final List<TabButton> categoryButtons = new ArrayList<>();
    private final Scroller        scroller        = new Scroller();

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void init() {
        super.init();
        margin     = s(6, 4);
        textHeight = getTextRenderer().fontHeight;
        entryH     = s(22, 18);

        panelW = availableWidth;
        panelH = availableHeight;

        categoryX = x + margin;
        categoryY = y + margin;
        // BUG FIX: was s(100,80) but never used consistently — use a fixed fraction
        categoryW = s(90, 72);

        kitListX = categoryX + categoryW + margin;
        kitListY = categoryY;
        // BUG FIX: kitListW was never assigned
        kitListW = panelW - (categoryX - x) - categoryW - margin * 3;
        kitListH = panelH - margin * 2;

        buildCategoryButtons();
        rebuildKitList();
    }

    // ── Category buttons ──────────────────────────────────────────────────────

    private void buildCategoryButtons() {
        categoryButtons.clear();
        record Tab(String label, Runnable action) {}
        List<Tab> tabs = List.of(
                new Tab("All",      this::showAll),
                new Tab("Built-in", this::showBuiltIn),
                new Tab("Custom",   this::showCustom)
        );
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            int finalI = i;
            categoryButtons.add(new TabButton(
                    categoryX, categoryY + i * entryH,
                    categoryW, entryH - 1,
                    t.action(), t.label(),
                    () -> filter == (finalI == 0 ? Filter.ALL : finalI == 1 ? Filter.BUILT_IN : Filter.CUSTOM)
            ));
        }

        categoryButtons.add(new TabButton(
                categoryX, categoryY + 3 * entryH + entryH,
                categoryW, entryH -1,
                this::openNewKitPrompt, "+ New Kit",
                ()-> false
                )
        );
    }

    private void openNewKitPrompt() {
        MinecraftClient.getInstance().setScreen(new StyledTextEditor(parent,Text.of("Create New Kit"), "New Kit",
                name -> {
                    String id = name.toLowerCase().replaceAll("[^a-z0-9_]", "_")
                            + "_" + (System.currentTimeMillis() % 10000);
                    int i = 1;
                    boolean b = true;
                    while(ClientKitRegistry.allKitIds().contains(id)){
                        if(b){
                            id = id + "_" + i++;
                            b = false;
                            continue;
                        }
                        id = id.replace("_" + (i - 1),"_" + i++);
                    }
                    Kit kit = new Kit(id, name, Registries.ITEM.getId(Items.WOODEN_SWORD), true);
                    ClientKitRegistry.register(kit);
                    ClientPlayNetworking.send(new KitSyncC2SPacket(
                            KitSyncC2SPacket.CMD_NEW_KIT, id, name, Registries.ITEM.getId(Items.WOODEN_SWORD).getPath(), "{}", true));
                }));
    }

    private void showAll()     { filter = Filter.ALL;      rebuildKitList(); }
    private void showBuiltIn() { filter = Filter.BUILT_IN; rebuildKitList(); }
    private void showCustom()  { filter = Filter.CUSTOM;   rebuildKitList(); }

    // ── Kit list ──────────────────────────────────────────────────────────────

    public void rebuildKitList() {
        kitEntries.clear();

        List<Kit> ks = new ArrayList<>();
        for (Kit k : ClientKitRegistry.allSorted()) {
            switch (filter) {
                case ALL      -> ks.add(k);
                case BUILT_IN -> { if (!k.isCustom()) ks.add(k); }
                case CUSTOM   -> { if ( k.isCustom()) ks.add(k); }
            }
        }

        // BUG FIX: was using indexOf() inside forEach — O(n²) and broken if duplicates
        for (int i = 0; i < ks.size(); i++) {
            kitEntries.add(new KitEntry(ks.get(i)));
        }

        int contentH = kitEntries.size() * entryH;
        scroller.setArea(kitListX, kitListY, kitListH, Math.max(kitListH, contentH));
    }

    private void updateEntryPositions() {
        int offset = scroller.getOffset();
        for (int i = 0; i < kitEntries.size(); i++) {
            KitEntry e = kitEntries.get(i);
            e.x = kitListX;
            e.y = kitListY + i * entryH - offset;
            e.w = kitListW - SCROLLBAR_W - margin;
            e.h = entryH;
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);

        // Category tabs
        for (TabButton b : categoryButtons)
            if (b.mouseClicked(mx, my)) return true;

        // Scrollbar
        int barX = kitListX + kitListW - SCROLLBAR_W;
        if (scroller.mouseClicked(mx, my, barX, SCROLLBAR_W)) return true;

        // Kit entries
        updateEntryPositions();
        for (KitEntry e : kitEntries) {
            if (e.isMouseOver(mx, my)) {
                openKitMenu(e.kit);
                return true;
            }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if(scroller.mouseReleased()){
            return true;
        };
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (btn == 0) if(scroller.mouseDragged(my)) return true;
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (scroller.mouseScrolled(mx, my, kitListX, kitListW, vAmount, entryH))
            return true;
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    private void openKitMenu(Kit kit) {
        MinecraftClient.getInstance().setScreen(new KitMenuPopup(parent, kit,
                null
        ));
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        c.fill(x, y, x + panelW, y + panelH, theme.body_background());

        renderCategories(c, mx, my);
        // Divider between tabs and list
        c.fill(kitListX - margin / 2, kitListY, kitListX - margin / 2 + 1, kitListY + kitListH, theme.panel_separator());

        updateEntryPositions();
        renderKitList(c, mx, my);
        renderScrollbar(c);

        // Draw category buttons AFTER scissor so they render on top
        super.render(c, mx, my, d);
    }

    private void renderCategories(DrawContext c, int mx, int my) {
        for (TabButton b : categoryButtons) b.render(c, mx, my);
    }

    private void renderKitList(DrawContext c, int mx, int my) {
        // Clip to list area so entries don't bleed outside
        c.enableScissor(kitListX, kitListY, kitListX + kitListW - SCROLLBAR_W, kitListY + kitListH);
        for (KitEntry e : kitEntries) {
            // Only render visible entries
            if (e.y + e.h > kitListY && e.y < kitListY + kitListH)
                e.render(c, mx, my);
        }
        c.disableScissor();
    }

    private void renderScrollbar(DrawContext c) {
        int barX = kitListX + kitListW - SCROLLBAR_W;
        scroller.render(c, barX, SCROLLBAR_W,
                theme.panel_separator(),
                theme.accent());
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    private long ticks = 0;
    @Override
    public void tick() {
        if (ticks++ % 40 == 0) {
            rebuildKitList();
        }
    }

    @Override
    public void clearAndInit() {
        kitEntries.clear();
        categoryButtons.clear();
        super.clearAndInit();
    }

    @Override
    public Text getLabel() { return Text.of("Kits"); }

    @Override
    public boolean keyPressed(int k, int s, int m) { return false; }

    // ── Filter ────────────────────────────────────────────────────────────────

    public enum Filter { ALL, BUILT_IN, CUSTOM }

    // ── KitEntry ──────────────────────────────────────────────────────────────

    protected class KitEntry {
        int x, y, w, h;
        final Kit kit;

        KitEntry(Kit kit) {
            this.kit = kit;
        }

        boolean isMouseOver(double mx, double my) {
            return mx >= x && my >= y && mx < x + w && my < y + h;
        }

        void render(DrawContext c, int mx, int my) {
            boolean hov = isMouseOver(mx, my);
            int bg = hov
                    ? (theme.body_button_background() & 0x00FFFFFF | 0xCC000000)
                    : (theme.body_background()        & 0x00FFFFFF | 0xAA000000);
            c.fill(x, y, x + w, y + h, bg);

            // Subtle bottom separator
            c.fill(x + margin, y + h - 1, x + w - margin, y + h, theme.panel_separator() & 0x44FFFFFF);

            // Icon
            int iconX = x + margin;
            int iconY = y + (h - 16) / 2;
            try {
                Identifier iconId = kit.getIconId();
                if (iconId != null) {
                    Item item = Registries.ITEM.get(
                            iconId);
                    c.drawItem(new ItemStack(item), iconX, iconY);
                }
            } catch (Exception ignored) {}

            // Name
            int textX = iconX + 18 + margin / 2;
            // BUG FIX: was (h - textHeight) / 2 (absolute) instead of y + (h - textHeight) / 2
            int textY = y + (h - textHeight) / 2;

            c.drawText(getTextRenderer(),
                    LegacyText.parse(kit.getDisplayName()),
                    textX, textY, theme.body_label(), false);

            // Badge (only if not showing the "All" filter — otherwise always shown)
            String badge   = kit.isCustom() ? " [Custom]" : " [Built-in]";
            int    badgeC  = kit.isCustom() ? theme.success() : theme.accent();
            int    nameEnd = textX + getTextRenderer().getWidth(LegacyText.parse(kit.getDisplayName()));
            c.drawText(getTextRenderer(), Text.of(badge), nameEnd, textY, badgeC, false);

            // Item count (right-aligned)
            String cnt  = kit.getItems().size() + " items";
            int    cntX = x + w - margin - getTextRenderer().getWidth(cnt);
            if (cntX > nameEnd + margin) {
                c.drawText(getTextRenderer(), Text.of(cnt), cntX, textY, theme.body_value(), false);
            }
        }
    }

    // ── TabButton ─────────────────────────────────────────────────────────────

    protected class TabButton {
        private final int x, y, w, h;
        private final Runnable onClick;
        private final String label;
        private final BooleanSupplier isActive;

        TabButton(int x, int y, int w, int h, Runnable onClick, String label,
                  BooleanSupplier isActive) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.onClick  = onClick;
            this.label    = label;
            this.isActive = isActive;
        }

        boolean mouseClicked(double mx, double my) {
            if (isMouseOver(mx, my)) { onClick.run(); return true; }
            return false;
        }

        boolean isMouseOver(double mx, double my) {
            return mx >= x && my >= y && mx < x + w && my < y + h;
        }

        void render(DrawContext c, int mx, int my) {
            boolean active = isActive.getAsBoolean();
            boolean hov    = isMouseOver(mx, my);
            int bg = active ? theme.accent()
                    : hov   ? (theme.body_button_background() & 0x00FFFFFF | 0xCC000000)
                    : theme.body_button_background();
            c.fill(x, y, x + w, y + h, bg);

            // Active indicator: left bar
            if (active) c.fill(x, y, x + 2, y + h, theme.accent_hover());

            // BUG FIX: was using (w - textW)/2 and (w - 1 - textH)/2 both relative
            // to 0 instead of to x,y
            int textW = getTextRenderer().getWidth(label);
            int textX = x + (w - textW) / 2;
            int textY = y + (h - textHeight) / 2;
            c.drawText(getTextRenderer(), Text.of(label), textX, textY,
                    active ? 0xFFFFFFFF : theme.body_button_foreground(), true);
        }
    }
}