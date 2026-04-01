package net.katch0420.macebot.client.gui.widgets.buttons;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.katch0420.macebot.client.gui.widgets.builder.ButtonBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ButtonWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClickButtonWidget extends ButtonWidget {

    private final Text label;
    private final int backgroundColor;
    private final int foregroundColor;

    protected ClickButtonWidget(int x, int y, int width, int height,
                                Text label, int backgroundColor, int foregroundColor,
                                Consumer<ButtonWidget> onClick, ParentWidget parent) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);

        this.label = label;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;

        setOnClick(onClick);
        setParent(parent);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = backgroundColor;
        if (isHovered()) {
            // Slightly darken when hovered
            color = (color & 0xFF000000) | Math.max(0, ((color & 0x00FFFFFF) - 0x101010));
        }

        // Draw button background
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

        if (label == null) return;

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textOffsetX = (getWidth() - textRenderer.getWidth(label)) / 2;
        int textOffsetY = (getHeight() - textRenderer.fontHeight) / 2;

        // Draw centered label
        context.drawText(textRenderer, label,
                getX() + textOffsetX,
                getY() + textOffsetY,
                foregroundColor,
                false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ButtonBuilder<Builder> {
        public ClickButtonWidget build() {
            return new ClickButtonWidget(x, y, width, height, label, color, textColor, onClick, parent);
        }
    }
}
