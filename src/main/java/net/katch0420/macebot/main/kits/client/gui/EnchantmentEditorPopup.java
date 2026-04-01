package net.katch0420.macebot.main.kits.client.gui;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.katch0420.macebot.client.gui.widgets.core.BaseWidget;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A popup dialog for selecting and configuring enchantments.
 * Features a searchable list, level input, and add/cancel buttons.
 */
public class EnchantmentEditorPopup extends BaseWidget {

    // ========================================
    // MINECRAFT CLIENT REFERENCES
    // ========================================
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;

    // ========================================
    // POPUP SIZE CONSTRAINTS
    // ========================================
    private static final int MIN_POPUP_WIDTH = 300;
    private static final int MAX_POPUP_WIDTH = 600;
    private static final int MIN_POPUP_HEIGHT = 200;
    private static final int MAX_POPUP_HEIGHT = 500;

    // Screen coverage (popup will be 1/3 of screen size by default)
    private static final float SCREEN_WIDTH_RATIO = 0.33f;
    private static final float SCREEN_HEIGHT_RATIO = 0.33f;

    // ========================================
    // LAYOUT SPACING & SIZING
    // ========================================
    private static final int OUTER_PADDING = 12;        // Padding from popup edges
    private static final int ELEMENT_SPACING = 8;       // Space between UI elements
    private static final int SMALL_SPACING = 4;         // Small gaps within elements

    private static final int TITLE_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 22;
    private static final int LIST_ITEM_HEIGHT = 18;
    private static final int LEVEL_INPUT_HEIGHT = 32;
    private static final int SELECTED_DISPLAY_HEIGHT = 44;

    // Right panel width (for selected enchantment & level)
    private static final int RIGHT_PANEL_WIDTH = 140;

    // ========================================
    // COLORS (Dark Theme)
    // ========================================
    private static final int COLOR_BACKGROUND = 0xD0101010;          // Main popup background
    private static final int COLOR_BORDER = 0xFF606060;              // Borders
    private static final int COLOR_LIST_BACKGROUND = 0xFF1A1A1A;     // List background (darker)
    private static final int COLOR_LIST_ITEM_HOVER = 0xFF2A2A2A;     // Hovered item
    private static final int COLOR_LIST_ITEM_SELECTED = 0xFF3A3A3A;  // Selected item
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFF;          // Main text
    private static final int COLOR_TEXT_SECONDARY = 0xAAAAAA;        // Labels
    private static final int COLOR_SCROLLBAR = 0xFF808080;           // Scrollbar

    // ========================================
    // ENCHANTMENT DATA
    // ========================================
    private final List<RegistryEntry<Enchantment>> allEnchantments = new ArrayList<>();
    private RegistryEntry<Enchantment> selectedEnchantment = null;
    private int selectedLevel = 1;

    // ========================================
    // LAYOUT COORDINATES (calculated dynamically)
    // ========================================
    // Main popup bounds (inherited from BaseWidget: x, y, width, height)

    // Enchantment list
    private int listX, listY, listWidth, listHeight;

    // Right panel
    private int rightPanelX, rightPanelY;

    // Selected enchantment display
    private int selectedBoxX, selectedBoxY, selectedBoxWidth, selectedBoxHeight;

    // Level input
    private int levelBoxX, levelBoxY, levelBoxWidth, levelBoxHeight;

    // Buttons
    private int addButtonX, addButtonY, addButtonWidth;
    private int cancelButtonX, cancelButtonY, cancelButtonWidth;

    // ========================================
    // SCROLLING STATE
    // ========================================
    private int scrollOffset = 0;              // Current scroll position in pixels
    private int maxScrollOffset = 0;           // Maximum scroll value
    private boolean isDraggingScrollbar = false;

    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_MARGIN = 6;  // Distance from right edge

    // ========================================
    // UI WIDGETS
    // ========================================
    private TextFieldWidget levelInputField;
    private SimpleButton addButton;
    private SimpleButton cancelButton;

    // ========================================
    // CALLBACKS
    // ========================================
    private final BiConsumer<RegistryEntry<Enchantment>, Integer> onAddCallback;
    private final Runnable onCancelCallback;

    // ========================================
    // CONSTRUCTOR
    // ========================================
    public EnchantmentEditorPopup(
            BiConsumer<RegistryEntry<Enchantment>, Integer> onAdd,
            Runnable onCancel) {
        this.onAddCallback = onAdd;
        this.onCancelCallback = onCancel;

        loadAllEnchantments();
        calculatePopupSizeAndPosition();
        calculateLayout();
        createWidgets();
        updateScrollBounds();
    }

