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
public class ClickToggleButtonWidget extends ButtonWidget {

    private final Text label;
    private final int backgroundColor;
    private final int foregroundColor;
    
    private boolean toggled;

    protected ClickToggleButtonWidget(int x, int y, int width, int height,
                                Text label, int backgroundColor, int foregroundColor,
                                Consumer<ButtonWidget> onClick, ParentWidget parent, boolean toggled) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);

        this.label = label;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;

        setOnClick(onClick);
        setParent(parent);

        this.toggled = toggled;
    }
    
    public boolean isToggled(){
        return toggled;
    }
    
    public void setToggled(boolean toggled){
        this.toggled = toggled;
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
        
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textOffsetY = (getHeight() - textRenderer.fontHeight) / 2;

        // Draw left label
        if (label != null) {
            context.drawText(textRenderer, label,
                    getX() + 4,
                    getY() + textOffsetY,
                    foregroundColor,
                    false);
        }
        
        int size = getHeight() / 2;
        int startY = getY() + size / 2;
        int startX = getX() + getWidth() - size - size / 2;

        drawToggleIcon(context, size, startX, startY);
    }

    private void drawToggleIcon(DrawContext context, int size, int startX, int startY) {
        //Outer Rectangle;
        int thickness = size / 8;
        if(thickness < 1) thickness = 1;
        context.fill(startX, startY, startX + size, startY + thickness, foregroundColor);
        context.fill(startX, startY + thickness, startX + thickness, startY + size, foregroundColor);
        context.fill(startX + thickness, startY + size - thickness, startX + size, startY + size, foregroundColor);
        context.fill(startX + size - thickness, startY + thickness, startX + size, startY + size - thickness, foregroundColor);
        
        //Inner Square
        if(toggled){
            context.fill(startX + 2 * thickness, startY + 2 * thickness, startX + size - 2 * thickness, startY + size - 2 * thickness, foregroundColor);
        }
    }

    @Override
    protected void handleClick(double mouseX, double mouseY) {
        toggled = !toggled;
        super.handleClick(mouseX, mouseY);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ButtonBuilder<Builder> {
        boolean toggled;
        public ClickToggleButtonWidget build() {
            return new ClickToggleButtonWidget(x, y, width, height, label, color, textColor, onClick, parent, toggled);
        }
        
        public Builder toggled(boolean bl){
            toggled = bl;
            return this;
        }
    }
}
