package net.katch0420.macebot.client.gui.bodies;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.katch0420.macebot.client.MaceBotClient.theme;

/**
 * Read-only kit inventory viewer. Renders all 41 slots (hotbar, inventory,
 * armor, offhand) of a kit in a layout mirroring the vanilla inventory,
 * scaled to fit the Body's available space. No slot interactions.
 * Re-uses the existing KitViewScreen slot layout math, adapted for Body.
 */
@Environment(EnvType.CLIENT)
public class KitViewerBody extends Body {

    private Kit kitData;
    private final Map<Integer, ItemStack> slots = new HashMap<>();

    private int panelX, panelY, panelWidth, panelHeight;
    private int margin, textHeight;

    private int slotSize, slotPad;
    private int blockX, armorSlotX, mainGridX;
    private int invRowsY, hotbarY, armorTopY;

    private int hoveredSlot = -1;
    private static final int ARMOR_LABEL_W = 32;
    private static final int COL_GAP       = 10;

    @Override
    public Text getLabel() { return Text.of("Kit Viewer"); }

    public void setKit(Kit kit) {
        this.kitData = kit;
        loadSlots();
    }

    private void loadSlots() {
        slots.clear();
        if (kitData == null) return;
        // Server-side kit - get stacks
        Kit kit = KitRegistry.get(kitData.getId());
        if (kit == null) return;
        kit.getItems().forEach((slot, ks) -> {
            ItemStack stack = ks.toStack();
            if (stack != null && !stack.isEmpty()) slots.put(slot, stack);
        });
    }

    @Override
    public void init() {
        super.init();
        margin     = s(8, 6);
        textHeight = getTextRenderer().fontHeight;
        panelX     = x; panelY = y;
        panelWidth = availableWidth; panelHeight = availableHeight;
        loadSlots();
        calculateLayout();
    }

    private void calculateLayout() {
        int availW = panelWidth  - ARMOR_LABEL_W - COL_GAP - margin * 2;
        int availH = panelHeight - margin * 2 - textHeight * 2 - 8;

        int maxByW = availW / 10;
        int maxByH = availH / 6;
        slotSize   = Math.max(10, Math.min(20, Math.min(maxByW, maxByH)));
        slotPad    = Math.max(2, slotSize / 5);

        int mainGridW = 9 * (slotSize + slotPad) - slotPad;
        int totalW    = ARMOR_LABEL_W + slotSize + COL_GAP + mainGridW;

        blockX      = panelX + (panelWidth - totalW) / 2;
        armorSlotX  = blockX + ARMOR_LABEL_W;
        mainGridX   = armorSlotX + slotSize + COL_GAP;

        int invH      = 3 * (slotSize + slotPad) - slotPad;
        int hotbarGap = slotPad * 3;
        int gridH     = invH + hotbarGap + slotSize;
        int armorH    = 5 * (slotSize + slotPad) - slotPad;
        int totalH    = Math.max(armorH, gridH);
        int topY      = panelY + textHeight + margin + (panelHeight - textHeight - margin * 2 - totalH) / 2;

        invRowsY  = topY;
        hotbarY   = invRowsY + 3 * (slotSize + slotPad) - slotPad + slotPad * 3;
        armorTopY = topY;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        c.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, theme.body_background());

        // Title
        String title = kitData != null
                ? "Viewing: " + kitData.getDisplayName().replace('&', '\u00A7')
                : "Kit Viewer";
        c.drawText(getTextRenderer(), Text.literal(title), panelX + margin, panelY + margin, theme.body_label(), true);

        if (kitData == null) {
            c.drawCenteredTextWithShadow(getTextRenderer(), "No kit selected",
                    panelX + panelWidth / 2, panelY + panelHeight / 2, theme.body_value());
            return;
        }

        hoveredSlot = -1;
        ItemStack tooltipStack = ItemStack.EMPTY;

        // ── Labels ───────────────────────────────────────────────────────
        int labelY = invRowsY - textHeight - 2;
        c.drawText(getTextRenderer(), Text.of("Armor"), armorSlotX, labelY, theme.body_value(), true);
        c.drawText(getTextRenderer(), Text.of("Inventory"), mainGridX, labelY, theme.body_value(), true);

        // ── Main inventory (slots 9-35) ───────────────────────────────────
        for (int slot = 9; slot <= 35; slot++) {
            int col = (slot - 9) % 9;
            int row = (slot - 9) / 9;
            int sx  = mainGridX + col * (slotSize + slotPad);
            int sy  = invRowsY  + row * (slotSize + slotPad);
            ItemStack stack = slots.getOrDefault(slot, ItemStack.EMPTY);
            boolean hov = isHovered(mx, my, sx, sy);
            if (hov) { hoveredSlot = slot; if (!stack.isEmpty()) tooltipStack = stack; }
            renderSlot(c, stack, sx, sy, hov);
        }

