package net.katch0420.macebot.client.gui.widgets.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ButtonWidget extends ChildWidget {

    private boolean visible = true;
    private boolean active = true;
    private boolean hovered;
    private boolean focused;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = this.client.textRenderer;

    private Consumer<ButtonWidget> onClick;
    private final TooltipState tooltip = new TooltipState();

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        hovered = context.scissorContains(mouseX, mouseY)
                && mouseX >= getX()
                && mouseY >= getY()
                && mouseX < getX() + width
                && mouseY < getY() + height;

        renderWidget(context, mouseX, mouseY, delta);
    }


    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override in subclasses to draw the actual button
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip.setTooltip(tooltip);
    }

    @Nullable
    public Tooltip getTooltip() {
        return tooltip.getTooltip();
    }

    public void setTooltipDelay(Duration tooltipDelay) {
        tooltip.setDelay(tooltipDelay);
    }

    public void setOnClick(Consumer<ButtonWidget> onClick) {
        this.onClick = onClick;
    }

    protected void handleClick(double mouseX, double mouseY) {
        if (onClick != null) {
            onClick.accept(this);
        }
    }

    protected void handleRelease(double mouseX, double mouseY) {
        // Override if needed
    }

    protected void handleDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Override if needed
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible) return false;
        if (!isValidClickButton(button)) return false;

        if (clicked(mouseX, mouseY)) {
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            handleClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            handleRelease(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isValidClickButton(button)) {
            handleDrag(mouseX, mouseY, deltaX, deltaY);
            return true;
        }
        return false;
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    protected boolean clicked(double mouseX, double mouseY) {
        return active && visible
                && mouseX >= getX()
                && mouseY >= getY()
                && mouseX < getX() + getWidth()
                && mouseY < getY() + getHeight();
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (!active || !visible) return null;
        return !isFocused() ? GuiNavigationPath.of(this) : null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible
                && mouseX >= getX()
                && mouseY >= getY()
                && mouseX < getX() + width
                && mouseY < getY() + height;
    }

    public void playDownSound(SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isSelected() {
        return hovered || focused;
    }

    @Override
    public Selectable.SelectionType getType() {
        if (focused) return Selectable.SelectionType.FOCUSED;
        return hovered ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
    }

    // Getters and setters for visibility/active if needed
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
