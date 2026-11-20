package net.katch0420.macebot.client.gui.bodies.popup;

import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.Scroller;
//? <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Searchable, scrollable list of every registered enchantment. This is the
 * "direct way" to add an enchantment from {@link ItemEditorPopup} — pick a
 * row, dial in a level (no vanilla max-level cap, up to 255), hit Add.
 * <p>
 * Enchantments are data-driven, so entries come from the client's dynamic
 * registry manager via {@link ItemEditorPopup#enchantmentRegistry()} rather
 * than a static {@code Registries.ENCHANTMENT} lookup.
 */
public class EnchantPickerPopup extends PopupScreen {

    /** Hard ceiling only — not limited to each enchantment's vanilla max level. */
    private static final int ABSOLUTE_MAX_LEVEL = 255;

    private final Set<Holder<Enchantment>> alreadyAdded;
    private final BiConsumer<Holder<Enchantment>, Integer> onPick;

    private List<Holder<Enchantment>> allEntries;
    private List<Holder<Enchantment>> filtered;

    private final List<EnchantmentEntry> entries = new ArrayList<>();

    private String search = "";
    private EditBox searchField;

    private Holder<Enchantment> selected;
    private int selectedLevel = 1;

    private final Scroller scroller = new Scroller();

    // ── Layout ────────────────────────────────────────────────────────────────
    private int margin;
    private int titleH;
    private int searchH;
    private int rowH;
    private int listY;
    private int listH;
    private int footerH;
    private int readoutH;   // reserved strip for the "selected x level" readout, so it never overlaps the list or buttons
    private int btnH;

    public EnchantPickerPopup(Screen parent, Set<Holder<Enchantment>> alreadyAdded,
                              BiConsumer<Holder<Enchantment>, Integer> onPick) {
        super(parent);
        this.alreadyAdded = alreadyAdded;
        this.onPick = onPick;
        this.title = Component.literal("Add Enchantment");
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        margin   = 8;
        btnH     = 16;
        rowH     = font.lineHeight + 6;
        titleH   = font.lineHeight + margin * 2;
        searchH  = font.lineHeight + 8;
        readoutH = font.lineHeight + 4;
        footerH  = btnH + margin * 2 + readoutH;

        popupW = Math.max(240, Math.min(width * 2 / 5, 320));
        popupH = Math.max(220, Math.min(height * 3 / 4, 300));

        popupX = (width  - popupW) / 2;
        popupY = (height - popupH) / 2;

        listY = popupY + titleH + margin + searchH + margin;
        listH = popupY + popupH - footerH - listY - margin;

        Registry<Enchantment> registry = ItemEditorPopup.enchantmentRegistry();
        allEntries = registry.stream()
                .map(registry::wrapAsHolder)
                .sorted(Comparator.comparing(this::idString))
                .toList();
        applyFilter();

        searchField = new EditBox(font, popupX + margin, popupY + titleH + margin,
                popupW - margin * 2, searchH, Component.literal("Search..."));
        searchField.setMaxLength(64);
        searchField.setValue(search);
        searchField.setResponder(s -> { search = s; applyFilter();});
        searchField.setFocused(true);
        addRenderableWidget(searchField);
        setFocused(searchField);

        scroller.setArea(popupX, listY, listH, filtered.size() * rowH);
        initEntries();
        initFooter();
    }

    private void initEntries() {
        entries.clear();
        int y = listY;
        for (int i = 0; i < filtered.size(); i++) entries.add(new EnchantmentEntry(popupX + margin, y + i * rowH, popupW - margin * 2, rowH, filtered.get(i)));
    }

    @Override
    protected void setInitialFocus() {
        super.setInitialFocus(searchField);
    }

    private String idString(Holder<Enchantment> entry) {
        return entry.value().toString();
    }

    private void applyFilter() {
        String q = search.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filtered = allEntries;
        } else {
            filtered = allEntries.stream()
                    .filter(e -> idString(e).toLowerCase(Locale.ROOT).contains(q)
                            || e.value().description().copy().getString().toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }
        if (selected != null && !filtered.contains(selected)) {
            selected = null;
        }
        initEntries();
    }

    private String shortName(Holder<Enchantment> entry) {
        Enchantment ench = entry.value();
        if (ench == null) return idString(entry);
        return Enchantment.getFullname(entry, 1).getString();
    }

    private void initFooter() {
        // Buttons sit below the reserved readout strip so the readout never overlaps them.
        int fy = popupY + popupH - footerH + margin + readoutH;

        int stepW = 20;
        int addW = 70;
        int cancelW = 60;
        int gap = margin / 2;

        addRenderableWidget(Button.builder()
                .position(popupX + margin, fy).size(stepW, btnH)
                .baseLabel(Component.nullToEmpty("-"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .activeSupplier(() -> selectedLevel > 1 && selected != null)
                .onClick(b -> { selectedLevel = Math.max(1, selectedLevel - 1); repositionElements(); })
                .build());

        addRenderableWidget(Button.builder()
                .position(popupX + margin + stepW + gap, fy).size(stepW, btnH)
                .baseLabel(Component.nullToEmpty("+"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .activeSupplier(() -> selectedLevel < ABSOLUTE_MAX_LEVEL && selected != null)
                .onClick(b -> { selectedLevel = Math.min(ABSOLUTE_MAX_LEVEL, selectedLevel + 1); repositionElements(); })
                .build());

        int rightBlock = cancelW + gap + addW;
        int addX = popupX + popupW - margin - rightBlock;

        addRenderableWidget(Button.builder()
                .position(addX, fy).size(addW, btnH)
                .baseLabel(Component.nullToEmpty("Add Lvl " + selectedLevel))
                .backgroundColor(theme.success()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.success() + 0xFF101010).holdColor(theme.success() + 0xFF202020)
                .activeSupplier(() -> selected != null)
                .onClick(b -> {
                    onPick.accept(selected, selectedLevel);
                    onClose();
                })
                .build());

        addRenderableWidget(Button.builder()
                .position(addX + addW + gap, fy).size(cancelW, btnH)
                .baseLabel(Component.nullToEmpty("Cancel"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> onClose())
                .build());
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
        if(scroller.mouseClicked(mx,my,popupX + popupW - 5, 3)) return true;
        if(super.mouseClicked(
                //? if >=1.21.9 {
                c, bl
                //?} else
                //mx, my, btn
        )) return true;
        int offset = scroller.getOffset();
        for(EnchantmentEntry e: entries){
            if(e.isMouseOver(mx,my, offset)){
                selected = e.ench;
                selectedLevel = 1;
                // Rebuild so the footer's "Add Lvl N" label and button active-states
                // reflect the freshly-selected enchantment immediately.
                repositionElements();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (scroller.mouseScrolled(mx, my, popupX, popupW, v, rowH)) {
            return true;
        }
        return super.mouseScrolled(mx, my, h, v);
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(
                //? if >=1.21.9 {
                keyEvent
                //?} else
                //keyCode, scanCode, modifiers
        );
    }

    // ── Render ────────────────────────────────────────────────────────────────
    //? >1.21.11 {
    /*@Override
    public void renderPopupScreen(GuiGraphicsExtractor c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());
        c.text(font, title,
                popupX + margin, popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);

        // Body
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        if (filtered.isEmpty()) {
            c.text(font, Component.nullToEmpty("No enchantments match \"" + search + "\""),
                    popupX + margin, listY, theme.body_value() & 0x88FFFFFF, false);
        } else {
            // Right edge must match the rows' actual right edge (popupX + popupW - margin),
            // not popupX + popupW - 2*margin - clipping one margin too early cut off the
            // right side of every row (and visually collided with the scrollbar track).
            c.enableScissor(popupX + margin, listY, popupX + popupW - margin, listY + listH);
            entries.forEach(e -> e.render(c,mx,my,scroller.getOffset()));
            c.disableScissor();
        }

        scroller.render(c, popupX + popupW - 5, 3, theme.panel_separator(), theme.accent());

        // Footer divider + reserved readout strip (always laid out, so nothing below it ever shifts)
        int footerY = popupY + popupH - footerH;
        c.fill(popupX, footerY, popupX + popupW, footerY + 1, theme.panel_separator());
        if (selected != null) {
            String label = shortName(selected) + "  x" + selectedLevel;
            c.text(font, Component.nullToEmpty(label),
                    popupX + margin, footerY + 2, theme.body_label(), false);
        }

        drawPopupBorder(c, theme.accent());
    }

    protected class EnchantmentEntry{
        int x,y,w,h;
        Holder<Enchantment> ench;

        public EnchantmentEntry(int x, int y, int w, int h, Holder<Enchantment> ench) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.ench = ench;
        }

        public void render(GuiGraphicsExtractor c, double mx, double my, int offsetY) {
            int drawY = y - offsetY;

            // Skip if completely outside scissor area
            if (drawY + h < popupY || drawY > popupY + popupH) {
                return;
            }

            boolean isSelected = ench.equals(selected);
            boolean hovered = isMouseOver(mx, my, offsetY);
            boolean already = alreadyAdded.contains(ench);

            int bg = isSelected
                    ? (theme.accent() & 0x00FFFFFF | 0x66000000)
                    : hovered
                    ? (theme.accent() & 0x00FFFFFF | 0x33000000)
                    : (theme.body_background() & 0x00FFFFFF | 0x55000000);

            c.fill(x, drawY + 1, x + w, drawY + h, bg);

            String label = (already ? "✓ " : "") + ench.value().description().copy().getString();
            // BUGFIX: this used to draw at the un-scrolled `y` instead of `drawY`, so the
            // label stayed frozen in place while the highlighted row scrolled underneath it.
            c.text(font, Component.nullToEmpty(label), x + margin, drawY + (h - font.lineHeight) / 2,
                    (isSelected || hovered) ? theme.body_value() : theme.body_key(), false);
        }
        boolean isMouseOver(double mx, double my, int offsetY) {
            return mx >= x && my >= y - offsetY && mx < x + w && my < y - offsetY + h;
        }
    }

    *///?}
    //? <=1.21.11 {
    @Override
    public void renderPopupScreen(GuiGraphics c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());
        c.drawString(font, title,
                popupX + margin, popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);

        // Body
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH, popupX + popupW, popupY + titleH + 1, theme.panel_separator());

        if (filtered.isEmpty()) {
            c.drawString(font, Component.nullToEmpty("No enchantments match \"" + search + "\""),
                    popupX + margin, listY, theme.body_value() & 0x88FFFFFF, false);
        } else {
            // Right edge must match the rows' actual right edge (popupX + popupW - margin),
            // not popupX + popupW - 2*margin - clipping one margin too early cut off the
            // right side of every row (and visually collided with the scrollbar track).
            c.enableScissor(popupX + margin, listY, popupX + popupW - margin, listY + listH);
            entries.forEach(e -> e.render(c,mx,my,scroller.getOffset()));
            c.disableScissor();
        }

        scroller.render(c, popupX + popupW - 5, 3, theme.panel_separator(), theme.accent());

        // Footer divider + reserved readout strip (always laid out, so nothing below it ever shifts)
        int footerY = popupY + popupH - footerH;
        c.fill(popupX, footerY, popupX + popupW, footerY + 1, theme.panel_separator());
        if (selected != null) {
            String label = shortName(selected) + "  x" + selectedLevel;
            c.drawString(font, Component.nullToEmpty(label),
                    popupX + margin, footerY + 2, theme.body_label(), false);
        }

        drawPopupBorder(c, theme.accent());
    }

    protected class EnchantmentEntry{
        int x,y,w,h;
        Holder<Enchantment> ench;

        public EnchantmentEntry(int x, int y, int w, int h, Holder<Enchantment> ench) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.ench = ench;
        }

        public void render(GuiGraphics c, double mx, double my, int offsetY) {
            int drawY = y - offsetY;

            // Skip if completely outside scissor area
            if (drawY + h < popupY || drawY > popupY + popupH) {
                return;
            }

            boolean isSelected = ench.equals(selected);
            boolean hovered = isMouseOver(mx, my, offsetY);
            boolean already = alreadyAdded.contains(ench);

            int bg = isSelected
                    ? (theme.accent() & 0x00FFFFFF | 0x66000000)
                    : hovered
                    ? (theme.accent() & 0x00FFFFFF | 0x33000000)
                    : (theme.body_background() & 0x00FFFFFF | 0x55000000);

            c.fill(x, drawY + 1, x + w, drawY + h, bg);

            String label = (already ? "✓ " : "") + ench.value().description().copy().getString();
            // BUGFIX: this used to draw at the un-scrolled `y` instead of `drawY`, so the
            // label stayed frozen in place while the highlighted row scrolled underneath it.
            c.drawString(font, Component.nullToEmpty(label), x + margin, drawY + (h - font.lineHeight) / 2,
                    (isSelected || hovered) ? theme.body_value() : theme.body_key(), false);
        }
        boolean isMouseOver(double mx, double my, int offsetY) {
            return mx >= x && my >= y - offsetY && mx < x + w && my < y - offsetY + h;
        }
    }
    //?}
}