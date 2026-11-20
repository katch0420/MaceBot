package net.katch0420.macebot.client.gui.themes;

import net.katch0420.macebot.client.utils.RenderUtils;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

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

    private EditBox nameField;

    public ThemeNamePromptScreen(Screen parent, Component title, String initialValue, Consumer<String> onConfirm) {
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

        nameField = new EditBox(this.font, x, y, fieldWidth, 20, Component.literal("Theme name"));
        nameField.setMaxLength(32);
        nameField.setValue(initialValue == null ? "" : initialValue);
        this.addRenderableWidget(nameField);
        this.setInitialFocus(nameField);

        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), b -> confirm())
                .bounds(x, y + 28, 105, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> setScreen(parent))
                .bounds(x + 115, y + 28, 105, 20).build());
    }

    private void confirm() {
        String value = nameField.getValue().trim();
        if (value.isEmpty()) return;
        onConfirm.accept(value);
        setScreen(parent);
    }

    private void setScreen(Screen screen){
        //? if <26.2 {
        Minecraft.getInstance().setScreen(screen);
        //?} else
        //Minecraft.getInstance().setScreenAndShow(screen);
    }

    @Override
    public boolean keyPressed(
            //? if >=1.21.9 {
            KeyEvent keyEvent
            //?} else
            //int keyCode, int scanCode, int modifiers
    ) {
        //? if >=1.21.9
        int keyCode = keyEvent.key();
        if (keyCode == 257 || keyCode == 335) { // Enter / numpad Enter
            confirm();
            return true;
        }
        return super.keyPressed(
                //? if >=1.21.9 {
                keyEvent
                //?} else
                //keyCode, scanCode, modifiers
        );
    }
    //? if <=1.21.11 {
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.render(context, -1, -1, delta);
        }

        YarnHelpers.pushMatrix(context);
        //? if >=1.21.6 {
        context.nextStratum();
        //?} else
        //context.pose().translate(0,0,100f);

        int panelWidth = 260, panelHeight = 90;
        int x = (this.width - panelWidth) / 2;
        int y = this.height / 2 - 45;
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xE615171B);
        RenderUtils.drawBorder(context, x, y, panelWidth, panelHeight, 0xFF3D7BB8);
        context.drawCenteredString(this.font, this.title, this.width / 2, y + 8, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
        YarnHelpers.popMatrix(context);
    }
     //?} else {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.extractRenderState(context, -1, -1, delta);
        }

        YarnHelpers.pushMatrix(context);
        //? if >=1.21.6 {
        /^context.nextStratum();
        ^///?} else
        context.pose().translate(0,0,100f);

        int panelWidth = 260, panelHeight = 90;
        int x = (this.width - panelWidth) / 2;
        int y = this.height / 2 - 45;
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xE615171B);
        RenderUtils.drawBorder(context, x, y, panelWidth, panelHeight, 0xFF3D7BB8);
        context.centeredText(this.font, this.title, this.width / 2, y + 8, 0xFFFFFFFF);

        super.extractRenderState(context, mouseX, mouseY, delta);
        YarnHelpers.popMatrix(context);
    }
    *///?}

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}