    // ========================================
    // INITIALIZATION METHODS
    // ========================================

    /**
     * Load all enchantments from the registry and sort them alphabetically
     */
    private void loadAllEnchantments() {
        allEnchantments.clear();
        client.world.getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .streamEntries()
                                .forEach(allEnchantments::add);

        // Sort alphabetically by display name
        allEnchantments.sort((a, b) -> {
            String nameA = getEnchantmentDisplayName(a);
            String nameB = getEnchantmentDisplayName(b);
            return nameA.compareTo(nameB);
        });
    }

    /**
     * Calculate the popup's size and center it on screen
     */
    private void calculatePopupSizeAndPosition() {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate desired size (ratio of screen)
        int desiredWidth = (int) (screenWidth * SCREEN_WIDTH_RATIO);
        int desiredHeight = (int) (screenHeight * SCREEN_HEIGHT_RATIO);

        // Clamp to min/max constraints
        int finalWidth = clamp(desiredWidth, MIN_POPUP_WIDTH, MAX_POPUP_WIDTH);
        int finalHeight = clamp(desiredHeight, MIN_POPUP_HEIGHT, MAX_POPUP_HEIGHT);

        // Center on screen
        int finalX = (screenWidth - finalWidth) / 2;
        int finalY = (screenHeight - finalHeight) / 2;

        setX(finalX);
        setY(finalY);
        setWidth(finalWidth);
        setHeight(finalHeight);
    }

    /**
     * Calculate positions of all UI elements based on popup size
     */
    private void calculateLayout() {
        int popupX = getX();
        int popupY = getY();
        int popupWidth = getWidth();
        int popupHeight = getHeight();

        // Available content area (inside padding)
        int contentX = popupX + OUTER_PADDING;
        int contentY = popupY + OUTER_PADDING + TITLE_HEIGHT;
        int contentWidth = popupWidth - (OUTER_PADDING * 2);
        int contentHeight = popupHeight - (OUTER_PADDING * 2) - TITLE_HEIGHT;

        // Bottom buttons
        int buttonAreaHeight = BUTTON_HEIGHT + ELEMENT_SPACING;
        int buttonY = popupY + popupHeight - OUTER_PADDING - BUTTON_HEIGHT;
        int buttonWidth = (contentWidth - ELEMENT_SPACING) / 2;

        addButtonX = contentX;
        addButtonY = buttonY;
        addButtonWidth = buttonWidth;

        cancelButtonX = contentX + buttonWidth + ELEMENT_SPACING;
        cancelButtonY = buttonY;
        cancelButtonWidth = buttonWidth;

        // Right panel (selected enchantment + level)
        rightPanelX = contentX + contentWidth - RIGHT_PANEL_WIDTH;
        rightPanelY = contentY;

        // Selected enchantment display (top of right panel)
        selectedBoxX = rightPanelX;
        selectedBoxY = rightPanelY;
        selectedBoxWidth = RIGHT_PANEL_WIDTH;
        selectedBoxHeight = SELECTED_DISPLAY_HEIGHT;

        // Level input (below selected display)
        levelBoxX = rightPanelX;
        levelBoxY = selectedBoxY + selectedBoxHeight + ELEMENT_SPACING;
        levelBoxWidth = RIGHT_PANEL_WIDTH;
        levelBoxHeight = LEVEL_INPUT_HEIGHT;

        // Enchantment list (left side, full height minus buttons)
        listX = contentX;
        listY = contentY;
        listWidth = contentWidth - RIGHT_PANEL_WIDTH - ELEMENT_SPACING;
        listHeight = contentHeight - buttonAreaHeight;
    }

