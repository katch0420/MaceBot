package net.katch0420.macebot.client.gui.themes;

import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Small popup overlay for naming/renaming a theme - same "render the parent
 * screen behind, draw a panel on top" pattern as {@link ColorPickerScreen}.
 *
 */
public class ThemeNamePromptScreen extends Screen {

    private final Screen parent;
    private final String initialValue;
    private final Consumer<String> onConfirm;

    private TextFieldWidget nameField;

    public ThemeNamePromptScreen(Screen parent, Text title, String initialValue, Consumer<String> onConfirm) {
        super(title);
        this.parent = parent;
        this.initialValue = initialValue;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int fieldWidth = 220;
        int x = (this.width - fieldWidth) / 2;
        int y = this.height / 2 - 20;

        nameField = new TextFieldWidget(this.textRenderer, x, y, fieldWidth, 20, Text.literal("Theme name"));
        nameField.setMaxLength(32);
        nameField.setText(initialValue == null ? "" : initialValue);
        this.addDrawableChild(nameField);
        this.setInitialFocus(nameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Confirm"), b -> confirm())
                .dimensions(x, y + 28, 105, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> this.client.setScreen(parent))
                .dimensions(x + 115, y + 28, 105, 20).build());
    }

    private void confirm() {
        String value = nameField.getText().trim();
        if (value.isEmpty()) return;
        onConfirm.accept(value);
        this.client.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter / numpad Enter
            confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.render(context, -1, -1, delta);
        }

        YarnHelpers.pushMatrix(context);
        context.getMatrices().translate(0,0,100f);

        int panelWidth = 260, panelHeight = 90;
        int x = (this.width - panelWidth) / 2;
        int y = this.height / 2 - 45;
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xE615171B);
        context.drawBorder(x, y, panelWidth, panelHeight, 0xFF3D7BB8);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, y + 8, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
        YarnHelpers.popMatrix(context);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}