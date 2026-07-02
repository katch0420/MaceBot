package net.katch0420.macebot.client.gui.widgets.core;

public class ChildWidget<T extends ChildWidget<T>> extends BaseWidget<T> {

    public ParentWidget<?> parent;

    @SuppressWarnings("unchecked")
    public T setRelativePos(int x, int y){
        if(parent != null){
            setX(parent.getX() + x);
            setY(parent.getY() + y);
            return (T) this;
        }
        setX(x);
        setY(y);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setParent(ParentWidget<?> parent){
        if(parent == null) return (T) this;
        this.parent = parent;
        parent.addDrawableChild(this);
        return (T) this;
    }

    protected boolean onMouseClick(double mx, double my, int btn) {
        return false;
    }

    protected boolean onMouseRelease(double mx, double my, int btn) {
        return false;
    }

    protected boolean onMouseDrag(double mx, double my, int btn, double dx, double dy) {
        return false;
    }

    protected boolean onCharTyped(char chr, int modifiers) {
        return false;
    }

    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}
