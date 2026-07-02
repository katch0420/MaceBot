package net.katch0420.macebot.client.gui.bodies;

import net.katch0420.macebot.client.gui.frames.MainFrame;
import net.katch0420.macebot.client.gui.layout.ResponsiveLayout;
import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class Body extends ParentWidget<Body> {

    protected Theme theme = Themes.CURRENT;

    public Screen parent;

    protected int availableWidth;
    protected int availableHeight;

    /**
     * Continuous responsive scale factor (~1.0 at the reference width).
     * Use {@link #s(int)} to scale a base pixel value by it.
     */
    protected float uiScale = 1.0f;

    public Text getLabel() {
        return Text.of("");
    }

    public void init() {
        clearChildren();
        theme = Themes.CURRENT;
        availableWidth = Math.max(1, this.width);
        availableHeight = Math.max(1, this.height);

        uiScale = ResponsiveLayout.scaleFactor(availableWidth);
    }

    MainFrame parentScreen;

    public Body setParentScreen(MainFrame parentScreen) {
        this.parentScreen = parentScreen;
        return this;
    }

    public void clearAndInit() {
        clearChildren();
        init();
    }

    public void removed(){
    }

    public TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    /** Scales a base pixel value by this body's current responsive {@link #uiScale}. */
    protected int s(int base) {
        return ResponsiveLayout.scaled(base, uiScale);
    }

    /** Scales a base pixel value, but never below {@code min}. */
    protected int s(int base, int min) {
        return ResponsiveLayout.scaledMin(base, uiScale, min);
    }
}
