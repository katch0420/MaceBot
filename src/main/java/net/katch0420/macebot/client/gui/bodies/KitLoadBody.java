package net.katch0420.macebot.client.gui.bodies;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.networking.packets.c2s.KitSyncC2SPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Kit load options body. Replaces KitLoadScreen.
 * Lets the user choose target (MaceBot / Player / All Players)
 * and modifier options (Unbreaking, Mending, Unbreakable) before
 * sending the load request.
 */
@Environment(EnvType.CLIENT)
public class KitLoadBody extends Body {

    private Kit kitData;

    private int panelX, panelY, panelWidth, panelHeight, margin, textHeight;

    // Options state
    private String  target     = "MACEBOT";
    private boolean unbreaking  = false;
    private boolean mending     = false;
    private boolean unbreakable = false;

    // Layout for hit-testing (we draw toggles manually, no widget)
    private int targetRowY, targetBtnW, targetBtnH;
    private int checkboxStartY;

    private Button loadButton;
    private Button cancelButton;

    @Override
    public Text getLabel() { return Text.of("Load Kit"); }

    public void setKit(Kit kit) { this.kitData = kit; }

    @Override
    public void init() {
        super.init();
        margin     = s(10, 8);
        textHeight = getTextRenderer().fontHeight;
        panelX     = x; panelY = y;
        panelWidth = availableWidth; panelHeight = availableHeight;

        targetBtnW = s(82, 64);
        targetBtnH = s(18, 14);

        // Load / Cancel buttons at bottom
        int btnW = s(100, 80), btnH = s(18, 14);
        int bottomY = panelY + panelHeight - btnH - margin;

        loadButton = Button.builder()
                .position(panelX + margin, bottomY)
                .size(btnW, btnH)
                .baseLabel(Text.of("Load"))
                .backgroundColor(theme.success())
                .foregroundColor(0xFFFFFFFF)
                .borderColor(-1)
                .hoverColor(theme.success() + 0xFF101010)
                .holdColor(theme.success() + 0xFF202020)
                .onClick(b -> confirmLoad())
                .build();
        addDrawableChild(loadButton);

        cancelButton = Button.builder()
                .position(panelX + panelWidth - margin - btnW, bottomY)
                .size(btnW, btnH)
                .baseLabel(Text.of("Cancel"))
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> parentScreen.navigateBack())
                .build();
        addDrawableChild(cancelButton);
    }

    private void confirmLoad() {
        if (kitData == null) return;
        ClientPlayNetworking.send(KitSyncC2SPacket.loadKit(
                kitData.getId(), target, unbreaking, mending, unbreakable));
        parentScreen.navigateBack();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);

        // Target buttons
        int tY = targetRowY;
        int bx = panelX + margin;
        String[] targets = {"MACEBOT", "MYSELF", "ALL_PLAYERS"};
        for (String t : targets) {
            if (mx >= bx && mx <= bx + targetBtnW && my >= tY && my <= tY + targetBtnH) {
                target = t;
                return true;
            }
            bx += targetBtnW + s(4, 3);
        }

        // Checkboxes
        int boxSize = s(12, 10);
        int cx = panelX + margin;
        if (mx >= cx && mx <= cx + boxSize) {
            if (my >= checkboxStartY && my <= checkboxStartY + boxSize)          { unbreaking  = !unbreaking;  return true; }
            if (my >= checkboxStartY + s(24,19) && my <= checkboxStartY + s(36,29)) { mending   = !mending;    return true; }
            if (my >= checkboxStartY + s(48,38) && my <= checkboxStartY + s(60,48)) { unbreakable = !unbreakable; return true; }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        c.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, theme.body_background());

        int y = panelY + margin;

        // Title
        String title = kitData != null
                ? "Load: " + kitData.getDisplayName().replace('&', '\u00A7')
                : "Load Kit";
        c.drawText(getTextRenderer(), Text.literal(title), panelX + margin, y, theme.body_label(), true);
        y += textHeight + margin;

        // Separator
        c.drawHorizontalLine(panelX + margin, panelX + panelWidth - margin, y, theme.panel_separator());
        y += s(8, 6);

        // ── Target ────────────────────────────────────────────────────────
        c.drawText(getTextRenderer(), Text.of("Load to"), panelX + margin, y, theme.body_value(), true);
        y += textHeight + s(6, 4);
        targetRowY = y;

        String[] targets     = { "MACEBOT", "MYSELF",  "ALL_PLAYERS" };
        String[] targetLabels = { "MaceBot", "Player", "All Players" };
        int bx = panelX + margin;
        for (int i = 0; i < targets.length; i++) {
            boolean active = targets[i].equals(target);
            boolean hov = mx >= bx && mx <= bx + targetBtnW && my >= y && my <= y + targetBtnH;
            int bg = active ? theme.accent() : (hov ? theme.body_button_background() + 0xFF101010 : theme.body_button_background());
            c.fill(bx, y, bx + targetBtnW, y + targetBtnH, bg);
            drawBorder(c, bx, y, targetBtnW, targetBtnH, active ? theme.accent_hover() : theme.body_border());
            int lw = getTextRenderer().getWidth(targetLabels[i]);
            c.drawText(getTextRenderer(), Text.of(targetLabels[i]),
                    bx + (targetBtnW - lw) / 2, y + (targetBtnH - textHeight) / 2,
                    0xFFFFFFFF, true);
            bx += targetBtnW + s(4, 3);
        }
        y += targetBtnH + margin;

        // Separator
        c.drawHorizontalLine(panelX + margin, panelX + panelWidth - margin, y, theme.panel_separator());
        y += s(8, 6);

        // ── Modifiers ─────────────────────────────────────────────────────
        c.drawText(getTextRenderer(), Text.of("Modifiers"), panelX + margin, y, theme.body_value(), true);
        y += textHeight + s(6, 4);
        checkboxStartY = y;

        renderCheckbox(c, mx, my, "Unbreaking III", panelX + margin, y, unbreaking); y += s(24, 19);
        renderCheckbox(c, mx, my, "Mending",        panelX + margin, y, mending);    y += s(24, 19);
        renderCheckbox(c, mx, my, "Unbreakable Tag",panelX + margin, y, unbreakable);y += s(24, 19) + margin;

        // Note
        c.drawHorizontalLine(panelX + margin, panelX + panelWidth - margin, y, theme.panel_separator());
        y += s(6, 4);
        c.drawText(getTextRenderer(), Text.of("Modifiers apply to all damageable items in the kit."),
                panelX + margin, y, theme.body_value(), true);

        // Buttons
        loadButton.render(c, mx, my, d);
        cancelButton.render(c, mx, my, d);
    }

    private void renderCheckbox(DrawContext c, int mx, int my, String label, int x, int y, boolean checked) {
        int boxSize = s(12, 10);
        boolean hov = mx >= x && mx <= x + boxSize && my >= y && my <= y + boxSize;
        c.fill(x, y, x + boxSize, y + boxSize, hov ? theme.body_button_background() : theme.body_background());
        drawBorder(c, x, y, boxSize, boxSize, checked ? theme.accent() : theme.body_border());
        if (checked) {
            c.fill(x + 2, y + 5, x + 5, y + 8, theme.accent());
            c.fill(x + 4, y + 3, x + 10, y + 6, theme.accent());
        }
        c.drawText(getTextRenderer(), Text.of(label), x + boxSize + s(5, 4),
                y + (boxSize - textHeight) / 2, theme.body_label(), true);
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