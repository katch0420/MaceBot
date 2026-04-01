package net.katch0420.macebot.client.gui.widgets.buttons;

import net.katch0420.macebot.client.gui.widgets.builder.ButtonBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ButtonWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TexturedButtonWidget extends net.katch0420.macebot.client.gui.widgets.core.ButtonWidget {
    private final Identifier texture;
    private final Identifier texture_disabled;
    private final Text tooltip;
    private final Text label;
    private final int backgroundColor;
    private final int foregroundColor;
    private final int labelMargin;
    private final Supplier<Boolean> disableFunction;
    private boolean disabled;

    protected TexturedButtonWidget(int x, int y, int size, Identifier textureDisabled, Text tooltip, Text label,
                                   int backgroundColor, int foregroundColor,
                                   Consumer<ButtonWidget> onClick, ParentWidget parent,
                                   Identifier texture, int labelMargin, Supplier<Boolean> disableFunction, boolean disabled) {
        texture_disabled = textureDisabled;
        this.disableFunction = disableFunction;
        setX(x);
        setY(y);
        setWidth(size);
        setHeight(size + (label != null ? labelMargin + 10 : 0)); // 10 = text height approx
        setOnClick(onClick);
        this.tooltip = tooltip;
        this.label = label;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.texture = texture;
        this.labelMargin = labelMargin;
        this.disabled = disabled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!disabled) {
            if(super.mouseClicked(mouseX, mouseY, button)){
                updateDisableStatus();
                return true;
            }
        }
        updateDisableStatus();
        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = 0x00000000;

        if (isHovered()) {
            color += 0x80101010;
        }

        // Draw button background + texture
        Identifier finalTexture;
        if(disabled){
            finalTexture = texture_disabled;
        } else {
            finalTexture = texture;
        }
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getWidth(), color);
        context.drawTexture(
                RenderLayer::getGuiTextured,
                finalTexture,
                getX(),
                getY(),
                0, 0,
                getWidth(),
                getWidth(),
                getWidth(),
                getWidth()
        );

        if (label != null) {
            int textX = getX() + (getWidth() / 2) - (MinecraftClient.getInstance().textRenderer.getWidth(label) / 2);
            int textY = getY() + getWidth() + labelMargin;
            context.drawText(MinecraftClient.getInstance().textRenderer, label, textX, textY, foregroundColor, false);
        }

        // Tooltip only if enabled
        if (!disabled && isHovered() && tooltip != null) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX, mouseY);
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void updateDisableStatus(){
        setDisabled(disableFunction.get());
    }

    public static Builder builder() { return new Builder(); }

    public int getTextureHeight() {
        return getHeight() - (labelMargin * 2 + MinecraftClient.getInstance().textRenderer.fontHeight);
    }

    public static class Builder extends ButtonBuilder<Builder> {
        private Identifier texture;
        private Identifier texture_disabled;
        private Text tooltip;
        private int labelMargin = 4;
        private boolean disabled = false;
        private Supplier<Boolean> disableFunction;

        public TexturedButtonWidget build() {
            return new TexturedButtonWidget(x, y, width, texture_disabled, tooltip, label, color, textColor, onClick, parent, texture, labelMargin,disableFunction , disabled);
        }
        public Builder disableFunction(Supplier<Boolean> disableFunction){ this.disableFunction = disableFunction; return this; }
        public Builder texture(Identifier texture) { this.texture = texture; return this; }
        public Builder textureDisabled(Identifier texture_disabled) { this.texture_disabled = texture_disabled; return this; }
        public Builder tooltip(Text tooltip) { this.tooltip = tooltip; return this; }
        public Builder labelMargin(int margin) { this.labelMargin = margin; return this; }
        public Builder disabled(boolean disabled) { this.disabled = disabled; return this; }
    }
}
