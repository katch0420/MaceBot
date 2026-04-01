package net.katch0420.macebot.main.utils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class MatricesHelper {
    private DrawContext context;
    private TextRenderer textRenderer;
    private float scale = 1.0f;

    public MatricesHelper(DrawContext context, TextRenderer textRenderer) {
        this.context = context;
        this.textRenderer = textRenderer;
    }

    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public void setContext(DrawContext context) {
        this.context = context;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void calculateScale(int width, int availableWidth){
        this.scale = Math.min(1.0f, (float) availableWidth / width);
    }

    public void drawScaledText(Text text, int x, int y, int color){
        if (scale >= 0.99f) {
            // No scaling needed, draw normally
            context.drawText(textRenderer, text, x, y, color, false);
            return;
        }

        context.getMatrices().push();

        // Scale around the origin point (x, y)
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        // Draw at origin since we already translated
        context.drawText(textRenderer, text, 0, 0, color, false);

        context.getMatrices().pop();
    }

    public void drawScaledText(String s, int x, int y, int color){
        this.drawScaledText(Text.of(s), x, y, color);
    }

    public void drawScaledCenteredText(Text text, int x, int y, int color){
        if (scale >= 0.99f) {
            // No scaling needed, draw normally
            context.drawCenteredTextWithShadow(textRenderer, text, x, y, color);
            return;
        }

        context.getMatrices().push();

        // Translate to the center position, accounting for scaled width
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        context.drawCenteredTextWithShadow(textRenderer, text, 0, 0, color);

        context.getMatrices().pop();
    }

    public void drawScaledCenteredText(String s, int x, int y, int color){
        this.drawScaledCenteredText(Text.of(s), x, y, color);
    }

    /**
     * Get the scaled width of a text element
     */
    public int getScaledWidth(Text text) {
        return (int) (textRenderer.getWidth(text) * scale);
    }

    /**
     * Get the scaled width of a string
     */
    public int getScaledWidth(String s) {
        return getScaledWidth(Text.of(s));
    }

    /**
     * Get current scale factor
     */
    public float getScale() {
        return scale;
    }
}