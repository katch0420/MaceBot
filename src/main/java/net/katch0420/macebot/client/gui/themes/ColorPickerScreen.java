package net.katch0420.macebot.client.gui.themes;

import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.function.IntConsumer;

/**
 * A popup overlay (rendered on top of its parent screen) for editing a single
 * ARGB color value using a classic saturation/value square + hue strip,
 * plus live R/G/B/A and hex text fields.
 *
 * Changes are applied live via {@code onChange} as the user drags the picker
 * or edits a field, so the parent screen's preview updates immediately.
 * "Done" persists the change (via {@code onApply}); "Cancel" reverts to the
 * original color.
 */
public class ColorPickerScreen extends Screen {

    private static final int SQUARE_SIZE = 120;
    private static final int HUE_WIDTH = 18;
    private static final int GAP = 14;
    private static final int FIELD_WIDTH = 70;
    private static final int FIELD_HEIGHT = 18;
    private static final int PANEL_PAD = 18;

    private final Screen parent;
    private final int originalColor;
    private final IntConsumer onChange;
    private final Runnable onApply;

    private int currentColor;
    private float hue, sat, val;
    private int alpha;

    private TextFieldWidget rField, gField, bField, aField, hexField;

    private int squareX, squareY;
    private int hueX, hueY, hueH;

    private boolean draggingSquare, draggingHue;
    private boolean suppressFieldCallback;

    public ColorPickerScreen(Screen parent, Text title, int initialColor, IntConsumer onChange, Runnable onApply) {
        super(title);
        this.parent = parent;
        this.originalColor = initialColor;
        this.currentColor = initialColor;
        this.onChange = onChange;
        this.onApply = onApply;
        unpackToHsv(initialColor);
    }

    // ----------------------------------------------------------------
    // Color math
    // ----------------------------------------------------------------

    private void unpackToHsv(int color) {
        alpha = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float[] hsv = Color.RGBtoHSB(r, g, b, null);
        hue = hsv[0];
        sat = hsv[1];
        val = hsv[2];
    }

    private void recomputeFromHsv() {
        int rgb = Color.HSBtoRGB(hue, sat, val) & 0xFFFFFF;
        currentColor = (alpha << 24) | rgb;
    }

    // ----------------------------------------------------------------
    // Layout
    // ----------------------------------------------------------------

