package net.katch0420.macebot.client.gui.widgets.layout;

import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;

public class FlowLayout implements Layout {

    private int itemWidth;
    private int itemHeight;
    private int margin;

    public FlowLayout(int itemWidth, int itemHeight, int margin) {
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.margin = margin;
    }

    @Override
    public void apply(ParentWidget parent, int width, int height) {
        int x = margin;
        int y = margin;
        int rowHeight = 0;

        for (ChildWidget child : parent.getChildren()) {
            child.setSize(itemWidth, itemHeight);

            if (x + itemWidth > width) {
                x = margin;
                y += rowHeight + margin;
                rowHeight = 0;
            }

            child.setRelativePos(x, y);

            x += itemWidth + margin;
            rowHeight = Math.max(rowHeight, itemHeight);
        }
    }

    @Override
    public int getContentHeight(ParentWidget parent, int width) {
        int x = margin;
        int y = margin;
        int rowHeight = 0;

        for (ChildWidget child : parent.getChildren()) {
            if (x + itemWidth > width) {
                x = margin;
                y += rowHeight + margin;
                rowHeight = 0;
            }

            rowHeight = Math.max(rowHeight, itemHeight);
            x += itemWidth + margin;
        }

        return y + rowHeight + margin;
    }
}
