package net.katch0420.macebot.client.gui.widgets.layout;

import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;

public interface Layout {

    void apply(ParentWidget parent, int width, int height);

    /** Height actually used by children */
    default int getContentHeight(ParentWidget parent, int width) {
        return heightFallback(parent);
    }

    /** Width actually used by children (optional, future-proof) */
    default int getContentWidth(ParentWidget parent, int height) {
        return widthFallback(parent);
    }

    /* ===== Fallbacks (old behavior) ===== */

    private static int heightFallback(ParentWidget parent) {
        int h = 0;
        for (var child : parent.getChildren()) {
            h += child.getHeight();
        }
        return h;
    }

    private static int widthFallback(ParentWidget parent) {
        int w = 0;
        for (var child : parent.getChildren()) {
            w = Math.max(w, child.getWidth());
        }
        return w;
    }
}
