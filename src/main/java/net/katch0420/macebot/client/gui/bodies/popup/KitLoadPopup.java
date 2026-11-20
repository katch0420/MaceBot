package net.katch0420.macebot.client.gui.bodies.popup;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.ColorHelper;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.network.packets.c2s.KitSyncC2SPacket;
import net.katch0420.macebot.main.utils.LegacyText;
//? if <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class KitLoadPopup extends PopupScreen {

    private final Kit kit;

    // Options
    private String  target     = "MACEBOT";
    private boolean unbreaking  = false;
    private boolean mending     = false;
    private boolean unbreakable = false;

    // Layout
    private int margin;
    private int titleH;
    private int btnH;
    private int targetBtnW;
    private int targetBtnH;
    private int checkboxSize;

    // Hit-test Y positions (set in init)
    private int targetRowY;
    private int cbUnbreakingY;
    private int cbMendingY;
    private int cbUnbreakableY;

    public KitLoadPopup(Screen parent, Kit kit) {
        super(parent);
        this.kit   = kit;
        this.title = Component.literal("Load Kit");
        if(kit.isCustom()) target = "MYSELF";
    }

    @Override
    protected void init() {
        super.init();

        margin       = 8;
        btnH         = 16;
        targetBtnW   = 70;
        targetBtnH   = 16;
        checkboxSize = 11;

        titleH = font.lineHeight + margin * 2;

        int labelH  = font.lineHeight;
        int rowGap  = margin / 2;

        // Height: title + target label + target buttons + gap + modifiers label
        //         + 3 checkboxes + divider + note + confirm buttons + padding
        popupW = Math.max(260, Math.min(width * 2 / 5, 340));
        popupH = titleH
                + margin + labelH                    // "Load to" label
                + rowGap + targetBtnH                // target buttons
                + margin + 1 + margin                // separator
                + labelH                             // "Modifiers" label
                + rowGap + (labelH + rowGap) * 3    // 3 checkboxes
                + margin + 1 + margin                // separator
                + labelH                             // note
                + margin + btnH + margin;            // confirm/cancel buttons

        popupX = (width  - popupW) / 2;
        popupY = (height - popupH) / 2;

        // Load / Cancel at bottom
        int bW2   = (popupW - margin * 3) / 2;
        int bY    = popupY + popupH - btnH - margin;

        addRenderableWidget(Button.builder()
                .position(popupX + margin, bY).size(bW2, btnH)
                .baseLabel(Component.nullToEmpty("Load"))
                .backgroundColor(theme.success()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.success() + 0xFF101010).holdColor(theme.success() + 0xFF202020)
                .onClick(b -> confirmLoad()).build());

        addRenderableWidget(Button.builder()
                .position(popupX + margin * 2 + bW2, bY).size(bW2, btnH)
                .baseLabel(Component.nullToEmpty("Cancel"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> onClose()).build());

        // Compute hit-test Y values
        int y       = popupY + titleH + margin;
        y          += font.lineHeight + margin / 2; // "Load to" label
        targetRowY  = y;
        y          += targetBtnH + margin + 1 + margin;    // target btns + sep
        y          += font.lineHeight + margin / 2; // "Modifiers" label
        cbUnbreakingY  = y;
        cbMendingY     = y + font.lineHeight + margin / 2;
        cbUnbreakableY = y + (font.lineHeight + margin / 2) * 2;
    }

    private void confirmLoad() {
        ClientPlayNetworking.send(KitSyncC2SPacket.loadKit(
                kit.getId(), target, unbreaking, mending, unbreakable));
        onClose();
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(
            //? if >=1.21.9 {
            MouseButtonEvent c, boolean bl
            //?} else
            //double mx, double my, int btn
    ) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int btn = c.button();
        if (btn != 0) return super.mouseClicked(
                //? if >=1.21.9 {
                c, bl
                //?} else
                //mx, my, btn
        );

        // Target buttons (hit-tested manually — no widget, just fills)
        String[] targets = { "MACEBOT", "MYSELF", "ALL_PLAYERS" };
        int bx = popupX + margin;
        for (String t : targets) {
            if (mx >= bx && mx < bx + targetBtnW && my >= targetRowY && my < targetRowY + targetBtnH) {
                target = t;
                return true;
            }
            bx += targetBtnW + margin / 2;
        }

        // Checkboxes
        int cx = popupX + margin;
        if (mx >= cx && mx < cx + checkboxSize) {
            if (my >= cbUnbreakingY  && my < cbUnbreakingY  + checkboxSize) { unbreaking  = !unbreaking;  return true; }
            if (my >= cbMendingY     && my < cbMendingY     + checkboxSize) { mending     = !mending;     return true; }
            if (my >= cbUnbreakableY && my < cbUnbreakableY + checkboxSize) { unbreakable = !unbreakable; return true; }
        }

        return super.mouseClicked(
                //? if >=1.21.9 {
                c, bl
                //?} else
                //mx, my, btn
        );
    }

    // ── Render ────────────────────────────────────────────────────────────────
    //? if >1.21.11 {
    /*@Override
    public void renderPopupScreen(GuiGraphicsExtractor c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());
        c.text(font, title,
                popupX + margin, popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);
        // Kit name in title bar right side
        c.text(font,
                LegacyText.parse(kit.getDisplayName()),
                popupX + popupW - margin - font.width(LegacyText.parse(kit.getDisplayName()).getString()),
                popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);

        // Body
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        int y = popupY + titleH + margin;

        // ── Target ────────────────────────────────────────────────────────────
        c.text(font, Component.nullToEmpty("Load to"), popupX + margin, y, theme.body_label(), false);
        y += font.lineHeight + margin / 2;

        String[] targets     = { "MACEBOT",  "MYSELF",  "ALL_PLAYERS" };
        String[] targetLbls  = { "MaceBot",  "Player",  "All Players" };
        int bx = popupX + margin;
        for (int i = 0; i < targets.length; i++) {
            boolean disabled = i == 0 && kit.isCustom();
            boolean active = targets[i].equals(target) && !disabled;
            boolean hov    = mx >= bx && mx < bx + targetBtnW && my >= y && my < y + targetBtnH;
            int bg = disabled ? ColorHelper.darken(theme.body_button_background(), 0.5f) :(active ? theme.accent() : hov ? theme.body_button_background() + 0xFF101010 : theme.body_button_background());
            c.fill(bx, y, bx + targetBtnW, y + targetBtnH, bg);
            if (active) c.fill(bx, y, bx + 2, y + targetBtnH, theme.accent_hover());
            drawBorder(c, bx, y, targetBtnW, targetBtnH, disabled ? ColorHelper.darken(theme.body_border(), 0.5f) : (active ? theme.accent_hover() : theme.body_border()));
            int lw = font.width(targetLbls[i]);
            c.text(font, Component.nullToEmpty(targetLbls[i]),
                    bx + (targetBtnW - lw) / 2, y + (targetBtnH - font.lineHeight) / 2,
                    0xFFFFFFFF, false);
            bx += targetBtnW + margin / 2;
        }
        y += targetBtnH + margin;

        // Separator
        c.fill(popupX + margin, y, popupX + popupW - margin, y + 1, theme.panel_separator());
        y += 1 + margin;

        // ── Modifiers ─────────────────────────────────────────────────────────
        c.text(font, Component.nullToEmpty("Modifiers"), popupX + margin, y, theme.body_label(), false);
        y += font.lineHeight + margin / 2;

        renderCheckbox(c, mx, my, "Unbreaking III",   popupX + margin, y, unbreaking);  y += font.lineHeight + margin / 2;
        renderCheckbox(c, mx, my, "Mending",           popupX + margin, y, mending);     y += font.lineHeight + margin / 2;
        renderCheckbox(c, mx, my, "Unbreakable Tag",   popupX + margin, y, unbreakable); y += font.lineHeight + margin / 2 + margin;

        // Separator
        c.fill(popupX + margin, y, popupX + popupW - margin, y + 1, theme.panel_separator());
        y += 1 + margin;

        // Note
        c.text(font, Component.nullToEmpty("Applies to all damageable items in kit."),
                popupX + margin, y, theme.body_value() & 0xAAFFFFFF, false);

        drawPopupBorder(c, theme.accent());
    }

    private void renderCheckbox(GuiGraphicsExtractor c, int mx, int my, String label, int x, int y, boolean checked) {
        boolean hov = mx >= x && mx < x + checkboxSize && my >= y && my < y + checkboxSize;
        c.fill(x, y, x + checkboxSize, y + checkboxSize,
                hov ? theme.body_button_background() : popupBodyColor());
        drawBorder(c, x, y, checkboxSize, checkboxSize,
                checked ? theme.accent() : theme.body_border());
        if (checked) {
            // Tick marks
            c.fill(x + 2, y + 5, x + 5,  y + 8,  theme.accent());
            c.fill(x + 4, y + 3, x + 10, y + 6,  theme.accent());
        }
        c.text(font, Component.nullToEmpty(label),
                x + checkboxSize + 4,
                y + (checkboxSize - font.lineHeight) / 2,
                theme.body_label(), false);
    }
    *///?}
    //? if <=1.21.11 {
    @Override
    public void renderPopupScreen(GuiGraphics c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());
        c.drawString(font, title,
                popupX + margin, popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);
        // Kit name in title bar right side
        c.drawString(font,
                LegacyText.parse(kit.getDisplayName()),
                popupX + popupW - margin - font.width(LegacyText.parse(kit.getDisplayName()).getString()),
                popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);

        // Body
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        int y = popupY + titleH + margin;

        // ── Target ────────────────────────────────────────────────────────────
        c.drawString(font, Component.nullToEmpty("Load to"), popupX + margin, y, theme.body_label(), false);
        y += font.lineHeight + margin / 2;

        String[] targets     = { "MACEBOT",  "MYSELF",  "ALL_PLAYERS" };
        String[] targetLbls  = { "MaceBot",  "Player",  "All Players" };
        int bx = popupX + margin;
        for (int i = 0; i < targets.length; i++) {
            boolean disabled = i == 0 && kit.isCustom();
            boolean active = targets[i].equals(target) && !disabled;
            boolean hov    = mx >= bx && mx < bx + targetBtnW && my >= y && my < y + targetBtnH;
            int bg = disabled ? ColorHelper.darken(theme.body_button_background(), 0.5f) :(active ? theme.accent() : hov ? theme.body_button_background() + 0xFF101010 : theme.body_button_background());
            c.fill(bx, y, bx + targetBtnW, y + targetBtnH, bg);
            if (active) c.fill(bx, y, bx + 2, y + targetBtnH, theme.accent_hover());
            drawBorder(c, bx, y, targetBtnW, targetBtnH, disabled ? ColorHelper.darken(theme.body_border(), 0.5f) : (active ? theme.accent_hover() : theme.body_border()));
            int lw = font.width(targetLbls[i]);
            c.drawString(font, Component.nullToEmpty(targetLbls[i]),
                    bx + (targetBtnW - lw) / 2, y + (targetBtnH - font.lineHeight) / 2,
                    0xFFFFFFFF, false);
            bx += targetBtnW + margin / 2;
        }
        y += targetBtnH + margin;

        // Separator
        c.fill(popupX + margin, y, popupX + popupW - margin, y + 1, theme.panel_separator());
        y += 1 + margin;

        // ── Modifiers ─────────────────────────────────────────────────────────
        c.drawString(font, Component.nullToEmpty("Modifiers"), popupX + margin, y, theme.body_label(), false);
        y += font.lineHeight + margin / 2;

        renderCheckbox(c, mx, my, "Unbreaking III",   popupX + margin, y, unbreaking);  y += font.lineHeight + margin / 2;
        renderCheckbox(c, mx, my, "Mending",           popupX + margin, y, mending);     y += font.lineHeight + margin / 2;
        renderCheckbox(c, mx, my, "Unbreakable Tag",   popupX + margin, y, unbreakable); y += font.lineHeight + margin / 2 + margin;

        // Separator
        c.fill(popupX + margin, y, popupX + popupW - margin, y + 1, theme.panel_separator());
        y += 1 + margin;

        // Note
        c.drawString(font, Component.nullToEmpty("Applies to all damageable items in kit."),
                popupX + margin, y, theme.body_value() & 0xAAFFFFFF, false);

        drawPopupBorder(c, theme.accent());
    }

    private void renderCheckbox(GuiGraphics c, int mx, int my, String label, int x, int y, boolean checked) {
        boolean hov = mx >= x && mx < x + checkboxSize && my >= y && my < y + checkboxSize;
        c.fill(x, y, x + checkboxSize, y + checkboxSize,
                hov ? theme.body_button_background() : popupBodyColor());
        drawBorder(c, x, y, checkboxSize, checkboxSize,
                checked ? theme.accent() : theme.body_border());
        if (checked) {
            // Tick marks
            c.fill(x + 2, y + 5, x + 5,  y + 8,  theme.accent());
            c.fill(x + 4, y + 3, x + 10, y + 6,  theme.accent());
        }
        c.drawString(font, Component.nullToEmpty(label),
                x + checkboxSize + 4,
                y + (checkboxSize - font.lineHeight) / 2,
                theme.body_label(), false);
    }
    //?}
}