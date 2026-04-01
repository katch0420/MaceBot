package net.katch0420.macebot.main.kits.client.gui;

import net.katch0420.macebot.client.gui.widgets.core.BaseWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class NameEditorPopup extends BaseWidget {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;

    // Dimensions
    private static final int POPUP_WIDTH = 400;
    private static final int POPUP_HEIGHT = 180;
    private static final int PADDING = 16;
    private static final int GAP = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 80;
    private static final int TEXT_EDITOR_HEIGHT = 60;

    // Colors - matching ItemEditor dark theme
    private final int backgroundColor = 0xD0101010;
    private final int borderColor = 0xFF606060;

    // UI Components
    private TextFieldWidget nameField;
    private SimpleButton saveButton;
    private SimpleButton cancelButton;

    private int textEditorX, textEditorY, textEditorWidth;
    private int noteBoxX, noteBoxY, noteBoxWidth, noteBoxHeight;

    // Data
    private String currentName;

    // Callbacks
    private final Consumer<String> onSave;
    private final Runnable onCancel;

    public NameEditorPopup(String initialName, Consumer<String> onSave, Runnable onCancel) {
        this.currentName = initialName != null ? initialName : "";
        this.onSave = onSave;
        this.onCancel = onCancel;

        resize();
        initWidgets();
    }

    /**
     * Resize the popup to fit the current screen dimensions
     */
    public void resize() {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate popup size with constraints
        int maxWidth = Math.min(POPUP_WIDTH, screenWidth - 40); // 20px margin on each side
        int maxHeight = Math.min(POPUP_HEIGHT, screenHeight - 40);

        setX((screenWidth - maxWidth) / 2);
        setY((screenHeight - maxHeight) / 2);
        setWidth(maxWidth);
        setHeight(maxHeight);

        // If buttons exist, reposition them
        if (saveButton != null && cancelButton != null) {
            int buttonY = getY() + getHeight() - PADDING - BUTTON_HEIGHT;
            int buttonSpacing = 10;
            int totalButtonWidth = BUTTON_WIDTH * 2 + buttonSpacing;
            int buttonStartX = getX() + (getWidth() - totalButtonWidth) / 2;

            saveButton = new SimpleButton(
                    buttonStartX,
                    buttonY,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    "Save",
                    this::handleSave,
                    0x00000000,
                    0x80505050,
                    0xFFFFFF
            );

            cancelButton = new SimpleButton(
                    buttonStartX + BUTTON_WIDTH + buttonSpacing,
                    buttonY,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    "Cancel",
                    this::handleCancel,
                    0x00000000,
                    0x80505050,
                    0xFFFFFF
            );
        }
    }

    private void initWidgets() {
        // Create the name text field (will be positioned in calculateLayout)
        nameField = new TextFieldWidget(textRenderer, 0, 0,
                getWidth() - PADDING * 2, 16,
                Text.literal("Item Name"));
        nameField.setMaxLength(100);
        nameField.setText(stripColorCodes(currentName));
        nameField.setChangedListener(this::onNameChange);
        nameField.setFocusUnlocked(true);
        nameField.setEditableColor(0xFFFFFF);

        // Calculate button positions
        int buttonY = getY() + getHeight() - PADDING - BUTTON_HEIGHT;
        int buttonSpacing = 10;
        int totalButtonWidth = BUTTON_WIDTH * 2 + buttonSpacing;
        int buttonStartX = getX() + (getWidth() - totalButtonWidth) / 2;

        saveButton = new SimpleButton(
                buttonStartX,
                buttonY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                "Save",
                this::handleSave,
                0x00000000,
                0x80505050,
                0xFFFFFF
        );

        cancelButton = new SimpleButton(
                buttonStartX + BUTTON_WIDTH + buttonSpacing,
                buttonY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                "Cancel",
                this::handleCancel,
                0x00000000,
                0x80505050,
                0xFFFFFF
        );
    }

    private void onNameChange(String name) {
        currentName = name;
    }

    private void calculateLayout() {
        // Title takes space at top
        int currentY = getY() + PADDING + 16;

        // Label "Name:"
        currentY += GAP;

        // Text editor
        textEditorX = getX() + PADDING;
        textEditorY = currentY;
        textEditorWidth = getWidth() - PADDING * 2;

        // Position the text field
        nameField.setX(textEditorX + 4);
        nameField.setY(textEditorY + 4);
        nameField.setWidth(textEditorWidth - 8);

        currentY += TEXT_EDITOR_HEIGHT + GAP;

        // Note box
        noteBoxX = getX() + PADDING;
        noteBoxY = currentY;
        noteBoxWidth = getWidth() - PADDING * 2;
        noteBoxHeight = 18;
    }

    private void handleSave() {
        if (onSave != null) {
            onSave.accept(currentName);
        }
    }

    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return false;

        // Check text field first
        if (mx >= nameField.getX() && mx < nameField.getX() + nameField.getWidth() &&
                my >= nameField.getY() && my < nameField.getY() + nameField.getHeight()) {
            nameField.mouseClicked(mx, my, btn);
            nameField.setFocused(true);
            return true;
        } else {
            nameField.setFocused(false);
        }

        // Check buttons
        if (saveButton.mouseClicked(mx, my, btn)) return true;
        if (cancelButton.mouseClicked(mx, my, btn)) return true;

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField.isFocused()) {
            return nameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (nameField.isFocused()) {
            return nameField.charTyped(chr, modifiers);
        }
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        calculateLayout();

        // Background
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);
        ctx.drawBorder(getX(), getY(), getWidth(), getHeight(), borderColor);

        // Title
        String title = "Name Editor";
        ctx.drawCenteredTextWithShadow(textRenderer, title,
                getX() + getWidth() / 2, getY() + PADDING, 0xFFFFFF);

        int currentY = getY() + PADDING + 16 + GAP;

        // Text editor
        renderTextEditor(ctx, mx, my, delta);
        currentY += TEXT_EDITOR_HEIGHT + GAP;

        // Note box
        renderNoteBox(ctx);

        // Buttons
        saveButton.render(ctx, mx, my);
        cancelButton.render(ctx, mx, my);
    }

    private void renderTextEditor(DrawContext ctx, int mx, int my, float delta) {
        // Just border, no background fill
        ctx.drawBorder(textEditorX, textEditorY,
                textEditorWidth, TEXT_EDITOR_HEIGHT, borderColor);

        // Render text field
        nameField.render(ctx, mx, my, delta);

        // Show preview of colored text at the bottom
        if (!currentName.isEmpty()) {
            Text coloredPreview = parseColorCodes(currentName);
            int previewY = textEditorY + TEXT_EDITOR_HEIGHT - 20;

            ctx.drawText(textRenderer, "Preview:", textEditorX + 4, previewY - 12, 0x888888, false);
            ctx.drawText(textRenderer, coloredPreview, textEditorX + 4, previewY, 0xFFFFFF, true);
        }
    }

    private void renderNoteBox(DrawContext ctx) {
        // Just border, no background fill
        ctx.drawBorder(noteBoxX, noteBoxY,
                noteBoxWidth, noteBoxHeight, borderColor);

        // Note text - scaled to fit nicely
        String noteText = "Color codes supported: &a, &c, &b, etc.";
        int textWidth = textRenderer.getWidth(noteText);

        float scale = Math.min(1.0f, (float)(noteBoxWidth - 8) / textWidth);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(noteBoxX + 4, noteBoxY + (noteBoxHeight - 8 * scale) / 2, 0);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.drawText(textRenderer, noteText, 0, 0, 0x888888, false);
        ctx.getMatrices().pop();
    }

    /**
     * Strip color codes from text for editing
     */
    private String stripColorCodes(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("&[0-9a-fk-or]", "");
    }

    /**
     * Parse color codes (&a, &c, etc.) into formatted text
     */
    private Text parseColorCodes(String input) {
        if (input == null || input.isEmpty()) {
            return Text.empty();
        }

        MutableText result = Text.empty();
        StringBuilder current = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                // Flush current text
                if (!current.isEmpty()) {
                    result.append(Text.literal(current.toString()).setStyle(style));
                    current = new StringBuilder();
                }

                // Parse color code
                char code = input.charAt(++i);
                Formatting fmt = Formatting.byCode(code);
                if (fmt != null) {
                    if (fmt.isColor()) {
                        // Reset style and apply new color
                        style = Style.EMPTY.withFormatting(fmt);
                    } else {
                        // Add formatting (bold, italic, etc.)
                        style = style.withFormatting(fmt);
                    }
                }
            } else {
                current.append(c);
            }
        }

        // Flush remaining text
        if (!current.isEmpty()) {
            result.append(Text.literal(current.toString()).setStyle(style));
        }

        return result;
    }

    /**
     * Get the current name with color codes
     */
    public String getCurrentName() {
        return currentName;
    }
}