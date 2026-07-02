package net.katch0420.macebot.client.gui.bodies;

import net.katch0420.macebot.client.gui.bodies.popup.ItemEditorPopup;
import net.katch0420.macebot.client.utils.Scroller;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KitEditorBody extends Body {
    private int margin;
    private int browserPanelWidth;
    private int inventoryPanelWidth;

    private final Kit activeKit;

    public enum BrowserMode { ALL, PRESET }
    private BrowserMode activeBrowser = BrowserMode.ALL;

    private ItemBrowser allBrowser;
    private ItemBrowser presetBrowser;
    private final List<KitSlot> kitSlots = new ArrayList<>();

    public KitEditorBody(Kit activeKit) {
        this.activeKit = activeKit;
    }

    @Override
    public void init() {
        super.init();
        margin = s(6, 4);

        // Fixed 9 columns for Inventory + margins
        inventoryPanelWidth = 9 * 18 + 2 * margin;

        // Dynamic Browser Width: Max 9 columns, minimum fills remaining space
        int remainingWidth = availableWidth - inventoryPanelWidth - margin * 3;
        int browserCols = Math.max(1, Math.min(9, remainingWidth / 18));
        browserPanelWidth = browserCols * 18 + margin * 2;

        int browserX = x + inventoryPanelWidth + margin;

        allBrowser = new ItemBrowser(browserX, y + 24, browserPanelWidth, availableHeight - 24 - margin, browserCols);
        presetBrowser = new ItemBrowser(browserX, y + 24, browserPanelWidth, availableHeight - 24 - margin, browserCols);

        populateAllBrowser();
        populatePresetBrowser();
        initKitSlots();
    }

    private void initKitSlots() {
        kitSlots.clear();
        int startX = x + margin;
        int startY = y + 24; // Below header

        // 1st Row: 4 "Crafting" slots, 4 Armor slots, 1 Offhand slot
        for (int i = 0; i < 4; i++) { // Generic top slots (could represent crafting)
            kitSlots.add(new KitSlot(startX + i * 18, startY, 41 + i));
        }
        for (int i = 0; i < 4; i++) { // Armor (36-39)
            kitSlots.add(new KitSlot(startX + (4 + i) * 18, startY, 39 - i));
        }
        // Offhand (40)
        kitSlots.add(new KitSlot(startX + 8 * 18, startY, 40));

        // 3x9 Grid (Main inventory: slots 9-35)
        int gridY = startY + 18 + margin;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = (row * 9) + col + 9;
                kitSlots.add(new KitSlot(startX + col * 18, gridY + row * 18, slot));
            }
        }

        // 1x9 Hotbar (slots 0-8)
        int hotbarY = gridY + 3 * 18 + margin;
        for (int col = 0; col < 9; col++) {
            kitSlots.add(new KitSlot(startX + col * 18, hotbarY, col));
        }
    }

    private void populateAllBrowser() {
        // Categorize items by namespace as a fallback for Creative Tabs
        Map<String, ItemBrowser.ItemCategory> categoryMap = new LinkedHashMap<>();
        for (Item item : Registries.ITEM) {
            String namespace = Registries.ITEM.getId(item).getNamespace();
            String catName = namespace.substring(0, 1).toUpperCase() + namespace.substring(1);

            categoryMap.putIfAbsent(catName, allBrowser.new ItemCategory(Text.literal(catName)));
            categoryMap.get(catName).items.add(item);
        }
        allBrowser.categories.addAll(categoryMap.values());
        allBrowser.calculateScroll();
    }

    private void populatePresetBrowser() {
        // TODO: Load client-side saved presets here
        presetBrowser.categories.add(presetBrowser.new ItemCategory(Text.literal("Saved Presets")));
        presetBrowser.calculateScroll();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Check slots for Ctrl + Click to open ItemEditor
        boolean isCtrlPressed = Screen.hasControlDown();
        for (KitSlot slot : kitSlots) {
            if (slot.isMouseOver(mx, my)) {
                if (isCtrlPressed && slot.stack != null) {
                    MinecraftClient.getInstance().setScreen(
                            new ItemEditorPopup(parent, slot.stack, updatedStack -> {
                                slot.stack = updatedStack;
                                activeKit.getItems().put(slot.slotId, updatedStack);
                            })
                    );
                    return true;
                }
                // TODO: Handle standard item pickup/drop logic here
                return true;
            }
        }

        ItemBrowser current = (activeBrowser == BrowserMode.ALL) ? allBrowser : presetBrowser;
        if (current.mouseClicked(mx, my, btn)) return true;

        // Handle Header Tab Clicks
        int headerX = x + inventoryPanelWidth + margin;
        if (my >= y && my <= y + 20) {
            if (mx >= headerX && mx <= headerX + 50) activeBrowser = BrowserMode.ALL;
            else if (mx >= headerX + 60 && mx <= headerX + 120) activeBrowser = BrowserMode.PRESET;
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        // Inventory Title
        String title = "Kit: " + (activeKit != null ? activeKit.getDisplayName() : "Unnamed");
        c.drawText(getTextRenderer(), Text.literal(title), x + margin, y + margin, theme.accent(), false);

        // Render Slots
        for (KitSlot slot : kitSlots) {
            c.fill(slot.x, slot.y, slot.x + 18, slot.y + 18, theme.panel_separator());
            if (slot.stack != null) {
                c.drawItem(slot.stack.toStack(), slot.x + 1, slot.y + 1);
            }
            if (slot.isMouseOver(mx, my)) {
                c.fill(slot.x, slot.y, slot.x + 18, slot.y + 18, 0x55FFFFFF);
            }
        }

        // Render Tooltips/Tips
        int tipY = kitSlots.get(kitSlots.size() - 1).y + 24;
        c.drawText(getTextRenderer(), Text.literal("Tips:"), x + margin, tipY, theme.body_label(), false);
        c.drawText(getTextRenderer(), Text.literal("Ctrl + Click a slot to open the Item Editor"), x + margin, tipY + 12, theme.body_value(), false);

        // Render Browser Header
        int headerX = x + inventoryPanelWidth + margin;
        int allColor = activeBrowser == BrowserMode.ALL ? theme.accent() : theme.body_label();
        int preColor = activeBrowser == BrowserMode.PRESET ? theme.accent() : theme.body_label();

        c.drawText(getTextRenderer(), Text.literal("[ All ]"), headerX, y + margin, allColor, false);
        c.drawText(getTextRenderer(), Text.literal("[ Preset ]"), headerX + 60, y + margin, preColor, false);

        // Render Active Browser
        if (activeBrowser == BrowserMode.ALL) allBrowser.render(c, mx, my);
        else presetBrowser.render(c, mx, my);
    }

    // ── Inner Classes ────────────────────────────────────────────────────────
    protected class ItemBrowser {
        int x, y, w, h, cols;
        Scroller scroller = new Scroller();
        List<ItemCategory> categories = new ArrayList<>();

        ItemBrowser(int x, int y, int w, int h, int cols) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.cols = cols;
        }

        void calculateScroll() {
            int totalH = 0;
            for (ItemCategory cat : categories) {
                totalH += 18; // Header height
                int rows = (int) Math.ceil((double) cat.items.size() / cols);
                totalH += rows * 18;
            }
            scroller.setArea(x, y, h, totalH);
        }

        void render(DrawContext c, int mx, int my) {
            c.enableScissor(x, y, x + w, y + h);
            int currentY = y - scroller.getOffset();

            for (ItemCategory cat : categories) {
                c.drawText(getTextRenderer(), cat.displayText, x, currentY + 4, theme.body_label(), false);
                currentY += 18;

                int col = 0;
                for (Item item : cat.items) {
                    int itemX = x + (col * 18);
                    c.drawItem(new ItemStack(item), itemX + 1, currentY + 1);
                    col++;
                    if (col >= cols) {
                        col = 0;
                        currentY += 18;
                    }
                }
                if (col != 0) currentY += 18; // Push down if row was incomplete
            }
            c.disableScissor();
            scroller.render(c, x + w - 5, 5, theme.panel_separator(), theme.accent());
        }

        boolean mouseClicked(double mx, double my, int btn) {
            return scroller.mouseClicked(mx, my, x + w - 5, 5);
        }

        public class ItemCategory {
            Text displayText;
            List<Item> items = new ArrayList<>();
            ItemCategory(Text text) { this.displayText = text; }
        }
    }

    protected class KitSlot {
        int x, y, slotId;
        KitStack stack; // Nullable
        KitSlot(int x, int y, int slotId) {
            this.x = x; this.y = y; this.slotId = slotId;
        }
        boolean isMouseOver(double mx, double my) {
            return mx >= x && my >= y && mx < x + 18 && my < y + 18;
        }
    }
}