    /**
     * Create interactive widgets (text field, buttons)
     */
    private void createWidgets() {
        // Level input field
        levelInputField = new TextFieldWidget(
                textRenderer,
                levelBoxX + SMALL_SPACING,
                levelBoxY + 20,
                levelBoxWidth - (SMALL_SPACING * 2),
                16,
                Text.literal("Level")
        );
        levelInputField.setMaxLength(3);
        levelInputField.setText("1");
        levelInputField.setChangedListener(this::onLevelInputChanged);
        levelInputField.setFocusUnlocked(true);
        levelInputField.setEditableColor(COLOR_TEXT_PRIMARY);

        // Add button
        addButton = new SimpleButton(
                addButtonX,
                addButtonY,
                addButtonWidth,
                BUTTON_HEIGHT,
                "Add",
                this::onAddButtonClicked,
                0x00000000,
                0x80505050,
                COLOR_TEXT_PRIMARY
        );

        // Cancel button
        cancelButton = new SimpleButton(
                cancelButtonX,
                cancelButtonY,
                cancelButtonWidth,
                BUTTON_HEIGHT,
                "Cancel",
                this::onCancelButtonClicked,
                0x00000000,
                0x80505050,
                COLOR_TEXT_PRIMARY
        );
    }

    /**
     * Calculate how far the list can scroll
     */
    private void updateScrollBounds() {
        int totalContentHeight = allEnchantments.size() * LIST_ITEM_HEIGHT;
        int visibleHeight = listHeight;
        maxScrollOffset = Math.max(0, totalContentHeight - visibleHeight);

        // Clamp current scroll to new bounds
        scrollOffset = clamp(scrollOffset, 0, maxScrollOffset);
    }

    // ========================================
    // WINDOW RESIZE HANDLER
    // ========================================

    /**
     * Called when the game window is resized - recalculate everything
     */
    public void resize() {
        calculatePopupSizeAndPosition();
        calculateLayout();
        createWidgets();  // Recreate widgets with new positions
        updateScrollBounds();
    }

    // ========================================
    // EVENT HANDLERS
    // ========================================

    private void onLevelInputChanged(String text) {
        try {
            int level = Integer.parseInt(text);
            if (level >= 1 && level <= 255) {
                selectedLevel = level;
            }
        } catch (NumberFormatException e) {
            // Invalid input - ignore
        }
    }

    private void onAddButtonClicked() {
        if (selectedEnchantment != null && onAddCallback != null) {
            onAddCallback.accept(selectedEnchantment, selectedLevel);
        }
    }

