package net.katch0420.macebot.client.gui.widgets.core;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BaseWidget<T extends BaseWidget<T>> implements LayoutElement, Renderable, NarratableEntry, GuiEventListener {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @SuppressWarnings("unchecked")
    public T setWidth(int width) {
        this.width = width;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setHeight(int height){
        this.height = height;
        return (T) this;

    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {

    }

    public void setSize(int width, int height){
        setWidth(width);
        setHeight(height);
    }
    public void tick(){}

    //? if <=1.21.11 {
    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
    }
    //?} else {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}
    *///?}

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }


}
