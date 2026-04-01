package net.katch0420.macebot.main.kits.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ItemBrowserWidget extends ClickableWidget {

    // ── Colours — same palette as the rest of the GUI ─────────────────────────
    private static final int COL_BG           = 0xFF1A1A2A; // panel background (dark navy)
    private static final int COL_BORDER       = 0xFF5566AA; // panel border
    private static final int COL_HDR_BG       = 0xFF2A2A4A; // category header fill
    private static final int COL_HDR_ACCENT   = 0xFF5566AA; // category header bottom line
    private static final int COL_HDR_TEXT     = 0xFFFFFFFF; // category header text
    private static final int COL_SLOT_NORMAL  = 0xFF252530; // idle slot bg
    private static final int COL_SLOT_HOVER   = 0xFF3A3A5A; // hovered slot bg
    private static final int COL_SLOT_BORDER  = 0xFF333345; // idle slot border
    private static final int COL_SLOT_BORD_HV = 0xFF4455AA; // hovered slot border
    private static final int COL_SCROLL_TRACK = 0xFF111120; // scrollbar track
    private static final int COL_SCROLL_THUMB = 0xFF555577; // idle scrollbar thumb
    private static final int COL_SCROLL_THB_H = 0xFF8888AA; // hovered/dragging thumb

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int ITEM_SIZE      = 18;
    private static final int ITEM_PAD       = 1;
    private static final int HDR_H          = 16; // category header height
    private static final int SCROLLBAR_W    = 6;
    private static final int SCROLLBAR_PAD  = 3; // gap between items and scrollbar
    private static final int MARGIN         = 6;  // inner padding

    // ── State ─────────────────────────────────────────────────────────────────
    private int columnsPerRow;
    private int displayRows;

    private final List<BrowserEntry> allEntries = new ArrayList<>();
    private final Consumer<ItemStack> onItemClick;

    private int  scrollOffset = 0;
    private int  maxScroll    = 0;
    private boolean isDragging        = false;

    // ── Construction ──────────────────────────────────────────────────────────

    public ItemBrowserWidget(int x, int y, Consumer<ItemStack> onItemClick) {
        super(x, y, 0, 0, Text.empty());
        this.onItemClick = onItemClick;
        initializeAllItems();
    }

    private void initializeAllItems() {
        addCategory("Combat",            ItemGroups.COMBAT);
        addCategory("Tools & Utilities", ItemGroups.TOOLS);
        addCategory("Food & Drinks",     ItemGroups.FOOD_AND_DRINK);
        addCategory("Functional Blocks", ItemGroups.FUNCTIONAL);
        addCategory("Ingredients",       ItemGroups.INGREDIENTS);
        addCategory("Natural Blocks",    ItemGroups.NATURAL);
        addCategory("Building Blocks",   ItemGroups.BUILDING_BLOCKS);
        addCategory("Colored Blocks",    ItemGroups.COLORED_BLOCKS);
        addCategory("Redstone",          ItemGroups.REDSTONE);
        addCategory("Spawn Eggs",        ItemGroups.SPAWN_EGGS);
    }

    private void addCategory(String name, RegistryKey<ItemGroup> groupKey) {
        allEntries.add(new BrowserEntry(name));
        ItemGroup group = Registries.ITEM_GROUP.get(groupKey);
        if (group == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        group.updateEntries(new ItemGroup.DisplayContext(
                mc.world.getEnabledFeatures(), true, mc.world.getRegistryManager()
        ));
        group.getDisplayStacks().forEach(stack -> {
            if (!stack.isEmpty()) allEntries.add(new BrowserEntry(stack.copy()));
        });
    }

    // ── Public resize API ─────────────────────────────────────────────────────

    /**
     * Called from KitEditorScreen on init and resize.
     * Computes columns/rows from the available left-side space and repositions the widget.
     *
     * @param screenW       full screen width
     * @param screenH       full screen height
     * @param invGridW      width of the inventory grid in the centre (backgroundWidth)
     * @param browserMargin right margin between this widget and the inventory grid
     */
    public void resize(int screenW, int screenH, int invGridW, int browserMargin) {
        // Available horizontal space = left half of screen minus the margin
        int availW = (screenW - invGridW) / 2 - browserMargin;

        // How many item columns fit?  at least 3, at most 9
        columnsPerRow = Math.max(3, Math.min(9,
                (availW - MARGIN * 2 - SCROLLBAR_W - SCROLLBAR_PAD) / (ITEM_SIZE + ITEM_PAD)));

        // How many rows fit vertically? at least 4, at most 12
        displayRows = Math.max(4, Math.min(12,
                (screenH - MARGIN * 2) / (ITEM_SIZE + ITEM_PAD)));

        int w = MARGIN * 2 + columnsPerRow * (ITEM_SIZE + ITEM_PAD) - ITEM_PAD
                + SCROLLBAR_PAD + SCROLLBAR_W;
        int h = MARGIN * 2 + displayRows   * (ITEM_SIZE + ITEM_PAD) - ITEM_PAD;

        setWidth(w);
        setHeight(h);

        // Flush against the right edge of the left panel, vertically centred
        setX(availW - browserMargin - w + browserMargin);
        setY((MinecraftClient.getInstance().getWindow().getScaledHeight() - h) / 2);

        calculateMaxScroll();
    }

    // ── Scroll helpers ────────────────────────────────────────────────────────

    private void calculateMaxScroll() {
        int totalRows  = 0;
        int rowItems   = 0;

        for (BrowserEntry e : allEntries) {
            if (e.isHeader) {
                if (rowItems > 0) { totalRows++; rowItems = 0; }
                totalRows++; // header itself
            } else {
                rowItems++;
                if (rowItems >= columnsPerRow) { totalRows++; rowItems = 0; }
            }
        }
        if (rowItems > 0) totalRows++;

        maxScroll    = Math.max(0, totalRows - displayRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Panel background + border
        ctx.fill(getX(), getY(), getX() + width, getY() + height, COL_BG);
        ctx.drawBorder(getX(), getY(), width, height, COL_BORDER);

        int contentX = getX() + MARGIN;
        int contentY = getY() + MARGIN;
        int contentW = width - MARGIN * 2 - SCROLLBAR_PAD - SCROLLBAR_W;
        int contentH = height - MARGIN * 2;

        ctx.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);

        int yPos     = contentY - scrollOffset * (ITEM_SIZE + ITEM_PAD);
        int xPos     = contentX;
        int rowItems = 0;

        ItemStack tooltipStack = null;
        int tooltipX = 0, tooltipY = 0;

        for (BrowserEntry entry : allEntries) {
            entry.lastX = -1;
            entry.lastY = -1;

            if (entry.isHeader) {
                // Flush pending partial row
                if (rowItems > 0) {
                    yPos += ITEM_SIZE + ITEM_PAD;
                    rowItems = 0;
                    xPos = contentX;
                }

                // Only draw if in visible range
                if (yPos + HDR_H >= contentY && yPos < contentY + contentH) {
                    // header fill + inner highlight + bottom accent line
                    ctx.fill(contentX, yPos, contentX + contentW, yPos + HDR_H, COL_HDR_BG);
                    ctx.fill(contentX, yPos + HDR_H - 1,
                            contentX + contentW, yPos + HDR_H, COL_HDR_ACCENT);
                    ctx.drawText(MinecraftClient.getInstance().textRenderer,
                            entry.headerText, contentX + 5,
                            yPos + (HDR_H - 8) / 2, COL_HDR_TEXT, false);
                }

                yPos += HDR_H + ITEM_PAD;
                xPos = contentX;

            } else {
                if (yPos + ITEM_SIZE >= contentY && yPos < contentY + contentH) {
                    boolean hov = mouseX >= xPos && mouseX < xPos + ITEM_SIZE
                            && mouseY >= yPos && mouseY < yPos + ITEM_SIZE
                            && mouseX >= contentX && mouseX < contentX + contentW
                            && mouseY >= contentY && mouseY < contentY + contentH;

                    ctx.fill(xPos, yPos, xPos + ITEM_SIZE, yPos + ITEM_SIZE,
                            hov ? COL_SLOT_HOVER : COL_SLOT_NORMAL);
                    ctx.drawBorder(xPos, yPos, ITEM_SIZE, ITEM_SIZE,
                            hov ? COL_SLOT_BORD_HV : COL_SLOT_BORDER);

                    ctx.drawItem(entry.itemStack, xPos + 1, yPos + 1);
                    ctx.drawStackOverlay(MinecraftClient.getInstance().textRenderer,
                            entry.itemStack, xPos + 1, yPos + 1);

                    entry.lastX = xPos;
                    entry.lastY = yPos;

                    if (hov) {
                        tooltipStack = entry.itemStack;
                        tooltipX = mouseX;
                        tooltipY = mouseY;
                    }
                }

                rowItems++;
                xPos += ITEM_SIZE + ITEM_PAD;

                if (rowItems >= columnsPerRow) {
                    yPos += ITEM_SIZE + ITEM_PAD;
                    rowItems = 0;
                    xPos = contentX;
                }
            }
        }

        ctx.disableScissor();

        // Scrollbar (drawn outside scissor so it's always visible)
        if (maxScroll > 0) renderScrollbar(ctx, mouseX, mouseY);

        // Tooltip (drawn outside scissor so it's not clipped)
        if (tooltipStack != null) {
            ctx.drawItemTooltip(MinecraftClient.getInstance().textRenderer,
                    tooltipStack, tooltipX, tooltipY);
        }
    }

    private void renderScrollbar(DrawContext ctx, int mouseX, int mouseY) {
        int sbX = getX() + width - SCROLLBAR_W - 1;
        int sbY = getY() + MARGIN;
        int sbH = height - MARGIN * 2;

        // Track
        ctx.fill(sbX, sbY, sbX + SCROLLBAR_W, sbY + sbH, COL_SCROLL_TRACK);
        ctx.drawBorder(sbX, sbY, SCROLLBAR_W, sbH, COL_BORDER);

        // Thumb
        int thumbH = Math.max(16, (displayRows * sbH) / (maxScroll + displayRows));
        int thumbY = sbY + (sbH - thumbH) * scrollOffset / maxScroll;

        boolean hov = mouseX >= sbX && mouseX <= sbX + SCROLLBAR_W
                && mouseY >= thumbY && mouseY <= thumbY + thumbH;

        ctx.fill(sbX + 1, thumbY, sbX + SCROLLBAR_W - 1, thumbY + thumbH,
                (hov || isDragging) ? COL_SCROLL_THB_H : COL_SCROLL_THUMB);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {

        if(!isMouseOver(mx,my)) return false;
        int contentX = getX() + MARGIN;
        int contentY = getY() + MARGIN;
        int contentW = width - MARGIN * 2 - SCROLLBAR_PAD - SCROLLBAR_W;
        int contentH = height - MARGIN * 2;

        // Item click
        if (mx >= contentX && mx < contentX + contentW
                && my >= contentY && my < contentY + contentH) {
            for (BrowserEntry entry : allEntries) {
                if (!entry.isHeader && entry.lastX >= 0
                        && mx >= entry.lastX && mx < entry.lastX + ITEM_SIZE
                        && my >= entry.lastY && my < entry.lastY + ITEM_SIZE) {
                    ItemStack stack = entry.itemStack.copy();
                    // Right-click → max stack; left-click → count 1
                    stack.setCount(button == 1 && stack.isStackable() ? stack.getMaxCount() : 1);
                    onItemClick.accept(stack);
                    return true;
                }
            }
        }

        // Scrollbar click
        int sbX = getX() + width - SCROLLBAR_W - 1;
        int sbY = getY() + MARGIN;
        int sbH = height - MARGIN * 2;

        if (maxScroll > 0 && mx >= sbX && mx <= sbX + SCROLLBAR_W
                && my >= sbY && my <= sbY + sbH && button == 0) {
            scrollOffset = clampScroll(calculateScrollFromMouse(my, sbY, sbH));
            isDragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (isDragging) { isDragging = false;}
        if(isMouseOver(mx,my)){
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (isDragging && maxScroll > 0 && button == 0) {
            int sbY = getY() + MARGIN;
            int sbH = height - MARGIN * 2;
            scrollOffset = clampScroll(calculateScrollFromMouse(my, sbY, sbH));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (isMouseOver(mx, my) && maxScroll > 0) {
            scrollOffset = clampScroll(scrollOffset - (int) vAmount);
            return true;
        }
        return false;
    }

    public boolean isMouseOver(double mx, double my) {
        return mx >= getX() && mx < getX() + width && my >= getY() && my < getY() + height;
    }

    private int calculateScrollFromMouse(double mouseY, int sbY, int sbH) {
        int thumbH = Math.max(16, (displayRows * sbH) / (maxScroll + displayRows));
        float relY = (float)(mouseY - sbY - thumbH / 2f);
        float pct  = relY / (sbH - thumbH);
        return Math.round(pct * maxScroll);
    }

    private int clampScroll(int v) { return Math.max(0, Math.min(maxScroll, v)); }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, "Item Browser");
    }

    // ── Entry type ────────────────────────────────────────────────────────────

    private static class BrowserEntry {
        final boolean  isHeader;
        final String   headerText;
        final ItemStack itemStack;
        int lastX = -1, lastY = -1;

        BrowserEntry(String headerText) {
            this.isHeader   = true;
            this.headerText = headerText;
            this.itemStack  = null;
        }

        BrowserEntry(ItemStack itemStack) {
            this.isHeader   = false;
            this.headerText = null;
            this.itemStack  = itemStack;
        }
    }
}