    private void onCancelButtonClicked() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }

    // ========================================
    // MOUSE INPUT
    // ========================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;  // Only handle left click

        // Priority 1: Text field
        if (isMouseInLevelInputField(mouseX, mouseY)) {
            levelInputField.mouseClicked(mouseX, mouseY, button);
            levelInputField.setFocused(true);
            return true;
        } else {
            levelInputField.setFocused(false);
        }

        // Priority 2: Buttons
        if (addButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (cancelButton.mouseClicked(mouseX, mouseY, button)) return true;

        // Priority 3: Scrollbar (before list, so it takes precedence)
        if (isMouseOnScrollbar(mouseX, mouseY)) {
            isDraggingScrollbar = true;
            return true;
        }

        // Priority 4: Enchantment list items
        if (isMouseInEnchantmentList(mouseX, mouseY)) {
            handleEnchantmentListClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingScrollbar = false;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar) {
            updateScrollFromMouseDrag(mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseInEnchantmentList(mouseX, mouseY) || isMouseOnScrollbar(mouseX, mouseY)) {
            int scrollAmount = (int) (verticalAmount * 20);
            scrollOffset = clamp(scrollOffset - scrollAmount, 0, maxScrollOffset);
            return true;
        }
        return false;
    }

    // ========================================
    // KEYBOARD INPUT
    // ========================================

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (levelInputField.isFocused()) {
            return levelInputField.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (levelInputField.isFocused()) {
            return levelInputField.charTyped(character, modifiers);
        }
        return false;
    }

    // ========================================
    // MOUSE INTERACTION HELPERS
    // ========================================

    private boolean isMouseInEnchantmentList(double mouseX, double mouseY) {
        // Exclude scrollbar area from clickable list
        int clickableWidth = listWidth - SCROLLBAR_MARGIN - SCROLLBAR_WIDTH;
        return mouseX >= listX
                && mouseX < listX + clickableWidth
                && mouseY >= listY
                && mouseY < listY + listHeight;
    }

    private boolean isMouseInLevelInputField(double mouseX, double mouseY) {
        return mouseX >= levelInputField.getX()
                && mouseX < levelInputField.getX() + levelInputField.getWidth()
                && mouseY >= levelInputField.getY()
                && mouseY < levelInputField.getY() + levelInputField.getHeight();
    }

    private boolean isMouseOnScrollbar(double mouseX, double mouseY) {
        if (maxScrollOffset <= 0) return false;  // No scrollbar if content fits

        int scrollbarX = listX + listWidth - SCROLLBAR_MARGIN;
        int scrollbarY = listY + getScrollbarPositionY();
        int scrollbarHeight = getScrollbarHeight();

        return mouseX >= scrollbarX
                && mouseX < scrollbarX + SCROLLBAR_WIDTH
                && mouseY >= scrollbarY
                && mouseY < scrollbarY + scrollbarHeight;
    }

    private void handleEnchantmentListClick(double mouseX, double mouseY) {
        // Calculate which item was clicked
        int relativeY = (int) (mouseY - listY) + scrollOffset;
        int itemIndex = relativeY / LIST_ITEM_HEIGHT;

        if (itemIndex >= 0 && itemIndex < allEnchantments.size()) {
            selectedEnchantment = allEnchantments.get(itemIndex);
        }
    }

    private void updateScrollFromMouseDrag(double mouseY) {
        int scrollbarHeight = getScrollbarHeight();
        int trackHeight = listHeight - scrollbarHeight;

        // Calculate percentage based on mouse position
        double relativeY = mouseY - listY;
        double scrollPercentage = clamp(relativeY / trackHeight, 0.0, 1.0);

        scrollOffset = (int) (scrollPercentage * maxScrollOffset);
        scrollOffset = clamp(scrollOffset, 0, maxScrollOffset);
    }

    // ========================================
    // SCROLLBAR CALCULATIONS
    // ========================================

    private int getScrollbarHeight() {
        if (maxScrollOffset <= 0) return listHeight;  // Full height if no scroll needed

        int totalContentHeight = allEnchantments.size() * LIST_ITEM_HEIGHT;
        int proportionalHeight = (listHeight * listHeight) / totalContentHeight;

        return Math.max(20, proportionalHeight);  // Minimum 20px
    }

    private int getScrollbarPositionY() {
        if (maxScrollOffset <= 0) return 0;

        int scrollbarHeight = getScrollbarHeight();
        int trackHeight = listHeight - scrollbarHeight;

        double scrollPercentage = (double) scrollOffset / maxScrollOffset;
        return (int) (scrollPercentage * trackHeight);
    }

    // ========================================
    // RENDERING
    // ========================================

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderPopupBackground(ctx);
        renderTitle(ctx);
        renderEnchantmentList(ctx, mouseX, mouseY);
        renderSelectedEnchantmentDisplay(ctx);
        renderLevelInput(ctx, mouseX, mouseY, delta);
        renderButtons(ctx, mouseX, mouseY);
    }

    private void renderPopupBackground(DrawContext ctx) {
        // Main background
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COLOR_BACKGROUND);

        // Border
        ctx.drawBorder(getX(), getY(), getWidth(), getHeight(), COLOR_BORDER);
    }

    private void renderTitle(DrawContext ctx) {
        String titleText = "Enchantment Editor";
        int titleX = getX() + (getWidth() / 2);
        int titleY = getY() + OUTER_PADDING;

        ctx.drawCenteredTextWithShadow(textRenderer, titleText, titleX, titleY, COLOR_TEXT_PRIMARY);
    }

    private void renderEnchantmentList(DrawContext ctx, int mouseX, int mouseY) {
        // Background box
        ctx.fill(listX, listY, listX + listWidth, listY + listHeight, COLOR_LIST_BACKGROUND);
        ctx.drawBorder(listX, listY, listWidth, listHeight, COLOR_BORDER);

        // Enable clipping to list bounds
        ctx.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        // Render each enchantment item
        int itemY = listY - scrollOffset;
        for (int i = 0; i < allEnchantments.size(); i++) {
            RegistryEntry<Enchantment> enchantment = allEnchantments.get(i);

            // Only render if visible
            if (itemY + LIST_ITEM_HEIGHT >= listY && itemY < listY + listHeight) {
                renderEnchantmentListItem(ctx, enchantment, itemY, mouseX, mouseY);
            }

            itemY += LIST_ITEM_HEIGHT;
        }

        ctx.disableScissor();

        // Scrollbar
        if (maxScrollOffset > 0) {
            renderScrollbar(ctx);
        }
    }

    private void renderEnchantmentListItem(
            DrawContext ctx,
            RegistryEntry<Enchantment> enchantment,
            int itemY,
            int mouseX,
            int mouseY) {

        boolean isSelected = enchantment.equals(selectedEnchantment);
        boolean isHovered = isItemHovered(itemY, mouseX, mouseY);

        // Background highlight
        if (isSelected) {
            ctx.fill(listX + 2, itemY, listX + listWidth - SCROLLBAR_MARGIN - SCROLLBAR_WIDTH,
                    itemY + LIST_ITEM_HEIGHT, COLOR_LIST_ITEM_SELECTED);
        } else if (isHovered) {
            ctx.fill(listX + 2, itemY, listX + listWidth - SCROLLBAR_MARGIN - SCROLLBAR_WIDTH,
                    itemY + LIST_ITEM_HEIGHT, COLOR_LIST_ITEM_HOVER);
        }

        // Enchantment name
        String displayName = getEnchantmentDisplayName(enchantment);
        int textX = listX + SMALL_SPACING + 2;
        int textY = itemY + (LIST_ITEM_HEIGHT - textRenderer.fontHeight) / 2;

        ctx.drawText(textRenderer, displayName, textX, textY, COLOR_TEXT_PRIMARY, false);
    }

    private boolean isItemHovered(int itemY, int mouseX, int mouseY) {
        int clickableWidth = listWidth - SCROLLBAR_MARGIN - SCROLLBAR_WIDTH;
        return mouseX >= listX
                && mouseX < listX + clickableWidth
                && mouseY >= itemY
                && mouseY < itemY + LIST_ITEM_HEIGHT;
    }

    private void renderScrollbar(DrawContext ctx) {
        int scrollbarX = listX + listWidth - SCROLLBAR_MARGIN;
        int scrollbarY = listY + getScrollbarPositionY();
        int scrollbarHeight = getScrollbarHeight();

        ctx.fill(scrollbarX, scrollbarY,
                scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight,
                COLOR_SCROLLBAR);
    }

    private void renderSelectedEnchantmentDisplay(DrawContext ctx) {
        // Border
        ctx.drawBorder(selectedBoxX, selectedBoxY, selectedBoxWidth, selectedBoxHeight, COLOR_BORDER);

        // Label
        String labelText = "Enchantment:";
        int labelX = selectedBoxX + SMALL_SPACING;
        int labelY = selectedBoxY + SMALL_SPACING;
        ctx.drawText(textRenderer, labelText, labelX, labelY, COLOR_TEXT_SECONDARY, false);

        // Selected enchantment name (or "Choose")
        String displayText = selectedEnchantment != null
                ? getEnchantmentDisplayName(selectedEnchantment)
                : "Choose";

        // Truncate if too long
        int maxTextWidth = selectedBoxWidth - (SMALL_SPACING * 2);
        if (textRenderer.getWidth(displayText) > maxTextWidth) {
            displayText = textRenderer.trimToWidth(displayText, maxTextWidth - 10) + "...";
        }

        int nameX = selectedBoxX + SMALL_SPACING;
        int nameY = selectedBoxY + SMALL_SPACING + textRenderer.fontHeight + 4;
        ctx.drawText(textRenderer, displayText, nameX, nameY, COLOR_TEXT_PRIMARY, false);
    }

    private void renderLevelInput(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Border
        ctx.drawBorder(levelBoxX, levelBoxY, levelBoxWidth, levelBoxHeight, COLOR_BORDER);

        // Label
        String labelText = "Level:";
        int labelX = levelBoxX + SMALL_SPACING;
        int labelY = levelBoxY + SMALL_SPACING;
        ctx.drawText(textRenderer, labelText, labelX, labelY, COLOR_TEXT_SECONDARY, false);

        // Text field
        levelInputField.render(ctx, mouseX, mouseY, delta);
    }

    private void renderButtons(DrawContext ctx, int mouseX, int mouseY) {
        addButton.render(ctx, mouseX, mouseY);
        cancelButton.render(ctx, mouseX, mouseY);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Convert enchantment registry entry to human-readable name
     */
    private String getEnchantmentDisplayName(RegistryEntry<Enchantment> entry) {
        return entry.getKey()
                .map(key -> key.getValue().getPath())
                .map(path -> path.replace("_", " "))
                .map(this::capitalizeEachWord)
                .orElse("Unknown");
    }

    /**
     * Capitalize the first letter of each word
     */
    private String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (c == ' ' || c == '_') {
                capitalizeNext = true;
                result.append(' ');
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Clamp a value between min and max
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}