package net.katch0420.macebot.client.gui.bodies.popup;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.client.gui.screens.popup.StringEditor;
import net.katch0420.macebot.client.gui.screens.popup.StyledTextEditor;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.networking.packets.c2s.KitSyncC2SPacket;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class KitMenuPopup extends PopupScreen {

    private final Kit kit;
    private final Runnable onChanged; // called after save/delete/duplicate so parent list refreshes

    // ── Layout ────────────────────────────────────────────────────────────────
    private int margin;
    private int titleH;
    private int rowH;
    private int btnH;
    private int btnW;
    private int sectionY; // Y where action buttons start

    public KitMenuPopup(Screen parent, Kit kit, Runnable onChanged) {
        super(parent);
        this.kit       = kit;
        this.onChanged = onChanged;
        this.title     = Text.literal("Kit");
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        margin = 8;
        btnH   = s(16, 13);
        btnW   = (Math.max(220, Math.min(width / 3, 320)) - margin * 2 - margin) / 2;
        rowH   = btnH + margin / 2;

        titleH  = textRenderer.fontHeight + margin * 2;

        // Rows: Name, Icon, divider, 3 rows of 2 buttons each (6 buttons total)
        int nameRowH    = textRenderer.fontHeight + margin;
        int iconRowH    = textRenderer.fontHeight + margin;
        int dividerH    = margin;
        int actionRows  = 3; // 3 rows × 2 buttons
        int actionsH    = actionRows * (btnH + margin / 2);

        popupW = Math.max(220, Math.min(width / 3, 320));
        popupH = titleH + nameRowH + iconRowH + dividerH + actionsH + margin * 2;

        popupX = (width  - popupW) / 2;
        popupY = (height - popupH) / 2;

        int currentY = popupY + titleH + margin;

        // Name row: label + Edit button right-aligned
        int editBtnW = s(36, 28);
        addDrawableChild(Button.builder()
                .position(popupX + popupW - margin - editBtnW, currentY)
                .size(editBtnW, s(14, 11))
                .baseLabel(Text.of("Edit"))
                .activeSupplier(kit::isCustom)
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> openNameEditor())
                .build());
        currentY += textRenderer.fontHeight + margin;

        // Icon row: label + Edit button right-aligned
        addDrawableChild(Button.builder()
                .position(popupX + popupW - margin - editBtnW, currentY)
                .size(editBtnW, s(14, 11))
                .baseLabel(Text.of("Edit"))
                .backgroundColor(theme.body_button_background())
                .foregroundColor(theme.body_button_foreground())
                .borderColor(-1)
                .activeSupplier(kit::isCustom)
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> openIconEditor())
                .build());
        currentY += textRenderer.fontHeight + margin + margin; // divider gap

        sectionY = currentY;

        // Action buttons — 2 per row
        int col1 = popupX + margin;
        int col2 = popupX + margin * 2 + btnW;

        // Row 1: Save | Load
        addDrawableChild(actionBtn("Save",      col1, currentY, false, this::save, kit::isCustom));
        addDrawableChild(actionBtn("Load",      col2, currentY, false, this::load, () -> true));
        currentY += rowH;

        // Row 2: Edit | Duplicate
        addDrawableChild(actionBtn("Edit",      col1, currentY, false, this::edit, kit::isCustom));
        addDrawableChild(actionBtn("Duplicate", col2, currentY, false, this::duplicate, () -> true));
        currentY += rowH;

        // Row 3: View | Delete
        addDrawableChild(actionBtn("View",      col1, currentY, false, this::view, () -> true));
        addDrawableChild(actionBtn("Delete",    col2, currentY, true,  this::delete, kit::isCustom));
    }

    private Button actionBtn(String label, int bx, int by, boolean danger, Runnable action, Supplier<Boolean> s) {
        int bg = danger ? theme.danger() : theme.body_button_background();
        int fg = danger ? 0xFFFFFFFF    : theme.body_button_foreground();
        return Button.builder()
                .position(bx, by)
                .size(btnW, btnH)
                .baseLabel(Text.of(label))
                .backgroundColor(bg).foregroundColor(fg).borderColor(-1)
                .hoverColor(bg + 0xFF101010).holdColor(bg + 0xFF202020)
                .activeSupplier(s)
                .onClick(b -> action.run())
                .build();
    }

    /** Scale helper — delegates to client GUI scale; fall back to base value. */
    private int s(int base, int min) {
        // Use Minecraft's GUI scale factor
        double scale = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaleFactor();
        int scaled = (int)(base * Math.min(1.65, Math.max(0.70, scale / 2.0)));
        return Math.max(min, scaled);
    }

    // ── Sub-popup openers ─────────────────────────────────────────────────────

    private void openNameEditor() {
        net.minecraft.client.MinecraftClient.getInstance().setScreen(
                new StyledTextEditor(
                        this,
                        Text.literal("Edit Kit Name"),
                        kit.getDisplayName().replace('\u00A7', '&'),
                        newName -> {
                            kit.setDisplayName(newName.replace('&', '\u00A7'));
                            if (onChanged != null) onChanged.run();
                        }
                )
        );
    }

    private void openIconEditor() {
        MinecraftClient.getInstance().setScreen(
                new StringEditor(
                        this,
                        Text.literal("Edit Icon (item id)"),
                        kit.getIconId() != null ? kit.getIconId().getPath() : "",
                        newIcon -> {
                            kit.setIconItem(newIcon.trim().isEmpty() ? null : validateIconInput(newIcon,kit.getIconId()));
                            if (onChanged != null) onChanged.run();
                        }
                )
        );
    }

    private Identifier validateIconInput(String input, Identifier original) {
        if (input == null || input.trim().isEmpty()) {
            return original; // keep old value
        }
        try {
            Identifier id = Identifier.of(input.trim());
            // Check if item exists
            if (Registries.ITEM.containsId(id)) {
                return id;
            } else {
                return original; // item not found
            }
        } catch (Exception e) {
            // Illegal identifier format
            return original;
        }
    }


    // ── Actions ───────────────────────────────────────────────────────────────

    private void save() {
        ClientPlayNetworking.send(KitSyncC2SPacket.syncKit(kit));
        if (onChanged != null) onChanged.run();
        close();
    }

    private void load() {
        MinecraftClient.getInstance().setScreen(
                new KitLoadPopup(this, kit));
    }

    private void edit() {
        MaceBotClient.mainFrame.openKitEdior(kit);
        close();
    }

    private void duplicate() {
        ClientPlayNetworking.send(KitSyncC2SPacket.duplicateKit(kit.getId()));
        if (onChanged != null) onChanged.run();
        close();
    }

    private void view() {
        MinecraftClient.getInstance().setScreen( new KitViewPopup(this, kit));
    }

    private void delete() {
        if (!kit.isCustom()) return;
        ClientPlayNetworking.send(KitSyncC2SPacket.deleteKit(kit.getId()));
        if (onChanged != null) onChanged.run();
        close();
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

        // Badge (Custom / Built-in) right-aligned in title bar
        String badge   = kit.isCustom() ? "Custom" : "Built-in";
        int    badgeC  = kit.isCustom() ? theme.success() : theme.accent();
        c.drawText(textRenderer, Text.of(badge),
                popupX + popupW - margin - textRenderer.getWidth(badge),
                popupY + (titleH - textRenderer.fontHeight) / 2, badgeC, false);

        // Body
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        int y = popupY + titleH + margin;

        // ── Name row ──────────────────────────────────────────────────────────
        c.drawText(textRenderer, Text.of("Name:"), popupX + margin, y, theme.body_label(), false);
        // Parsed name (with color codes) next to the label
        int nameX = popupX + margin + textRenderer.getWidth("Name: ");
        c.drawText(textRenderer,
                LegacyText.parse(kit.getDisplayName()),
                nameX, y, theme.body_value(), false);
        y += textRenderer.fontHeight + margin;

        // ── Icon row ──────────────────────────────────────────────────────────
        c.drawText(textRenderer, Text.of("Icon:"), popupX + margin, y, theme.body_label(), false);
        // Try to render the item icon
        String iconId = kit.getIconId().getPath();
        if (iconId != null && !iconId.isEmpty()) {
            try {
                net.minecraft.item.Item item = Registries.ITEM.get(Identifier.of(iconId));
                c.drawItem(new ItemStack(item), popupX + margin + textRenderer.getWidth("Icon: "), y - 4);
            } catch (Exception ignored) {}
            int iconTextX = popupX + margin + textRenderer.getWidth("Icon: ") + 18 + 2;
            String shortId = iconId.contains(":") ? iconId.split(":")[1] : iconId;
            c.drawText(textRenderer, Text.of(shortId), iconTextX, y, theme.body_value(), false);
        } else {
            c.drawText(textRenderer, Text.of("(none)"),
                    popupX + margin + textRenderer.getWidth("Icon: "), y, theme.body_value() & 0x88FFFFFF, false);
        }
        y += textRenderer.fontHeight + margin;

        // Divider before action buttons
        c.fill(popupX + margin, y, popupX + popupW - margin, y + 1, theme.panel_separator());

        drawPopupBorder(c, theme.accent());
    }
}