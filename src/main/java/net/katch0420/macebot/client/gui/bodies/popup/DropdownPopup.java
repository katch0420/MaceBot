package net.katch0420.macebot.client.gui.bodies.popup;

import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.Scroller;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DropdownPopup extends PopupScreen {
    private final int targetX, targetY;
    private final List<String> options;
    private final Consumer<String> onSelect;

    private final Scroller scroller = new Scroller();
    private final List<Button> optionButtons = new ArrayList<>();

    public DropdownPopup(Screen parent, int targetX, int targetY, List<String> options, Consumer<String> onSelect) {
        super(parent);
        this.targetX = targetX;
        this.targetY = targetY;
        this.options = options;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();
        popupW = 140;
        // Cap max height to 150px, otherwise fit to content
        popupH = Math.min(150, options.size() * 16 + 4);
        popupX = Math.min(targetX, width - popupW - 5); // Prevent clipping off right edge
        popupY = targetY - popupH;

        optionButtons.clear();
        for (int i = 0; i < options.size(); i++) {
            String opt = options.get(i);
            optionButtons.add(Button.builder() //[cite: 10]
                    .position(popupX + 2, popupY + 2 + (i * 16))
                    .size(popupW - 8, 14)
                    .baseLabel(Text.literal(opt))
                    .backgroundColor(theme.body_background())
                    .hoverColor(theme.panel_separator())
                    .onClick(b -> { onSelect.accept(opt); close(); })
                    .build());
        }

        scroller.setArea(popupX, popupY, popupH, options.size() * 16); //[cite: 6]
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (scroller.mouseClicked(mx, my, popupX + popupW - 4, 4)) return true; //[cite: 6]

        // Offset click testing manually since buttons are outside standard layout
        int offset = scroller.getOffset(); //[cite: 6]
        for (Button b : optionButtons) {
            b.setY(b.getY() - offset); // Temporary shift for hit detection
            if (b.mouseClicked(mx, my, btn)) return true;
            b.setY(b.getY() + offset); // Reset
        }

        // Close if clicked outside
        if (mx < popupX || mx > popupX + popupW || my < popupY || my > popupY + popupH) {
            close();
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (scroller.mouseDragged(my)) return true; //[cite: 6]
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (scroller.mouseScrolled(mx, my, popupX, popupW, v, 16)) return true; //[cite: 6]
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public void renderPopupScreen(DrawContext c, int mx, int my) {
        c.fill(popupX, popupY, popupX + popupW, popupY + popupH, theme.body_background());

        c.enableScissor(popupX, popupY, popupX + popupW, popupY + popupH);
        int offset = scroller.getOffset(); //[cite: 6]

        for (int i = 0; i < optionButtons.size(); i++) {
            Button b = optionButtons.get(i);
            int renderY = popupY + 2 + (i * 16) - offset;

            // Only render if visible
            if (renderY + 14 > popupY && renderY < popupY + popupH) {
                b.setY(renderY); // Update exact rendering position
                b.render(c, mx, my, 0); //[cite: 10]
            }
        }
        c.disableScissor();

        scroller.render(c, popupX + popupW - 4, 4, theme.panel_separator(), theme.accent()); //[cite: 6]
        drawPopupBorder(c, theme.accent());
    }
}