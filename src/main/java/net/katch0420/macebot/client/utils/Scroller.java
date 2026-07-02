package net.katch0420.macebot.client.utils;

import net.minecraft.client.gui.DrawContext;

/**
 * Reusable vertical scrollbar + scroll-offset tracker.
 *
 * Usage:
 *   // Create once per panel that needs scrolling
 *   Scroller scroller = new Scroller();
 *
 *   // Call in init / whenever content size changes:
 *   scroller.setArea(listX, listY, listH, contentTotalH);
 *
 *   // In render:
 *   scroller.render(ctx, mx, my, accentColor, trackColor);
 *
 *   // In mouseClicked / mouseReleased / mouseDragged / mouseScrolled:
 *   scroller.mouseClicked(mx, my);
 *   scroller.mouseReleased();
 *   scroller.mouseDragged(my);
 *   scroller.mouseScrolled(mx, my, amount);
 *
 *   // When laying out items, offset each item's Y by:
 *   int itemY = listY + index * itemH - scroller.getOffset();
 */
public class Scroller {

    // Area
    private int listX, listY, listH;
    private int contentH;   // total pixel height of all content

    // Thumb
    private int thumbH;
    private int thumbY;     // Y of thumb top, relative to listY
    private boolean dragging;
    private double dragStartLocalY; // mouse Y when drag started, relative to listY
    private int    dragStartThumbY;

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * @param listX    X of the scroll area (used for mouseScrolled hit-test)
     * @param listX    X of the scroll area (used for mouseScrolled hit-test)
     * @param listY    Y of the scroll area
     * @param listH    Visible height of the scroll area
     * @param contentH Total pixel height of all content (items × item height)
     */
    public void setArea(int listX, int listY, int listH, int contentH) {
        this.listX   = listX;
        this.listY   = listY;
        this.listH   = listH;
        this.contentH = Math.max(1, contentH);

        // Thumb height proportional to visible/total ratio, min 16px
        thumbH = Math.max(16, listH * listH / this.contentH);
        thumbH = Math.min(thumbH, listH);

        // Clamp existing thumb position
        thumbY = Math.max(0, Math.min(thumbY, listH - thumbH));
    }

    // ── Scroll offset ─────────────────────────────────────────────────────────

    /** How many pixels of content are scrolled past the top. */
    public int getOffset() {
        if (listH >= contentH) return 0;
        int scrollRange = contentH - listH;
        int thumbRange  = listH    - thumbH;
        if (thumbRange <= 0) return 0;
        return (int) ((float) thumbY / thumbRange * scrollRange);
    }

    public boolean needsScrollbar() {
        return contentH > listH;
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    /** Call from mouseClicked. Returns true if the scrollbar consumed the click. */
    public boolean mouseClicked(double mx, double my, int barX, int barW) {
        if (mx < barX || mx >= barX + barW) return false;
        if (my < listY || my >= listY + listH) return false;

        dragging       = true;
        dragStartLocalY = my - listY;
        dragStartThumbY = thumbY;

        // If click is outside thumb, jump thumb to mouse
        double localY = my - listY;
        if (localY < thumbY || localY >= thumbY + thumbH) {
            setThumbY((int)(localY - thumbH / 2.0));
        }
        return true;
    }

    /**
     * Call from mouseReleased.
     *
     * @return
     */
    public boolean mouseReleased() {
        dragging = false;
        return false;
    }

    /** Call from mouseDragged. Returns true if currently dragging. */
    public boolean mouseDragged(double my) {
        if (!dragging) return false;
        double delta = (my - listY) - dragStartLocalY;
        setThumbY((int)(dragStartThumbY + delta));
        return true;
    }

    /**
     * Call from mouseScrolled.
     * @param listX     Left edge of the scrollable list area
     * @param listW     Width of the scrollable list area (hit-test)
     * @param amount    verticalAmount from mouseScrolled (positive = scroll up)
     * @param lineH     Pixels per scroll "click"
     */
    public boolean mouseScrolled(double mx, double my, int listX, int listW, double amount, int lineH) {
        if (mx < listX || mx >= listX + listW) return false;
        if (my < listY || my >= listY + listH) return false;
        // amount > 0 means scroll up → move content up → decrease offset → decrease thumbY
        scrollByPixels((int)(-amount * lineH));
        return true;
    }

    public void scrollByPixels(int pixels) {
        if (contentH <= listH) return;
        int scrollRange = contentH - listH;
        int thumbRange  = listH    - thumbH;
        if (thumbRange <= 0) return;
        // Convert pixel delta in content space to thumb space
        float thumbDelta = (float) pixels / scrollRange * thumbRange;
        setThumbY(thumbY + (int) thumbDelta);
    }

    private void setThumbY(int y) {
        thumbY = Math.max(0, Math.min(y, listH - thumbH));
    }

    // ── Render ────────────────────────────────────────────────────────────────

    /**
     * Renders the scrollbar at the given X position.
     * @param barX       X of the scrollbar track
     * @param barW       Width of the track (typically 3-5px)
     * @param trackColor ARGB track color
     * @param thumbColor ARGB thumb color
     */
    public void render(DrawContext c, int barX, int barW, int trackColor, int thumbColor) {
        if (!needsScrollbar()) return;
        c.fill(barX, listY, barX + barW, listY + listH, trackColor);
        c.fill(barX, listY + thumbY, barX + barW, listY + thumbY + thumbH, thumbColor);
    }
}