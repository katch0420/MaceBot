package net.katch0420.macebot.client.gui.widgets.core;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ParentWidget<P extends ParentWidget<P>> extends ChildWidget<P> implements ContainerEventHandler {

    private final List<GuiEventListener> children = Lists.<GuiEventListener>newArrayList();
    private final List<Renderable> drawables = Lists.<Renderable>newArrayList();

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double d, double e) {
        return ContainerEventHandler.super.getChildAt(d, e);
    }

    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addDrawableChild(T drawableElement) {
        this.drawables.add(drawableElement);
        return this.addSelectableChild(drawableElement);
    }

    protected <T extends Renderable> T addDrawable(T drawable) {
        this.drawables.add(drawable);
        return drawable;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addSelectableChild(T child) {
        this.children.add(child);
        return child;
    }

    protected void remove(GuiEventListener child) {
        if (child instanceof Renderable) {
            this.drawables.remove((Renderable)child);
        }
        this.children.remove(child);
    }

    protected void clearChildren() {
        this.drawables.clear();
        this.children.clear();
    }

    @Nullable
    private GuiEventListener focused;
    private boolean dragging;

    @Override
    public final boolean isDragging() {
        return this.dragging;
    }

    @Override
    public final void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = focused;
    }

    @Override
    public void tick() {
        for (GuiEventListener child : children) {
            if(child instanceof BaseWidget<?> c) c.tick();
        }
    }
    //? if <=1.21.11 {
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        for (Renderable child : drawables) {
            child.render(context, mouseX, mouseY, delta);
        }
    }
    //?} else {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        for (Renderable child : drawables) {
            child.extractRenderState(context, mouseX, mouseY, delta);
        }
    }
    *///?}

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (GuiEventListener child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
    }
}