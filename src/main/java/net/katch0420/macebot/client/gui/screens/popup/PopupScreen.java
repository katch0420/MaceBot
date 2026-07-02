package net.katch0420.macebot.client.gui.screens.popup;

import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.client.gui.widgets.core.BaseWidget;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class PopupScreen extends Screen {

    public final Screen parent;
    private final Screen renderParent;

    protected Text title;
    protected Theme theme;

    protected int popupX, popupY, popupW, popupH;

    protected PopupScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
        Screen s = parent;
        while(s instanceof PopupScreen p){
            s = p.parent;
        }
        renderParent = s;
    }

    public void size(int w, int h) {
        this.popupW = w;
        this.popupH = h;
    }

    @Override
    protected void init() {
        theme = Themes.CURRENT;
    }

    // No-op: we handle background ourselves so Minecraft's dirt/blur never shows.
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        // 1. Render parent behind us (mouse coords set to -1 so no hover state fires)
        if (renderParent != null) renderParent.render(c, -1, -1, d);

        YarnHelpers.pushMatrix(c);
        // 2. Push Z to 400 — parent text batches flush at ~200, so anything below
        //    that value lets deferred text bleed through on top of our popup fills.
        c.getMatrices().translate(0, 0, 400);

        // 3. Full-screen dim so the parent visibly recedes behind the popup.
        //    Without this the popup blends into the parent since they share the same
        //    background color.
        c.fill(0, 0, this.width, this.height, 0xAA000000);

        // 4. Popup shadow (subtle depth cue)
        c.fill(popupX + 3, popupY + 3, popupX + popupW + 3, popupY + popupH + 3, 0x55000000);

        // 5. Popup content (implemented by subclasses)
        renderPopupScreen(c, mx, my);

        // 6. Widgets (buttons, text fields, etc.) — rendered after fills so they sit on top
        super.render(c, mx, my, d);

        YarnHelpers.popMatrix(c);
    }

    /** Override in subclasses to draw the popup panel. Called inside the Z-pushed matrix. */
    public void renderPopupScreen(DrawContext c, int mx, int my) {}

    // ── Shared helpers (available to all subclasses) ──────────────────────────

    /**
     * Draws a 1px border around a rectangle using the current theme's accent color.
     * Call from renderPopupScreen().
     */
    protected void drawPopupBorder(DrawContext c, int col) {
        drawBorder(c, popupX, popupY, popupW, popupH, col);
    }

    protected void drawBorder(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x,     y,         x + w, y + 1,     col);
        c.fill(x,     y + h - 1, x + w, y + h,     col);
        c.fill(x,     y,         x + 1, y + h,     col);
        c.fill(x + w - 1, y,     x + w, y + h,     col);
    }

    /**
     * Returns a background color that's always visually distinct from the parent
     * screen. On dark themes this is a near-black; on light themes it darkens
     * the theme's own background so the popup still reads clearly.
     */
    protected int popupBodyColor() {
        int bg = theme.body_background();
        int r = (bg >> 16) & 0xFF;
        int g = (bg >>  8) & 0xFF;
        int b =  bg        & 0xFF;
        float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255f;
        if (lum > 0.4f) {
            // Light theme — darken significantly so the popup stands out
            return 0xFF000000 | ((r * 55 / 100) << 16) | ((g * 55 / 100) << 8) | (b * 55 / 100);
        }
        // Dark theme — use a fixed near-black that's always darker than the parent
        return 0xFF16181D;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    public void tick() {
        for(Element e: children()){
            if(e instanceof BaseWidget<?> b) b.tick();
        }
    }
}