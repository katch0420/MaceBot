package net.katch0420.macebot.client.gui.widgets.buttons;

import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.function.IntConsumer;
import org.lwjgl.glfw.GLFW;

/** Compact integer slider used by setting rows. */
public final class IntegerSliderButton extends Button {
    private final int min;
    private final int max;
    private final int step;
    private final IntConsumer onValueChanged;
    private final Theme theme;
    private int value;

    public IntegerSliderButton(int x, int y, int width, int height, int min, int max, int step,
                               int value, Theme theme, IntConsumer onValueChanged) {
        super(x, y, width, height, 0, theme.body_button_foreground(), 0, 0,
                theme.body_button_border(), null, null, () -> true,
                new Data(FunctionType.CLICK, RenderType.CLASSIC, CaptionMode.TOOLTIP, false,
                        new HashMap<>(), new HashMap<>()));
        if (min > max || step < 1) throw new IllegalArgumentException("Invalid slider range");
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = clamp(value);
        this.theme = theme;
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(x, y, x + width, y + height, theme.body_button_background());
        graphics.fill(x, y + height - 3, x + width, y + height - 1, theme.panel_separator());
        int trackWidth = Math.max(1, width - 8);
        long range = Math.max(1L, (long) max - min);
        int thumbX = x + 4 + (int) (((long) value - min) * trackWidth / range);
        graphics.fill(x + 4, y + height - 3, thumbX, y + height - 1, theme.accent());
        graphics.fill(thumbX - 1, y + height - 5, thumbX + 2, y + height - 1, theme.accent());
        Component label = Component.literal(Integer.toString(value));
        float scale = Math.min(1.0f, Math.min(Math.max(1, width - 4) / (float) Math.max(1, textRenderer.width(label)),
                Math.max(1, height - 5) / (float) textRenderer.lineHeight));
        YarnHelpers.pushMatrix(graphics);
        //? if >=1.21.6 {
        graphics.pose().translate(x + width / 2.0f, y + (height - 3) / 2.0f);
        //?} else
        //graphics.pose().translate(x + width / 2.0f, y + (height - 3) / 2.0f, 0.0f);
        //? if >=1.21.6 {
        graphics.pose().scale(scale, scale);
        //?} else
        //graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawCenteredString(textRenderer, label, 0, -textRenderer.lineHeight / 2,
                theme.body_button_foreground());
        YarnHelpers.popMatrix(graphics);
        if (borderColor != 0) {
            net.katch0420.macebot.client.utils.RenderUtils.drawBorder(graphics, x, y, width, height,
                    active ? borderColor : net.katch0420.macebot.client.utils.ColorHelper.darken(borderColor, 0.6f));
        }
    }

    @Override
    protected void handleClick(double mouseX, double mouseY) {
        updateFromMouse(mouseX);
    }

    @Override
    protected void handleDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        updateFromMouse(mouseX);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }

    private void updateFromMouse(double mouseX) {
        int trackWidth = Math.max(1, width - 8);
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - (x + 4.0)) / trackWidth));
        long range = (long) max - min;
        long steps = Math.round(progress * range / step);
        int next = clamp((long) min + steps * step);
        if (next == value) return;
        value = next;
        onValueChanged.accept(value);
    }

    private int clamp(int candidate) {
        return clamp((long) candidate);
    }

    private int clamp(long candidate) {
        return (int) Math.max(min, Math.min(max, candidate));
    }
}
