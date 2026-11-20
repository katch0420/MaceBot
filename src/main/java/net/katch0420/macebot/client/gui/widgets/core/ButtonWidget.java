package net.katch0420.macebot.client.gui.widgets.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
//? if >=1.21.9
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ButtonWidget<T extends ButtonWidget<T>> extends ChildWidget<ButtonWidget<T>> {

    protected boolean visible = true;
    protected boolean active = true;
    protected boolean hovered;
    protected boolean held;
    protected boolean focused;

    protected int backgroundColor;
    protected int foregroundColor;
    protected int holdColor;
    protected int hoverColor;
    protected int borderColor = 0;

    public final Minecraft client = Minecraft.getInstance();
    public final Font textRenderer = this.client.font;

    private Consumer<T> onClick;
    private Consumer<T> onRightClick;
    private Consumer<T> onKeyActivate;

    public Supplier<Boolean> activeSupplier;
    private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

    /** Which mouse button THIS widget is currently tracking as held, or -1 if none. */
    private int heldButton = -1;

    public ButtonWidget(int backgroundColor, int foregroundColor, int holdColor, int hoverColor, int borderColor, Tooltip tooltip, Consumer<T> onClick, Supplier<Boolean> activeSupplier) {
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.holdColor = holdColor;
        this.hoverColor = hoverColor;
        this.borderColor = borderColor;
        this.activeSupplier = activeSupplier != null ? activeSupplier : () -> true;
        setTooltip(tooltip);
        setOnClick(onClick);
    }

    @Override
    //? if <=1.21.11 {
    public final void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    //?} else
    //public final void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        hovered = context.containsPointInScissor(mouseX, mouseY) && mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

        // Defense-in-depth on top of the routing fix below: if this widget
        // still thinks it's held but the real hardware button isn't down
        // anymore (e.g. focus got stolen, screen swapped mid-drag), self-heal
        // instead of staying visually stuck.
        if (held && heldButton >= 0 && !isHardwareButtonDown(heldButton)) {
            held = false;
            heldButton = -1;
            handleRelease(mouseX, mouseY);
        }

        renderWidget(context, mouseX, mouseY, delta);
    }

    private boolean isHardwareButtonDown(int button) {
        long handle = client.getWindow()
                //? if >=1.21.9 {
                .handle();
                //?} else
                //.getWindow();
        return GLFW.glfwGetMouseButton(handle, button) == GLFW.GLFW_PRESS;
    }

    protected void renderWidget(
            //? if <=1.21.11 {
            GuiGraphics
            //?} else
            //GuiGraphicsExtractor
                    context, int mouseX, int mouseY, float delta) {
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip.set(tooltip);
    }

    @Nullable
    public Tooltip getTooltip() {
        return tooltip.get();
    }

    public void setTooltipDelay(Duration tooltipDelay) {
        tooltip.setDelay(tooltipDelay);
    }

    public void setOnClick(Consumer<T> onClick) {
        this.onClick = onClick;
    }

    public void setOnRightClick(Consumer<T> onRightClick) {
        this.onRightClick = onRightClick;
    }

    public void setOnKeyActivate(Consumer<T> onKeyActivate) {
        this.onKeyActivate = onKeyActivate;
    }

    @SuppressWarnings("unchecked")
    protected void handleClick(double mouseX, double mouseY) {
        if (onClick != null) {
            onClick.accept((T)this);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleRightClick(double mouseX, double mouseY) {
        if (onRightClick != null) {
            onRightClick.accept((T)this);
        }
    }

    @SuppressWarnings("unchecked")
    public T setActiveSupplier(Supplier<Boolean> supplier){
        activeSupplier = supplier;
        return (T) this;
    }

    public void handleActive() {
        active = activeSupplier.get();
        if (!active && held) {
            held = false;
            heldButton = -1;
        }
    }

    protected void handleRelease(double mouseX, double mouseY) {
        // Override if needed
    }

    protected void handleDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Override if needed
    }

    @Override
    public boolean mouseClicked(
            //? if >=1.21.9 {
            MouseButtonEvent c, boolean bl
            //?} else
            //double mx, double my, int btn
    ) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int btn = c.button();
        if (!active || !visible) return false;
        if (!isValidClickButton(btn)) return false;

        if (clicked(mx, my)) {
            held = true;
            heldButton = btn;
            playDownSound(Minecraft.getInstance().getSoundManager());
            if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                handleRightClick(mx, my);
            } else {
                handleClick(mx, my);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(
            //? if >=1.21.9 {
            MouseButtonEvent c
            //?} else
            //double mx, double my, int btn
    ) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int btn = c.button();
        if (isValidClickButton(btn) && heldButton == btn) {
            held = false;
            heldButton = -1;
            if(isMouseOver(mx,my)) {
                handleRelease(mx, my);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(
            //? if >=1.21.9 {
            MouseButtonEvent c
            //?} else
            //double mx, double my, int btn
            , double dx, double dy) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int btn = c.button();
        // Same fix as mouseReleased: only react to drag if this widget is
        // actually the one being held, otherwise every drag over a container
        // would get silently swallowed by whichever child sits first.
        if (isValidClickButton(btn) && heldButton == btn) {
            handleDrag(mx, my, dx, dy);
            return true;
        }
        return false;
    }

    /**
     * Left-click drives {@link #handleClick}; right-click now drives
     * {@link #handleRightClick}. Override to restrict/widen further.
     */
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    protected boolean clicked(double mouseX, double mouseY) {
        return active && visible && mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
    }

    @SuppressWarnings("unchecked")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || !focused) return false;
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            held = true;
            heldButton = GLFW.GLFW_MOUSE_BUTTON_LEFT;
            playDownSound(Minecraft.getInstance().getSoundManager());
            if (onKeyActivate != null) {
                onKeyActivate.accept((T)this);
            } else {
                handleClick(getX() + (double) getWidth() / 2, getY() + (double) getHeight() / 2);
            }
            return true;
        }
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            held = false;
            heldButton = -1;
            return true;
        }
        return false;
    }


    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (!active || !visible) return null;
        return !isFocused() ? ComponentPath.leaf(this) : null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
    }

    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (!focused && held) {
            held = false;
            heldButton = -1;
        }
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isHeld() {
        return held;
    }

    public boolean isSelected() {
        return hovered || focused;
    }


    @Override
    public @NotNull NarrationPriority narrationPriority() {
        if (focused) return NarrationPriority.FOCUSED;
        return hovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
    }

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
        if (!active && held) {
            held = false;
            heldButton = -1;
        }
    }

    @Override
    public void tick() {
        handleActive();
    }
}