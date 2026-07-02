package net.katch0420.macebot.client.gui.bodies.popup;

import net.katch0420.macebot.client.gui.bodies.popup.DropdownPopup;
import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.client.gui.screens.popup.StyledTextEditor;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.Scroller;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemEditorPopup extends PopupScreen {
    private final KitStack kitStack;
    private final Consumer<KitStack> onSave;

    private final Scroller compScroller = new Scroller();
    private List<String> currentComponentsDisplay = new ArrayList<>();

    private int btnH = 18;
    private Button editNameBtn;

    public ItemEditorPopup(Screen parent, KitStack kitStack, Consumer<KitStack> onSave) {
        super(parent);
        this.kitStack = kitStack;
        this.onSave = onSave;
        this.title = Text.literal("Edit Item: " + kitStack.getItemId());
    }

    @Override
    protected void init() {
        super.init();
        popupW = Math.max(300, width / 2);
        popupH = 260;
        popupX = (width - popupW) / 2;
        popupY = (height - popupH) / 2;

        refreshComponentList();

        int startY = popupY + 30;

        // Small Edit Name Button (Positioned dynamically in render, but initialized here)
        editNameBtn = Button.builder()
                .position(popupX, startY) // X updated in render
                .size(30, 14)
                .baseLabel(Text.literal("Edit"))
                .backgroundColor(theme.body_button_background())
                .onClick(b -> editName())
                .build();
        addDrawableChild(editNameBtn);

        // Add Component Dropdown (Pulls from Registry)
        addDrawableChild(Button.builder()
                .position(popupX + popupW - 110, popupY + popupH - 30)
                .size(100, btnH)
                .baseLabel(Text.literal("Add Component..."))
                .backgroundColor(theme.success())
                .onClick(b -> openComponentDropdown(b.getX(), b.getY()))
                .build());

        // Enchant Shortcut Dropdown (Pulls from Registry)
        addDrawableChild(Button.builder()
                .position(popupX + popupW - 220, popupY + popupH - 30)
                .size(100, btnH)
                .baseLabel(Text.literal("Add Enchant..."))
                .backgroundColor(theme.accent())
                .onClick(b -> openEnchantmentDropdown(b.getX(), b.getY()))
                .build());

        // Save & Close
        addDrawableChild(Button.builder()
                .position(popupX + 10, popupY + popupH - 30)
                .size(80, btnH)
                .baseLabel(Text.literal("Save & Close"))
                .backgroundColor(theme.body_button_background())
                .onClick(b -> { onSave.accept(kitStack); close(); })
                .build());
    }

    private void refreshComponentList() {
        currentComponentsDisplay.clear();
        ComponentMap components = kitStack.itemData.components; //[cite: 2]

        for (ComponentType<?> type : components.getTypes()) {
            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(type);
            if (id != null) {
                Object value = components.get(type);
                // Simplify the string output for the UI
                String valStr = value != null ? value.toString() : "null";
                if (valStr.length() > 30) valStr = valStr.substring(0, 27) + "...";

                currentComponentsDisplay.add("§b" + id.getPath() + "§7: " + valStr);
            }
        }
        compScroller.setArea(popupX + 10, popupY + 60, popupH - 100, currentComponentsDisplay.size() * 12); //[cite: 6]
    }

    private void editName() {
        String currentName = kitStack.toStack().getName().getString(); //[cite: 2]
        MinecraftClient.getInstance().setScreen(new StyledTextEditor(this, Text.literal("Display Name"), currentName, newName -> {
            ItemStack temp = kitStack.toStack();
            temp.set(DataComponentTypes.CUSTOM_NAME, Text.literal(newName.replace('&', '§')));
            this.kitStack.itemData = KitStack.fromStack(temp, (Integer) kitStack.slot).itemData;
            refreshComponentList();
        }));
    }

    private void openComponentDropdown(int x, int y) {
        // Pull dynamically from the game's Data Component Registry
        List<String> allComponents = Registries.DATA_COMPONENT_TYPE.getIds().stream()
                .map(Identifier::getPath)
                .sorted()
                .toList();

        MinecraftClient.getInstance().setScreen(new DropdownPopup(this, x, y, allComponents, selection -> {
            // TODO: Implement actual component parsing based on selection.
            // For now, this just adds an unbreakable component as a proof-of-concept fallback.
            if (selection.equals("unbreakable")) {
                ItemStack temp = kitStack.toStack();
                KitStack.addUnbreakableComponent(temp); //[cite: 2]
                this.kitStack.itemData = KitStack.fromStack(temp, (Integer) kitStack.slot).itemData;
                refreshComponentList();
            }
        }));
    }

    private void openEnchantmentDropdown(int x, int y) {
        // Pull dynamically from the client world's Dynamic Registry for Enchantments
        if (client.world == null) return;
        List<String> allEnchants = client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                .getIds().stream()
                .map(Identifier::getPath)
                .sorted()
                .toList();

        MinecraftClient.getInstance().setScreen(new DropdownPopup(this, x, y, allEnchants, selection -> {
            // Apply enchantment (Simplified for example; requires building a new component map entry in practice)
            Identifier enchId = Identifier.of(Identifier.DEFAULT_NAMESPACE, selection);
            client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(enchId).ifPresent(entry -> {
                ItemStack temp = kitStack.toStack();
                temp.addEnchantment(entry, 1);
                this.kitStack.itemData = KitStack.fromStack(temp, (Integer) kitStack.slot).itemData; //[cite: 2]
                refreshComponentList();
            });
        }));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (compScroller.mouseClicked(mx, my, popupX + popupW - 15, 4)) return true; //[cite: 6]
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (compScroller.mouseDragged(my)) return true; //[cite: 6]
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (compScroller.mouseScrolled(mx, my, popupX + 10, popupW - 20, v, 12)) return true; //[cite: 6]
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public void renderPopupScreen(DrawContext c, int mx, int my) {
        c.fill(popupX, popupY, popupX + popupW, popupY + 24, theme.header_background());
        c.drawText(textRenderer, title, popupX + 10, popupY + 8, theme.header_foreground(), false);
        c.fill(popupX, popupY + 24, popupX + popupW, popupY + popupH, popupBodyColor());

        // --- Render Display Name Row ---
        int startY = popupY + 30;
        String nameStr = kitStack.toStack().getName().getString(); //[cite: 2]
        Text nameText = Text.literal("Name: §f" + nameStr);
        c.drawText(textRenderer, nameText, popupX + 10, startY + 3, theme.body_label(), false);

        // Update Edit button position based on name length
        int nameWidth = textRenderer.getWidth(nameText);
        editNameBtn.setPosition(popupX + 10 + nameWidth + 8, startY); //[cite: 10]

        // --- Render Components List ---
        int treeY = popupY + 60;
        int treeH = popupH - 100;
        c.fill(popupX + 10, treeY, popupX + popupW - 10, treeY + treeH, theme.panel_separator());
        c.drawText(textRenderer, Text.literal("Current Components:"), popupX + 12, treeY - 12, theme.body_label(), false);

        c.enableScissor(popupX + 10, treeY, popupX + popupW - 10, treeY + treeH);
        int currentY = treeY + 4 - compScroller.getOffset(); //[cite: 6]

        for (String compDisplay : currentComponentsDisplay) {
            if (currentY + 12 > treeY && currentY < treeY + treeH) {
                c.drawText(textRenderer, Text.literal(compDisplay), popupX + 14, currentY, 0xFFFFFF, false);
            }
            currentY += 12;
        }
        c.disableScissor();

        compScroller.render(c, popupX + popupW - 15, 4, theme.panel_separator(), theme.accent()); //[cite: 6]

        drawPopupBorder(c, theme.accent());
    }
}