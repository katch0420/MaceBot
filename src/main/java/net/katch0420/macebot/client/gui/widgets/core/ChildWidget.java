package net.katch0420.macebot.client.gui.widgets.core;

public class ChildWidget extends BaseWidget {

    public ParentWidget parent;

    public ChildWidget setRelativePos(int x, int y){
        if(parent != null){
            setX(parent.getX() + x);
            setY(parent.getY() + y);
            return this;
        }
        setX(x);
        setY(y);
        return this;
    }

    public ChildWidget setParent(ParentWidget parent){
        if(parent == null) return this;
        this.parent = parent;
        parent.addDrawableChild(this);
        return this;
    }
}