    @Override
    protected void init() {
        int panelWidth = SQUARE_SIZE + HUE_WIDTH + GAP * 3 + FIELD_WIDTH;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = this.height / 2 - 110;

        squareX = panelX + PANEL_PAD;
        squareY = panelY + 40;

        hueX = squareX + SQUARE_SIZE + GAP;
        hueY = squareY;
        hueH = SQUARE_SIZE;

        int fieldX = hueX + HUE_WIDTH + GAP;
        int fieldY = squareY;

        rField = channelField(fieldX, fieldY, "R", (currentColor >> 16) & 0xFF, v -> setChannel(0, v));
        gField = channelField(fieldX, fieldY + 24, "G", (currentColor >> 8) & 0xFF, v -> setChannel(1, v));
        bField = channelField(fieldX, fieldY + 48, "B", currentColor & 0xFF, v -> setChannel(2, v));
        aField = channelField(fieldX, fieldY + 72, "A", alpha, v -> setChannel(3, v));

        hexField = new TextFieldWidget(this.textRenderer, fieldX, fieldY + 104, FIELD_WIDTH, FIELD_HEIGHT, Text.literal("Hex"));
        hexField.setMaxLength(8);
        hexField.setText(String.format("%08X", currentColor));
        hexField.setChangedListener(this::onHexChanged);
        this.addDrawableChild(hexField);

        int buttonsY = squareY + SQUARE_SIZE + 34;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            onApply.run();
            this.client.setScreen(parent);
        }).dimensions(squareX, buttonsY, 105, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            onChange.accept(originalColor);
            this.client.setScreen(parent);
        }).dimensions(squareX + 115, buttonsY, 105, 20).build());
    }

    private TextFieldWidget channelField(int x, int y, String label, int initial, IntConsumer apply) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x + 16, y, FIELD_WIDTH - 16, FIELD_HEIGHT, Text.literal(label));
        field.setMaxLength(3);
        field.setText(String.valueOf(initial));
        field.setChangedListener(s -> {
            if (suppressFieldCallback) return;
            try {
                int v = Math.max(0, Math.min(255, Integer.parseInt(s.trim())));
                apply.accept(v);
            } catch (NumberFormatException ignored) {
                // wait for a valid number
            }
        });
        this.addDrawableChild(field);
        return field;
    }

    // ----------------------------------------------------------------
    // Field <-> color sync
    // ----------------------------------------------------------------

    private void setChannel(int channel, int value) {
        int r = (currentColor >> 16) & 0xFF;
        int g = (currentColor >> 8) & 0xFF;
        int b = currentColor & 0xFF;
        switch (channel) {
            case 0 -> r = value;
            case 1 -> g = value;
            case 2 -> b = value;
            case 3 -> alpha = value;
        }
        currentColor = (alpha << 24) | (r << 16) | (g << 8) | b;
        unpackToHsv(currentColor);
        syncHexOnly();
        onChange.accept(currentColor);
    }

    private void onHexChanged(String text) {
        if (suppressFieldCallback) return;
        String cleaned = text.replace("#", "").trim();
        if (cleaned.length() != 8) return;
        int parsed;
        try {
            parsed = (int) Long.parseLong(cleaned, 16);
        } catch (NumberFormatException e) {
            return;
        }
        currentColor = parsed;
        alpha = (currentColor >> 24) & 0xFF;
        unpackToHsv(currentColor);
        syncAllFields();
        onChange.accept(currentColor);
    }

    private void syncHexOnly() {
        suppressFieldCallback = true;
        hexField.setText(String.format("%08X", currentColor));
        suppressFieldCallback = false;
    }

    private void syncAllFields() {
        suppressFieldCallback = true;
        rField.setText(String.valueOf((currentColor >> 16) & 0xFF));
        gField.setText(String.valueOf((currentColor >> 8) & 0xFF));
        bField.setText(String.valueOf(currentColor & 0xFF));
        aField.setText(String.valueOf(alpha));
        hexField.setText(String.format("%08X", currentColor));
        suppressFieldCallback = false;
    }

    // ----------------------------------------------------------------
    // Mouse interaction (SV square + hue strip)
    // ----------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (inSquare(mx, my)) {
                draggingSquare = true;
                updateFromSquare(mx, my);
                return true;
            }
            if (inHue(mx, my)) {
                draggingHue = true;
                updateFromHue(my);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingSquare) {
            updateFromSquare(mx, my);
            return true;
        }
        if (draggingHue) {
            updateFromHue(my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingSquare = false;
        draggingHue = false;
        return super.mouseReleased(mx, my, button);
    }

    private boolean inSquare(double mx, double my) {
        return mx >= squareX && mx <= squareX + SQUARE_SIZE && my >= squareY && my <= squareY + SQUARE_SIZE;
    }

    private boolean inHue(double mx, double my) {
        return mx >= hueX && mx <= hueX + HUE_WIDTH && my >= hueY && my <= hueY + hueH;
    }

    private void updateFromSquare(double mx, double my) {
        sat = clamp01((float) ((mx - squareX) / SQUARE_SIZE));
        val = clamp01((float) (1 - (my - squareY) / SQUARE_SIZE));
        recomputeFromHsv();
        syncAllFields();
        onChange.accept(currentColor);
    }

    private void updateFromHue(double my) {
        hue = clamp01((float) ((my - hueY) / hueH));
        recomputeFromHsv();
        syncAllFields();
        onChange.accept(currentColor);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    // ----------------------------------------------------------------
    // Rendering
    // ----------------------------------------------------------------

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.render(context, -1, -1, delta);
        }

        YarnHelpers.pushMatrix(context);
        context.getMatrices().translate(0,0,100f);

        int panelWidth = SQUARE_SIZE + HUE_WIDTH + GAP * 3 + FIELD_WIDTH + PANEL_PAD * 2;
        int panelHeight = SQUARE_SIZE + 110;
        int panelX = squareX - PANEL_PAD;
        int panelY = squareY - 40;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE615171B);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF3D7BB8);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, panelX + panelWidth / 2, panelY + 10, 0xFFFFFFFF);

        drawSvSquare(context);
        drawHueStrip(context);
        drawFieldLabels(context);
        drawPreviewSwatch(context);

        super.render(context, mouseX, mouseY, delta);
        YarnHelpers.popMatrix(context);
    }

    private void drawSvSquare(DrawContext context) {
        final int steps = 24;
        final float stepSize = (float) SQUARE_SIZE / steps;
        for (int sx = 0; sx < steps; sx++) {
            for (int sy = 0; sy < steps; sy++) {
                float s = sx / (float) (steps - 1);
                float v = 1f - sy / (float) (steps - 1);
                int rgb = Color.HSBtoRGB(hue, s, v) | 0xFF000000;
                int x0 = squareX + Math.round(sx * stepSize);
                int y0 = squareY + Math.round(sy * stepSize);
                int x1 = squareX + Math.round((sx + 1) * stepSize);
                int y1 = squareY + Math.round((sy + 1) * stepSize);
                context.fill(x0, y0, x1, y1, rgb);
            }
        }
        context.drawBorder(squareX, squareY, SQUARE_SIZE, SQUARE_SIZE, 0xFFFFFFFF);

        int cursorX = squareX + Math.round(sat * SQUARE_SIZE);
        int cursorY = squareY + Math.round((1 - val) * SQUARE_SIZE);
        drawRingCursor(context, cursorX, cursorY);
    }

    private void drawRingCursor(DrawContext context, int cx, int cy) {
        context.fill(cx - 4, cy - 1, cx + 4, cy + 1, 0xFF000000);
        context.fill(cx - 1, cy - 4, cx + 1, cy + 4, 0xFF000000);
        context.fill(cx - 3, cy, cx - 1, cy + 1, 0xFFFFFFFF);
        context.fill(cx + 1, cy, cx + 3, cy + 1, 0xFFFFFFFF);
        context.fill(cx, cy - 3, cx + 1, cy - 1, 0xFFFFFFFF);
        context.fill(cx, cy + 1, cx + 1, cy + 3, 0xFFFFFFFF);
    }

    private void drawHueStrip(DrawContext context) {
        final int steps = 18;
        final float stepSize = (float) hueH / steps;
        for (int i = 0; i < steps; i++) {
            float h = i / (float) (steps - 1);
            int rgb = Color.HSBtoRGB(h, 1f, 1f) | 0xFF000000;
            int y0 = hueY + Math.round(i * stepSize);
            int y1 = hueY + Math.round((i + 1) * stepSize);
            context.fill(hueX, y0, hueX + HUE_WIDTH, y1, rgb);
        }
        context.drawBorder(hueX, hueY, HUE_WIDTH, hueH, 0xFFFFFFFF);

        int markerY = hueY + Math.round(hue * hueH);
        context.fill(hueX - 3, markerY - 1, hueX + HUE_WIDTH + 3, markerY + 1, 0xFFFFFFFF);
        context.fill(hueX - 3, markerY - 1, hueX + HUE_WIDTH + 3, markerY, 0xFF000000);
    }

    private void drawFieldLabels(DrawContext context) {
        int labelX = hueX + HUE_WIDTH + GAP;
        context.drawTextWithShadow(this.textRenderer, "R", labelX, squareY + 5, 0xFFE7E9EC);
        context.drawTextWithShadow(this.textRenderer, "G", labelX, squareY + 29, 0xFFE7E9EC);
        context.drawTextWithShadow(this.textRenderer, "B", labelX, squareY + 53, 0xFFE7E9EC);
        context.drawTextWithShadow(this.textRenderer, "A", labelX, squareY + 77, 0xFFE7E9EC);
        context.drawTextWithShadow(this.textRenderer, "#", labelX, squareY + 109, 0xFFE7E9EC);
    }

    private void drawPreviewSwatch(DrawContext context) {
        int size = 22;
        int x = squareX + SQUARE_SIZE - size;
        int y = squareY + SQUARE_SIZE + 6;
        drawCheckerboard(context, x, y, size, size);
        context.fill(x, y, x + size, y + size, currentColor);
        context.drawBorder(x, y, size, size, 0xFFFFFFFF);
    }

    private void drawCheckerboard(DrawContext context, int x, int y, int w, int h) {
        int cell = 4;
        for (int cy = 0; cy < h; cy += cell) {
            for (int cx = 0; cx < w; cx += cell) {
                boolean light = ((cx / cell) + (cy / cell)) % 2 == 0;
                int color = light ? 0xFFAAAAAA : 0xFF777777;
                context.fill(x + cx, y + cy, Math.min(x + cx + cell, x + w), Math.min(y + cy + cell, y + h), color);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Force the user through Done/Cancel so the live preview state stays consistent
        return false;
    }
}
