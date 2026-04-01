package net.katch0420.macebot.client.gui.widgets.buttons;

import net.katch0420.macebot.client.gui.widgets.builder.ButtonBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ButtonWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClickListButtonWidget extends ButtonWidget {

    private final Text label;
    private Text current;

    private final int backgroundColor;
    private final int foregroundColor;

    protected ClickListButtonWidget(int x, int y, int width, int height,
                                    int backgroundColor, Text label, int foregroundColor,
                                    Consumer<ButtonWidget> onClick, ParentWidget parent, Text option) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);

        this.label = label;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.current = option;

        setOnClick(onClick);
        setParent(parent);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = backgroundColor;
        if (isHovered()) {
            // Slightly brighten when hovered
            color = (color & 0xFF000000) | Math.max(0, ((color & 0x00FFFFFF) - 0x101010));
        }

        // Draw background
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textOffsetY = (getHeight() - textRenderer.fontHeight) / 2;
        int textOffsetX = textOffsetY;

        // Draw left label
        if (label != null) {
            context.drawText(textRenderer, label,
                    getX() + textOffsetX,
                    getY() + textOffsetY,
                    foregroundColor,
                    false);
        }

        // Draw right‑aligned current option
        if (current != null) {
            int optionX = getWidth() - (4 + textRenderer.getWidth(current));
            context.drawText(textRenderer, current,
                    getX() + optionX,
                    getY() + textOffsetY,
                    foregroundColor,
                    false);
        }
    }

    public Text getCurrent() {
        return current;
    }

    public void setCurrent(Text current) {
        this.current = current;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ButtonBuilder<Builder> {
        private Text option;

        public ClickListButtonWidget build() {
            return new ClickListButtonWidget(x, y, width, height, color, label, textColor, onClick, parent, option);
        }

        public Builder option(Text option) {
            this.option = option;
            return this;
        }
    }
}
