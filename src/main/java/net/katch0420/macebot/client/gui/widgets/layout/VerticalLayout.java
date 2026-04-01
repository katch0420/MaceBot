package net.katch0420.macebot.client.gui.widgets.layout;

import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;


public class VerticalLayout implements Layout {

    private int margin = 0;

    public VerticalLayout() {
    }

    public VerticalLayout margin(int margin) {
        this.margin = margin;
        return this;
    }

    @Override
    public void apply(ParentWidget parent, int width, int height) {
        int y = margin;

        for (ChildWidget child : parent.getChildren()) {
            child.setRelativePos(0, y);
            y += child.getHeight() + margin;
        }
    }

    @Override
    public int getContentHeight(ParentWidget parent, int width) {
        int total = margin;

        for (ChildWidget child : parent.getChildren()) {
            total += child.getHeight() + margin;
        }

        return total;
    }
}
