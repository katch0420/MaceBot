package net.katch0420.macebot.main.kits.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SimpleButton {
    private final int x, y;
    private final int width, height;
    protected final String label;
    private final Runnable onClick;
    private final int normalColor;
    private final int hoverColor;
    private final int textColor;

    public SimpleButton(int x, int y, int width, int height, String label, Runnable onClick) {
        this(x, y, width, height, label, onClick, 0x00000000, 0x80505050, 0xFFFFFF);
    }

    public SimpleButton(int x, int y, int width, int height, String label, Runnable onClick,
                        int normalColor, int hoverColor, int textColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.onClick = onClick;
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean hover = isMouseOver(mouseX, mouseY);

        // Draw background
        ctx.fill(x, y, x + width, y + height, hover ? hoverColor : normalColor);

        // Draw centered text
        ctx.drawCenteredTextWithShadow(textRenderer, label,
                x + width / 2,
                y + (height - 8) / 2,
                textColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            onClick.run();
            return true;
        }
        return false;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width &&
                mouseY >= y && mouseY < y + height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}