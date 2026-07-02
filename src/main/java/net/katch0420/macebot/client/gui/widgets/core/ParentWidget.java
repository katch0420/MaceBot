package net.katch0420.macebot.client.gui.widgets.core;

//? if >=1.21.9 {
/*import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
*///?}
import com.google.common.collect.Lists;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ParentWidget<P extends ParentWidget<P>> extends ChildWidget<P> implements ParentElement {

    private final List<Element> children = Lists.<Element>newArrayList();
    private final List<Drawable> drawables = Lists.<Drawable>newArrayList();

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        this.drawables.add(drawableElement);
        return this.addSelectableChild(drawableElement);
    }

    protected <T extends Drawable> T addDrawable(T drawable) {
        this.drawables.add(drawable);
        return drawable;
    }

    protected <T extends Element & Selectable> T addSelectableChild(T child) {
        this.children.add(child);
        return child;
    }

    protected void remove(Element child) {
        if (child instanceof Drawable) {
            this.drawables.remove((Drawable)child);
        }
        this.children.remove(child);
    }

    protected void clearChildren() {
        this.drawables.clear();
        this.children.clear();
    }

    @Nullable
    private Element focused;
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
    public Element getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
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
        for (Element child : children) {
            if(child instanceof BaseWidget<?> c) c.tick();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Drawable child : drawables) {
            child.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (Element child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
    }
}