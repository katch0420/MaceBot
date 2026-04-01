package net.katch0420.macebot.client.gui.widgets.layout;

import net.katch0420.macebot.client.gui.widgets.buttons.TexturedButtonWidget;
import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.gui.widgets.core.ParentWidget;

public class GridLayout implements Layout {

    private final ChildWidget[][] grid;
    private final int margin;

    public GridLayout(ChildWidget[][] grid, int margin) {
        this.grid   = grid;
        this.margin = margin;
    }

    @Override
    public void apply(ParentWidget parent, int width, int height) {
        if (grid == null || grid.length == 0) return;

        int rows = grid.length;
        int cols = grid[0].length;

        int cellW = width  / cols;
        int cellH = height / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                ChildWidget child = grid[r][c];
                if (child == null) continue;

                child.setParent(parent);

                int availW = cellW - margin * 2;
                int availH = cellH - margin * 2;

                int finalW, finalH;

                if (child instanceof TexturedButtonWidget tb) {
                    tb.updateDisableStatus();

                    int natW     = tb.getWidth();         // full widget width (== texture size)
                    int natH     = tb.getHeight();        // full widget height (texture + label)
                    int texH     = tb.getTextureHeight(); // just the texture square height

                    // Scale using full widget dimensions against available cell space
                    float scaleW = (float) availW / natW;
                    float scaleH = (float) availH / natH;
                    float scale = Math.min(scaleW, scaleH);

                    // Total height scales normally
                    finalH = (int)(natH * scale);

                    // Width must equal scaled texture height to keep texture square
                    finalW = (int)(texH * scale);

                    tb.setSize(finalW, finalH);

                } else {
                    int natW = child.getWidth();
                    int natH = child.getHeight();

                    if (natW <= 0 || natH <= 0) {
                        // No natural size — fill cell minus margin
                        finalW = availW;
                        finalH = availH;
                    } else {
                        int gapW = availW - natW;
                        int gapH = availH - natH;

                        if (gapW >= 0 && gapH >= 0) {
                            // Fits naturally — keep as-is
                            finalW = natW;
                            finalH = natH;
                        } else {
                            // Too big on at least one axis — scale down to tightest axis
                            float scale = Math.min(
                                    (float) availW / natW,
                                    (float) availH / natH);
                            finalW = (int)(natW * scale);
                            finalH = (int)(natH * scale);
                        }
                    }

                    child.setSize(finalW, finalH);
                }

                // Centre widget inside its cell
                int x = c * cellW + (cellW - finalW) / 2;
                int y = r * cellH + (cellH - finalH) / 2;
                child.setRelativePos(x, y);
            }
        }
    }

    @Override
    public int getContentHeight(ParentWidget parent, int width) {
        if (grid == null || grid.length == 0) return 0;
        int rows  = grid.length;
        int cellH = parent.getHeight() / rows;
        return rows * cellH;
    }
}