        // ── Hotbar separator ──────────────────────────────────────────────
        int sepY = hotbarY - slotPad * 3 / 2;
        c.fill(mainGridX, sepY, mainGridX + 9 * (slotSize + slotPad) - slotPad, sepY + 1, theme.panel_separator());
        String hbLabel = "Hotbar";
        c.drawText(getTextRenderer(), Text.of(hbLabel),
                mainGridX + (9 * (slotSize + slotPad) - slotPad - getTextRenderer().getWidth(hbLabel)) / 2,
                sepY + 2, theme.body_value(), true);

        // ── Hotbar (slots 0-8) ────────────────────────────────────────────
        for (int slot = 0; slot <= 8; slot++) {
            int sx = mainGridX + slot * (slotSize + slotPad);
            ItemStack stack = slots.getOrDefault(slot, ItemStack.EMPTY);
            boolean hov = isHovered(mx, my, sx, hotbarY);
            if (hov) { hoveredSlot = slot; if (!stack.isEmpty()) tooltipStack = stack; }
            renderSlot(c, stack, sx, hotbarY, hov);
        }

        // ── Armor + offhand (slots 36-40) ─────────────────────────────────
        int[] armorSlots     = { 39, 38, 37, 36, 40 };
        String[] armorLabels = { "Head", "Chest", "Legs", "Feet", "Off" };
        for (int i = 0; i < armorSlots.length; i++) {
            int slot = armorSlots[i];
            int sy   = armorTopY + i * (slotSize + slotPad);
            if (i == 4) sy += slotPad * 2; // small gap before offhand
            ItemStack stack = slots.getOrDefault(slot, ItemStack.EMPTY);
            boolean hov = isHovered(mx, my, armorSlotX, sy);
            if (hov) { hoveredSlot = slot; if (!stack.isEmpty()) tooltipStack = stack; }
            renderSlot(c, stack, armorSlotX, sy, hov);

            String lbl = armorLabels[i];
            int lx = blockX + ARMOR_LABEL_W - getTextRenderer().getWidth(lbl) - 3;
            int ly = sy + (slotSize - textHeight) / 2;
            c.drawText(getTextRenderer(), Text.of(lbl), lx, ly, theme.body_value(), true);

            if (i == 3) {
                int divY = sy + slotSize + slotPad;
                c.fill(blockX, divY, armorSlotX + slotSize, divY + 1, theme.panel_separator());
            }
        }

        // ── Summary ───────────────────────────────────────────────────────
        String summary = slots.size() + " / 41 slots filled";
        c.drawText(getTextRenderer(), Text.of(summary),
                panelX + (panelWidth - getTextRenderer().getWidth(summary)) / 2,
                panelY + panelHeight - margin - textHeight, theme.body_value(), true);

        // ── Tooltip ───────────────────────────────────────────────────────
        if (!tooltipStack.isEmpty()) {
            List<Text> lines = tooltipStack.getTooltip(
                    Item.TooltipContext.DEFAULT, null, TooltipType.Default.BASIC);
            c.drawTooltip(getTextRenderer(), lines, mx, my);
        }
    }

    private boolean isHovered(int mx, int my, int sx, int sy) {
        return mx >= sx && mx < sx + slotSize && my >= sy && my < sy + slotSize;
    }

    private void renderSlot(DrawContext c, ItemStack stack, int sx, int sy, boolean hov) {
        c.fill(sx, sy, sx + slotSize, sy + slotSize,
                hov ? 0xFF3A3A5A : theme.body_background());
        drawBorder(c, sx, sy, slotSize, slotSize,
                hov ? theme.accent() : theme.body_border());

        if (stack == null || stack.isEmpty()) return;

        if (slotSize < 18) {
            float scale = (slotSize - 2) / 16.0f;
            //? if >=1.21.6 {
            /*c.getMatrices().pushMatrix();
            c.getMatrices().translate(sx + 1, sy + 1);
            c.getMatrices().scale(scale, scale);
            c.drawItem(stack, 0, 0);
            c.getMatrices().popMatrix();
            *///?}
            //? if <=1.21.5 {
            c.getMatrices().push();
            c.getMatrices().translate(sx + 1, sy + 1, 0);
            c.getMatrices().scale(scale, scale, 1f);
            c.drawItem(stack, 0, 0);
            c.getMatrices().pop();
            //?}
        } else {
            c.drawItem(stack, sx + 1, sy + 1);
            //? if >=1.21.2 {
            /*c.drawStackOverlay(getTextRenderer(), stack, sx + 1, sy + 1);
             *///?} else
            c.drawItemInSlot(getTextRenderer(), stack, sx + 1, sy + 1);
        }
    }

    private void drawBorder(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) { return false; }
}