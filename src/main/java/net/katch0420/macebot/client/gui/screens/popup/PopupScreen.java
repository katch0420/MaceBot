package net.katch0420.macebot.client.gui.screens.popup;

import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.client.gui.widgets.core.BaseWidget;
import net.katch0420.macebot.main.utils.YarnHelpers;
//? <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PopupScreen extends Screen {

    public final Screen parent;
    private final Screen renderParent;
    protected Component title;
    protected Theme theme;

    protected int popupX, popupY, popupW, popupH;

    protected PopupScreen(Screen parent) {
        super(Component.empty());
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

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    // No-op: we handle background ourselves so Minecraft's dirt/blur never shows.
    //? if >1.21.11 {
    /*@Override
    public void extractBackground(GuiGraphicsExtractor c, int mx, int my, float a) {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float d) {
        // 1. Render parent behind us (mouse coords set to -1 so no hover state fires)
        if (renderParent != null) renderParent.extractRenderState(c, -1, -1, d);

        YarnHelpers.pushMatrix(c);
        // 2. Push Z to 400 — parent text batches flush at ~200, so anything below
        //    that value lets deferred text bleed through on top of our popup fills.
        YarnHelpers.pushMatrix(c);
        //? if >=1.21.6 {
        /^c.nextStratum();
        ^///?} else
        c.pose().translate(0, 0, 400);

        // 3. Full-screen dim so the parent visibly recedes behind the popup.
        //    Without this the popup blends into the parent since they share the same
        //    background color.
        c.fill(0, 0, this.width, this.height, 0xAA000000);

        // 4. Popup shadow (subtle depth cue)
        c.fill(popupX + 3, popupY + 3, popupX + popupW + 3, popupY + popupH + 3, 0x55000000);

        // 5. Popup content (implemented by subclasses)
        renderPopupScreen(c, mx, my);

        // 6. Widgets (buttons, text fields, etc.) — rendered after fills so they sit on top
        super.extractRenderState(c, mx, my, d);

        YarnHelpers.popMatrix(c);
    }

    public void renderPopupScreen(GuiGraphicsExtractor c, int mx, int my) {}

    protected void drawPopupBorder(GuiGraphicsExtractor c, int col) {
        drawBorder(c, popupX, popupY, popupW, popupH, col);
    }

    protected void drawBorder(GuiGraphicsExtractor c, int x, int y, int w, int h, int col) {
        c.fill(x,     y,         x + w, y + 1,     col);
        c.fill(x,     y + h - 1, x + w, y + h,     col);
        c.fill(x,     y,         x + 1, y + h,     col);
        c.fill(x + w - 1, y,     x + w, y + h,     col);
    }

    *///?}
    //? if <=1.21.11 {
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(GuiGraphics c, int mx, int my, float d) {
        // 1. Render parent behind us (mouse coords set to -1 so no hover state fires)
        if (renderParent != null) renderParent.render(c, -1, -1, d);

        YarnHelpers.pushMatrix(c);
        YarnHelpers.pushMatrix(c);
        //? if >=1.21.6 {
        c.nextStratum();
        //?} else
        //c.pose().translate(0, 0, 400);

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

    public void renderPopupScreen(GuiGraphics c, int mx, int my) {}

    protected void drawPopupBorder(GuiGraphics c, int col) {
        drawBorder(c, popupX, popupY, popupW, popupH, col);
    }

    protected void drawBorder(GuiGraphics c, int x, int y, int w, int h, int col) {
        c.fill(x,     y,         x + w, y + 1,     col);
        c.fill(x,     y + h - 1, x + w, y + h,     col);
        c.fill(x,     y,         x + 1, y + h,     col);
        c.fill(x + w - 1, y,     x + w, y + h,     col);
    }
    //?}

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
    public boolean mouseClicked(
            //? if >=1.21.9 {
            MouseButtonEvent c, boolean bl
            //?} else
            //double mx, double my, int b
    ) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int b = c.button();
        if(!(mx >= popupX - 20 && mx < popupX + popupW + 20 && my >= popupY - 20 && my < popupY + popupH + 20)) onClose();
        return super.mouseClicked(
                //? if >=1.21.9 {
                c,bl
                //?} else
                //mx, my, b
        );
    }



    @Override
    public void onClose() {
        setScreen(parent);
    }

    private void setScreen(Screen screen){
        //? if <26.2 {
        Minecraft.getInstance().setScreen(screen);
        //?} else
        //Minecraft.getInstance().setScreenAndShow(screen);
    }

    public void tick() {
        for(GuiEventListener e: children()){
            if(e instanceof BaseWidget<?> b) b.tick();
        }
    }
}