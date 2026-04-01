package net.katch0420.macebot.client.gui.widgets.panels;

import net.katch0420.macebot.client.gui.widgets.builder.LabelBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;
import net.katch0420.macebot.client.gui.widgets.layout.Layout;
import net.minecraft.client.gui.DrawContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PanelWidget extends ParentWidget {

    private final int color;
    private Layout layout;
    private boolean layoutDirty = false;

    protected PanelWidget(
            int x,
            int y,
            int width,
            int height,
            int color,
            ParentWidget parent,
            Layout layout
    ) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        this.color = color;
        this.layout = layout;

        if (parent != null) {
            setParent(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (color != 0x00000000) {
            context.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    color
            );
        }

        if (layout != null && layoutDirty) {
            layout.apply(this, getWidth(), getHeight());
            layoutDirty = false;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
        layout.apply(this, getWidth(), getHeight());
    }

    public Layout getLayout() {
        return layout;
    }

    public void markLayoutDirty(){
        layoutDirty = true;
    }

    @Override
    public void addDrawableChild(ChildWidget child) {
        super.addDrawableChild(child);
        markLayoutDirty();
    }

    @Override
    public void removeDrawableChild(ChildWidget child) {
        super.removeDrawableChild(child);
        markLayoutDirty();
    }

    @Override
    public void clearChildren() {
        super.clearChildren();
        markLayoutDirty();
    }

    @Override
    public ChildWidget setRelativePos(int x, int y) {
        markLayoutDirty();
        return super.setRelativePos(x, y);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        markLayoutDirty();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        markLayoutDirty();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        markLayoutDirty();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        markLayoutDirty();
    }

    public void setDynamicHeight() {
        if (layout == null) return;
        setHeight(layout.getContentHeight(this, getWidth()));
    }

    /** Sets width based on layout content (optional layouts only) */
    public void setDynamicWidth() {
        if (layout == null) return;
        setWidth(layout.getContentWidth(this, getHeight()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LabelBuilder<Builder> {

        private Layout layout;

        public Builder layout(Layout layout) {
            this.layout = layout;
            return this;
        }

        public PanelWidget build() {
            return new PanelWidget(
                    x,
                    y,
                    width,
                    height,
                    color,
                    parent,
                    layout
            );
        }
    }
}
