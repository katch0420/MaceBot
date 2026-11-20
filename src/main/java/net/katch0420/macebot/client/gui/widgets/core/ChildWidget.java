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
}
