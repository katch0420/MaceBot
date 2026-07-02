package net.katch0420.macebot.client.gui.widgets.builder;

import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;

public class ChildBuilder<T extends ChildBuilder<T>> extends Builder<T>{

    protected ParentWidget parent;
    @SuppressWarnings("unchecked")
    public T parent(ParentWidget parent){
        System.out.println("did someone call me and gave: " + parent);
        this.parent = parent;
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public T relativePos(int x, int y){
        if(parent != null) {
            this.x = parent.getX() + x;
            this.y = parent.getY() + y;
            return (T) this;
        }
        return position(x, y);
    }
}
