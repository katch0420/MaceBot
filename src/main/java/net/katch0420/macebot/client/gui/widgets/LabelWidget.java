package net.katch0420.macebot.client.gui.widgets;

import net.katch0420.macebot.client.gui.widgets.builder.LabelBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LabelWidget extends ChildWidget {

    public static final int DEFAULT_WIDTH = 100;
    public static final int DEFAULT_HEIGHT = 20;

    private Text label;
    private final int backgroundColor;
    private final int foregroundColor;

    public LabelWidget(int x, int y, int width, int height, Text label, int backgroundColor, int foregroundColor) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);

        this.label = label;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }

    public Text getLabel() {
        return label;
    }

    public void setLabel(Text label) {
        this.label = label;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);

        if (label == null) return;

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textOffsetX = (getWidth() - textRenderer.getWidth(label)) / 2;
        int textOffsetY = (getHeight() - textRenderer.fontHeight) / 2;

        // Draw centered text
        context.drawText(textRenderer, label,
                getX() + textOffsetX,
                getY() + textOffsetY,
                foregroundColor,
                false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LabelBuilder<Builder> {
        public LabelWidget build() {
            return new LabelWidget(x, y, width, height, label, color, textColor);
        }
    }
}
