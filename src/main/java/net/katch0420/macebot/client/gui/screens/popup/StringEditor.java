package net.katch0420.macebot.client.gui.screens.popup;

import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class StringEditor extends PopupScreen {

    private final String           initialVal;
    private final Consumer<String> onConfirm;

    // ── Layout ────────────────────────────────────────────────────────────────
    private int margin;
    private int titleH;
    private int fieldH;
    private int fieldY;
    private int btnY;
    private int btnH;
    private int btnW;

    private TextFieldWidget field;

    public StringEditor(Screen parent, Text title, String initialVal, Consumer<String> onConfirm) {
        super(parent);
        this.title      = title;
        this.initialVal = initialVal != null ? initialVal : "";
        this.onConfirm  = onConfirm;
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init(); // sets theme

        margin   = 8;
        btnH     = 18;
        btnW     = 64;

        // Compute heights first, then use them in popupH
        titleH   = textRenderer.fontHeight + margin * 2;
        fieldH   = textRenderer.fontHeight + 8;   // +8 so text isn't clipped

        popupW = Math.max(260, Math.min(width * 2 / 5, 400));
        popupH = titleH + margin + fieldH + margin + btnH + margin;

        popupX = (width  - popupW) / 2;
        popupY = (height - popupH) / 2;

        // Y positions in order — each depends on the one above
        fieldY   = popupY + titleH + margin;
        btnY = fieldY + fieldH + margin;

        // Text field
        field = new TextFieldWidget(
                textRenderer,
                popupX + margin, fieldY,
                popupW - 2 * margin, fieldH,
                Text.literal("Enter text...")
        );
        field.setMaxLength(256);
        field.setText(initialVal);
        field.setFocused(true);
        addDrawableChild(field);

        // Done
        addDrawableChild(Button.builder()
                .position(popupX + margin, btnY)
                .size(btnW, btnH)
                .backgroundColor(theme.success())
                .foregroundColor(0xFFFFFFFF)
                .hoverColor(theme.success() + 0xFF101010)
                .holdColor(theme.success() + 0xFF202020)
                .borderColor(-1)
                .baseLabel(Text.literal("Done"))
                .onClick(b -> confirm())
                .build());

        // Cancel
        addDrawableChild(Button.builder()
                .position(popupX + margin * 2 + btnW, btnY)
                .size(btnW, btnH)
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .borderColor(-1)
                .baseLabel(Text.literal("Cancel"))
                .onClick(b -> close())
                .build());
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void confirm() {
        if (onConfirm != null) onConfirm.accept(field.getText());
        close();
    }

    @Override
    protected void setInitialFocus() {
        super.setInitialFocus(field);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        // Esc is handled by super → close()
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void renderPopupScreen(DrawContext c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());
        c.drawText(textRenderer, title,
                popupX + margin,
                popupY + (titleH - textRenderer.fontHeight) / 2,
                theme.header_foreground(), false);

        // Body — use popupBodyColor() from base so it's always distinct from parent
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());

        // Separator under title
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        // Field border
        int fx = popupX + margin, fw = popupW - 2 * margin;
        c.fill(fx - 1, fieldY - 1,     fx + fw + 1, fieldY,              theme.panel_separator());
        c.fill(fx - 1, fieldY + fieldH, fx + fw + 1, fieldY + fieldH + 1, theme.panel_separator());
        c.fill(fx - 1, fieldY - 1,     fx,           fieldY + fieldH + 1, theme.panel_separator());
        c.fill(fx + fw, fieldY - 1,    fx + fw + 1,  fieldY + fieldH + 1, theme.panel_separator());

        // Accent border around the whole popup — drawPopupBorder() is from base class
        drawPopupBorder(c, theme.accent());
    }
}