package net.katch0420.macebot.client.gui.widgets;

import net.katch0420.macebot.client.gui.widgets.builder.ChildBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;

public class EmptyWidget extends ChildWidget {
    public EmptyWidget(int x, int y, int width, int height, ParentWidget parent) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setParent(parent);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder extends ChildBuilder<Builder> {
        public EmptyWidget build(){
            return new EmptyWidget(x, y, width, height, parent);
        }
    }
}
