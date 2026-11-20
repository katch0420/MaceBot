package net.katch0420.macebot.client.gui.bodies;
//? if <=1.21.11
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.client.gui.bodies.popup.ItemEditorPopup;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.RenderUtils;
import net.katch0420.macebot.client.utils.Scroller;
import net.katch0420.macebot.main.kits.client.data.ClientPresetRegistry;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.katch0420.macebot.main.network.packets.c2s.KitSyncC2SPacket;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.Minecraft;
//? if <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
//? if >=1.21.11
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KitEditorBody extends Body {
    private int margin;
    private int inventoryPanelWidth;
    private int browserPanelWidth;

    int iX,iY,iW,iH;

    int lmx,lmy;

    int slotSize, slotPad;

    private final Kit original;
    private Kit activeKit;

    private ItemStack cursorStack = ItemStack.EMPTY;

    private ItemBrowser allItemBrowser;
    private ItemBrowser activeBrowser;
    private ItemBrowser presetBrowser;
    private final List<KitSlot> kitSlots = new ArrayList<>();
    private int by;

    public KitEditorBody(Kit original) {
        this.original = original;
        this.activeKit = original.copy();
    }

    @Override
    public void init() {
        clearChildren();
        super.init();
        margin = s(6, 4);

        int minInventoryPanelWidth = 9 * 18 + 2 * margin;

        int remainingWidth = availableWidth - minInventoryPanelWidth - 2 * margin;
        int browserCols = Math.max(1, Math.min(9, remainingWidth / 20)); // 1 pixel to draw Border
        browserPanelWidth = browserCols * 20 + margin;
        inventoryPanelWidth = availableWidth - browserPanelWidth - 3 * margin;

        slotSize = Math.max(16, Math.min(20, inventoryPanelWidth / 11));
        slotPad = Math.max(2, slotSize / 8);

        int browserX = x + inventoryPanelWidth + 2 * margin;

        addDrawableChild(Button.builder().baseLabel(Component.literal("All")).foregroundColor(theme.body_button_foreground()).backgroundColor(theme.body_button_background()).borderColor(theme.body_button_border()).position(browserX, y + margin).size(browserPanelWidth / 2, 15).onClick(b -> switchBrowser()).activeSupplier(() -> activeBrowser != allItemBrowser).build());
        addDrawableChild(Button.builder().baseLabel(Component.literal("Presets")).foregroundColor(theme.body_button_foreground()).backgroundColor(theme.body_button_background()).borderColor(theme.body_button_border()).position(browserX + browserPanelWidth / 2, y + margin).size(browserPanelWidth / 2, 15).onClick(b -> switchBrowser()).activeSupplier(() -> activeBrowser != presetBrowser).build());

        allItemBrowser = new ItemBrowser(browserX, y + margin + 15, browserPanelWidth, availableHeight - 15 - 2 * margin, browserCols);
        presetBrowser = new ItemBrowser(browserX, y + margin + 15, browserPanelWidth, availableHeight - 15 - 2 * margin, browserCols);
        activeBrowser = allItemBrowser;
        populateAllItemBrowser();
        populatePresetBrowser();
        initKitSlots();
        initButtons(by);
    }

    private void initButtons(int by) {
        Map<String, Consumer<Button>> onClick = Map.of("Reset", b -> reset(), "Save", b -> save(), "Cancel", b -> close(), "Copy Inv", b -> copyPlayerInventory());

        int buttonCount = onClick.size();
        int bw = Math.min(80, (inventoryPanelWidth - 2 * margin) / buttonCount);
        int totalWidth = buttonCount * bw + (buttonCount - 1) * margin;

        // Center horizontally relative to inventory panel
        int startX = x + (inventoryPanelWidth - totalWidth) / 2;

        AtomicInteger i = new AtomicInteger();
        onClick.forEach((label, action) -> {
            addDrawableChild(Button.builder().baseLabel(Component.literal(label)).foregroundColor(theme.body_button_foreground()).backgroundColor(theme.body_button_background()).borderColor(theme.body_button_border()).position(startX + i.getAndIncrement() * (bw + margin), by).size(bw, 15).onClick(action).build());
        });
    }

    private void copyPlayerInventory() {
        // MOJANG-VERIFY: Yarn's PlayerInventory -> Mojang's Inventory; getStack(int) -> getItem(int).
        Inventory inv = MaceBotClient.getClientPlayer().getInventory();
        for (int i = 0; i < 41; i++) {
            activeKit.addItem(i, KitStack.fromStack(inv.getItem(i), i));
        }
        initKitSlots();
    }

    private void close() {
        MaceBotClient.mainFrame.setBody(Bodies.KITS);
    }

    private void update(){
        kitSlots.forEach(
                s -> activeKit.items.put(s.slotId,KitStack.fromStack(s.getStack(),s.slotId))
        );
    }

    private void save() {
        update();
        ClientPlayNetworking.send(KitSyncC2SPacket.syncKit(activeKit));
        close();
    }

    private void reset() {
        activeKit = original.copy();
        initKitSlots();
    }

    private void addPreset(ItemStack stack){
        ClientPresetRegistry.add(stack);
        populatePresetBrowser();
    }

    private void removePreset(ItemBrowser.BrowserSlot slot){
        ClientPresetRegistry.remove(slot.stack);
        populatePresetBrowser();
    }

    private void openItemEditor(KitSlot ks){
        if (ks.stack.isEmpty()) return;
        setScreen(new ItemEditorPopup(parent, ks.stack, edited -> {
            ks.stack = edited;
            update();
        }));
    }

    private void setScreen(Screen screen){
        //? if <26.2 {
        Minecraft.getInstance().setScreen(screen);
        //?} else
        //Minecraft.getInstance().setScreenAndShow(screen);
    }

    private void switchBrowser() {
        if (activeBrowser != allItemBrowser) activeBrowser = allItemBrowser;
        else activeBrowser = presetBrowser;
    }

    private void populatePresetBrowser() {
        presetBrowser.groups.clear();
        presetBrowser.groups.add(new Group(Component.literal("Presets"), ClientPresetRegistry.all()).setExpanded());
        presetBrowser.calculateScroll();
        presetBrowser.initSlotLayout();
    }

    @Override
    public Component getLabel() {
        return Component.literal("Kit Editor");
    }

    private void populateAllItemBrowser() {
        allItemBrowser.groups.clear();
        List<Group> groups = new ArrayList<>();
        // MOJANG-VERIFY: method name/signature for refreshing creative-tab contents by feature
        // flags + permission + registry access. tryRebuildTabContents is my best-confidence guess
        // for what Yarn's updateDisplayContext maps to, but double check against your sources.
        CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS,
                //? if >=1.21.11
                Minecraft.getInstance().player.permissions() == LevelBasedPermissionSet.OWNER,
                //? if <1.21.11
                //Minecraft.getInstance().player.hasPermissions(4),
                MaceBotClient.getClientPlayer().level().registryAccess());
        // MOJANG-VERIFY: Yarn's ItemGroups.getGroups() -> guessing CreativeModeTabs.allTabs().
        for (CreativeModeTab g : CreativeModeTabs.allTabs()) {
            if (isInvalidGroup(g)) continue;
            // MOJANG-VERIFY: getDisplayName()/getDisplayItems() naming on CreativeModeTab - not
            // independently confirmed, carried over from Yarn's getDisplayName()/getDisplayStacks().
            groups.add(new Group(g.getDisplayName(), g.getDisplayItems()));
        }
        allItemBrowser.groups.addAll(groups);
        allItemBrowser.calculateScroll();
        allItemBrowser.initSlotLayout();
    }

    private boolean isInvalidGroup(CreativeModeTab s) {
        // MOJANG-VERIFY: Registries.ITEM_GROUP (a Registry<ItemGroup> instance with .get()) is
        // guessed here as BuiltInRegistries.CREATIVE_MODE_TAB - this is the registry-instance
        // class (has .get()), not net.minecraft.core.registries.Registries (which holds
        // ResourceKey<Registry<...>> constants and has no .get()).
        String g = s.getDisplayName().toString();
        String a = Component.translatable("itemGroup.hotbar").toString();
        String b = Component.translatable("itemGroup.search").toString();
        String c = Component.translatable("itemGroup.inventory").toString();

        return Objects.equals(g, a) || Objects.equals(g, b) || Objects.equals(g, c);
    }

    private void initKitSlots() {
        kitSlots.clear();

        int totalWidth = (1 /*armor col*/ + 9 /*main grid*/) * (slotSize + slotPad) - slotPad + margin;
        int totalHeight = Math.max(
                (3 * (slotSize + slotPad)) + (slotPad * 3) + slotSize, // inventory grid + hotbar
                5 * (slotSize + slotPad) // armor column
        );

        // Move layout higher to leave space for tips (reserve ~4 lines of text)
        int reservedTipsHeight = getFont().lineHeight * 4 + margin * 2;
        int invX = x + (inventoryPanelWidth - totalWidth) / 2;
        int invY = y + (availableHeight - totalHeight - reservedTipsHeight) / 2;

        Map<Integer, ItemStack> slotMap = new HashMap<>();
        activeKit.getItems().forEach((i, k) -> slotMap.put(i, k.toStack()));

        // Armor + Offhand (39,38,37,36,40)
        int[] armorSlots = {39, 38, 37, 36, 40};
        for (int i = 0; i < armorSlots.length; i++) {
            int sy = invY + i * (slotSize + slotPad) + (i == 4 ? slotPad * 3 : 0);
            kitSlots.add(new KitSlot(invX, sy, armorSlots[i], slotMap.getOrDefault(armorSlots[i], ItemStack.EMPTY)));
        }

        // Main inventory (9–35)
        int mainGridX = invX + slotSize + margin;
        int mainGridY = invY;
        for (int slot = 9; slot <= 35; slot++) {
            int col = (slot - 9) % 9;
            int row = (slot - 9) / 9;
            int sx = mainGridX + col * (slotSize + slotPad);
            int sy = mainGridY + row * (slotSize + slotPad);
            kitSlots.add(new KitSlot(sx, sy, slot, slotMap.getOrDefault(slot, ItemStack.EMPTY)));
        }

        // Hotbar (0–8)
        int hotbarY = mainGridY + 3 * (slotSize + slotPad) + slotPad * 3;
        for (int slot = 0; slot <= 8; slot++) {
            int sx = mainGridX + slot * (slotSize + slotPad);
            kitSlots.add(new KitSlot(sx, hotbarY, slot, slotMap.getOrDefault(slot, ItemStack.EMPTY)));
        }

        // Buttons just below hotbar, leaving gap for tips
        by = hotbarY + 3 * slotSize + margin;
        iX = invX - slotSize;
        iY = invY - slotSize;
        iW = totalWidth + 2 * slotSize;
        iH = totalHeight + 2 * slotSize;
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
        // MOJANG-VERIFY: Yarn's options.dropKey -> guessing Minecraft's options.keyDrop.
        //? if <=1.21.11 {
        if(keyCode == KeyBindingHelper.getBoundKeyOf(Minecraft.getInstance().options.keyDrop).getValue()){
        //?} else {
        /*if(Minecraft.getInstance().options.keyDrop.matches(keyEvent)){
        *///?}
            for(KitSlot ks: kitSlots){
                if(ks.isMouseOver(lmx,lmy) && !ks.stack.isEmpty()){
                    if(
                            //? if >=1.21.9 {
                            keyEvent.hasControlDown()
                            //?} else
                            //Screen.hasControlDown()
                    ){
                        ks.stack = ItemStack.EMPTY;
                    }
                    ks.stack.shrink(1);
                    return true;
                }
            }
        }
        return super.keyPressed(
                //? if >=1.21.9 {
                keyEvent
                //?} else
                //keyCode, scanCode, modifiers
        );
    }

    @Override
    public boolean mouseClicked(
            //? if >=1.21.9 {
            MouseButtonEvent c, boolean bl
            //?} else
            //double mx, double my, int btn
    ) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int btn = c.button();
        if(super.mouseClicked(
                //? if >=1.21.9 {
                c, bl
                //?} else
                //mx, my, btn
        )){
            return true;
        }
        if (activeBrowser.mouseClicked(mx, my, btn/*? if >=1.21.9 >>*/ ,c /**/)) return true;
        if (activeBrowser.scroller.mouseClicked(mx, my, activeBrowser.bx + browserPanelWidth - 3, 3))
            return true;
        if(mx >= iX && mx < iX + iW && my >= iY && my < iY + iH){
            for(KitSlot ks: kitSlots) if(ks.isMouseOver(mx,my)) return handleKitSlotClick(btn, ks/*? if >=1.21.9 >>*/ ,c /**/);
        } else {
            if (btn == 0) cursorStack = ItemStack.EMPTY;
            if (btn == 1) cursorStack.shrink(1);
        }
        return false;
    }

    private boolean handleKitSlotClick(int b, KitSlot ks
                                       //? if >=1.21.9
                                       , MouseButtonEvent m
    ) {
        //? if >=1.21.9 {
        boolean c = m.hasControlDown(), s = m.hasShiftDown(), a = m.hasAltDown();
        //?} else
        //boolean c = Screen.hasControlDown(), s = Screen.hasShiftDown(), a = Screen.hasAltDown();
        if(!ks.stack.isEmpty()){
            if(c){
                openItemEditor(ks);
                return true;
            }
            if(s){
                ks.stack = ItemStack.EMPTY;
                return true;
            }
            if(a){
                addPreset(ks.stack);
                return true;
            }
            if(isCursorStackEmpty()){
                if(b == 0){
                    cursorStack = ks.stack.copy();
                    ks.stack = ItemStack.EMPTY;
                    return true;
                }
                if(b == 1){
                    cursorStack = ks.stack.copyWithCount(ks.stack.getCount() / 2);
                    ks.stack.setCount(ks.stack.getCount() - cursorStack.getCount());
                    return true;
                }
                if(b == 2){
                    cursorStack = ks.stack.copyWithCount(ks.stack.getMaxStackSize());
                    return true;
                }
            } else {
                // MOJANG-VERIFY: Yarn's ItemStack.areItemsAndComponentsEqual -> guessing
                // Mojang's isSameItemSameComponents.
                if(ItemStack.isSameItemSameComponents(ks.stack,cursorStack)){
                    if(b == 0){
                        if(ks.stack.getCount() >= cursorStack.getCount()){
                            int left = ks.stack.getMaxStackSize() - ks.stack.getCount();
                            if(cursorStack.getCount() > left){
                                ks.stack.setCount(ks.stack.getMaxStackSize());
                                cursorStack.setCount(cursorStack.getCount() - left);
                                return true;
                            } else {
                                ks.stack.setCount(ks.stack.getCount() + cursorStack.getCount());
                                cursorStack = ItemStack.EMPTY;
                                return true;
                            }
                        }
                    }
                    if(b == 1){
                        if(ks.stack.getCount() < ks.stack.getMaxStackSize()){
                            cursorStack.shrink(1);
                            ks.stack.grow(1);
                            return true;
                        }
                    }
                }
            }
        }
        if(!isCursorStackEmpty()){
            ItemStack stack = cursorStack.copy();
            cursorStack = ks.stack.copy();
            ks.stack = stack;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(
            //? if >=1.21.9 {
            MouseButtonEvent c
            //?} else
            //double mx, double my, int btn
    ) {
        if (activeBrowser.scroller.mouseReleased()) {
            return true;
        }
        return super.mouseReleased(
                //? if >=1.21.9 {
                c
                //?} else
                //mx, my, btn
        );
    }

    @Override
    public boolean mouseDragged(
            //? if >=1.21.9 {
            MouseButtonEvent c
            //?} else
            //double mx, double my, int btn
            , double dx, double dy) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int btn = c.button();
        if (btn == 0) if (activeBrowser.scroller.mouseDragged(my)) return true;
        return super.mouseDragged(
                //? if >=1.21.9 {
                c
                //?} else
                //mx, my, btn
                , dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (activeBrowser.scroller.mouseScrolled(mx, my, activeBrowser.bx, browserPanelWidth, vAmount, 20)) return true;
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }
    //? if <=1.21.11 {
    @Override
    public void render(GuiGraphics c, int mx, int my, float d) {
        // Inventory label
        lmx = mx;
        lmy = my;
        ItemStack hoverStack = ItemStack.EMPTY;
        c.drawString(getFont(), Component.literal("Kit: " + activeKit.getDisplayName()),
                x + margin, y + margin, theme.body_foreground(), false);

        // Browser
        if (activeBrowser != null) activeBrowser.render(c, mx, my);

        // Slots
        for (KitSlot slot : kitSlots) {
            if (slot.isMouseOver(mx, my)) hoverStack = slot.getStack();
            slot.render(c, mx, my);
        }
    //?} else {
    /*@Override
    public void extractRenderState( GuiGraphicsExtractor c, int mx, int my, float d) {
        // Inventory label
        lmx = mx;
        lmy = my;
        ItemStack hoverStack = ItemStack.EMPTY;
        c.text(getFont(), Component.literal("Kit: " + activeKit.getDisplayName()),
                x + margin, y + margin, theme.body_foreground(), false);

        // Browser
        if (activeBrowser != null) activeBrowser.render(c, mx, my);

        // Slots
        for (KitSlot slot : kitSlots) {
            if (slot.isMouseOver(mx, my)) hoverStack = slot.getStack();
            slot.render(c, mx, my);
        }
    *///?}
        if (!hoverStack.isEmpty() && isCursorStackEmpty()) {
            //? if >=1.21.6 {
            List<ClientTooltipComponent> cmp = hoverStack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)
                    .stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .collect(java.util.stream.Collectors.toList());
            hoverStack.getTooltipImage().ifPresent(img -> cmp.add(ClientTooltipComponent.create(img)));
            //? if <=1.21.11 {
            c.renderTooltip(getFont(), cmp, mx, my, DefaultTooltipPositioner.INSTANCE, null);
            //?} else
            //c.tooltip(getFont(), cmp, mx, my, DefaultTooltipPositioner.INSTANCE, null);
            //?} else
            //c.renderTooltip(getFont(), hoverStack.getTooltipLines(Item.TooltipContext.EMPTY,null,TooltipFlag.NORMAL), hoverStack.getTooltipImage(), mx, my);

        }

        // --- Tips Section ---
        // Example list of tips; you can populate this dynamically
        List<String> tips = Arrays.asList(
                "Ctrl+click a slot: edit item meta  |  Shift+click / Drop: remove",
                "Browser: L-click: take 1  |  R-click: take max",
                "Alt+click slot: add to presets  |  Alt+click preset: remove",
                "Ctrl+S: save  |  Click out side: clear cursor"
        );

        //? if <=1.21.11 {
        int tipsY = y + availableHeight - tips.size() * (getFont().lineHeight + 2);
        for (int i = 0; i < tips.size(); i++) {
            String line = tips.get(i);
            c.drawString(getFont(), Component.literal(line),
                    x + margin, tipsY + i * (getFont().lineHeight + 2),
                    theme.body_key(), false);
        }
        
        if(!isCursorStackEmpty()){
            YarnHelpers.pushMatrix(c);
            //? if >=1.21.6 {
            c.nextStratum();
            //?} else
            //c.pose().translate(0,0,300);
            c.renderItem(cursorStack,mx - 8, my - 8);
            c.renderItemDecorations(getFont(),cursorStack,mx-8,my-8);
            YarnHelpers.popMatrix(c);
        }

        super.render(c, mx, my, d);
         
        //?} else {
        /*int tipsY = y + availableHeight - tips.size() * (getFont().lineHeight + 2);
        for (int i = 0; i < tips.size(); i++) {
            String line = tips.get(i);
            c.text(getFont(), Component.literal(line),
                    x + margin, tipsY + i * (getFont().lineHeight + 2),
                    theme.body_key(), false);
        }
        if(!isCursorStackEmpty()){
            YarnHelpers.pushMatrix(c);
            //? if >=1.21.6 {
            /^c.nextStratum();
            ^///?} else
            c.pose().translate(0,0,300);
            c.item(cursorStack,mx - 8, my - 8);
            c.itemDecorations(getFont(),cursorStack,mx-8,my-8);
            YarnHelpers.popMatrix(c);
        }

        super.extractRenderState(c, mx, my, d);
        *///?}
    }

    public boolean isCursorStackEmpty(){
        return cursorStack == null || cursorStack.isEmpty();
    }

    // ── Inner Classes ────────────────────────────────────────────────
    protected class ItemBrowser {
        int bx, y, w, h, cols;
        Scroller scroller = new Scroller();
        List<Group> groups = new ArrayList<>();

        ItemBrowser(int bx, int y, int w, int h, int cols) {
            this.bx = bx;
            this.y = y;
            this.w = w;
            this.h = h;
            this.cols = cols;
        }
        //? if <=1.21.11 {
        void render(GuiGraphics c, int mx, int my) {
            c.enableScissor(bx, y, bx + w, y + h);
            int offsetY = scroller.getOffset();

            for (Group g : groups) {
                // Always render the header
                g.displayCard.render(c, offsetY);

                // Only render slots if expanded
                if (g.expanded) {
                    for (BrowserSlot s : g.slots) {
                        s.render(c, mx, my, offsetY);
                    }
                }
            }

            c.disableScissor();
            scroller.render(c, bx + w - 3, 3, theme.panel_separator(), theme.accent());
         //?} else {
        /*void render(GuiGraphicsExtractor c, int mx, int my) {
            c.enableScissor(bx, y, bx + w, y + h);
            int offsetY = scroller.getOffset();

            for (Group g : groups) {
                // Always render the header
                g.displayCard.render(c, offsetY);

                // Only render slots if expanded
                if (g.expanded) {
                    for (BrowserSlot s : g.slots) {
                        s.render(c, mx, my, offsetY);
                    }
                }
            }

            c.disableScissor();
            scroller.render(c, bx + w - 3, 3, theme.panel_separator(), theme.accent());
        *///?}


            // Tooltip handling outside scissor
            for (Group g : groups) {
                if (!g.expanded) continue;
                for (BrowserSlot s : g.slots) {
                    if (s.isMouseOver(mx, my, offsetY)) {
                        //? if >=1.21.6 {
                        List<ClientTooltipComponent> cmp = s.stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)
                                .stream()
                                .map(Component::getVisualOrderText)
                                .map(ClientTooltipComponent::create)
                                .collect(java.util.stream.Collectors.toList());
                        s.stack.getTooltipImage().ifPresent(img -> cmp.add(ClientTooltipComponent.create(img)));
                        //? if <=1.21.11 {
                        c.renderTooltip(getFont(), cmp, mx, my, DefaultTooltipPositioner.INSTANCE, null);
                         //?} else {
                        /*c.tooltip(getFont(), cmp, mx, my, DefaultTooltipPositioner.INSTANCE, null);
                        *///?}
                        //?} else {
                        /*c.renderTooltip(getFont(), s.stack.getTooltipLines(Item.TooltipContext.EMPTY,null,TooltipFlag.NORMAL), s.stack.getTooltipImage(), mx, my);
                         *///?}
                        return; // only show one tooltip
                    }
                }
            }
        }


        public void calculateScroll() {
            int totalH = 0;
            for (Group g : groups) {
                totalH += getFont().lineHeight + margin;
                int rows = (int) Math.ceil((double) g.displayStacks.size() / cols);
                if(g.expanded) totalH += rows * 20;
            }
            scroller.setArea(bx, y, h, totalH);
        }


        public void initSlotLayout() {
            int startX = bx + margin / 2;
            int nameH = getFont().lineHeight + margin;
            int cy = y;

            for (Group g : groups) {
                g.slots.clear();
                g.displayCard = new NameDisplay(startX, cy, g.displayName);
                cy += nameH;

                if (g.expanded) {
                    int col = 0;
                    for (ItemStack s : g.displayStacks) {
                        int itemX = startX + col * 20;
                        g.slots.add(new BrowserSlot(itemX, cy, s));
                        col++;
                        if (col >= cols) {
                            col = 0;
                            cy += 20;
                        }
                    }
                    if (col != 0) cy += 20;
                    cy += margin;
                }
            }
        }

        public void toggleGroup(Group target) {
            for (Group g : groups) {
                g.expanded = (g == target) && !g.expanded; // only one open at a time
            }
            initSlotLayout(); // rebuild layout
            calculateScroll();
        }


        public boolean mouseClicked(double mx, double my, int btn
                                    //? if >=1.21.9
                                    , MouseButtonEvent m
        ) {
            for (Group g : groups) {
                if (g.displayCard.isMouseOver(mx, my, scroller.getOffset())) {
                    toggleGroup(g);
                    return true;
                }
                for (BrowserSlot s: g.slots) {
                    if(s.isMouseOver(mx,my, scroller.getOffset())){
                        return handleSlotClick(btn, s/*? if >=1.21.9 >>*/ ,m /**/);
                    }
                }
            }
            return false;
        }

        private boolean handleSlotClick(int b, BrowserSlot s
                                        //? if >=1.21.9
                                        , MouseButtonEvent m
        ) {
            if(!s.stack.isEmpty()){
                if(
                        //? if >=1.21.9 {
                        m.hasAltDown()
                        //?} else
                        //Screen.hasAltDown()
                                && ItemBrowser.this == presetBrowser) {
                    removePreset(s);
                    return true;
                }
                if(b == 0) {
                    cursorStack = s.stack.copyWithCount(1);
                    return true;
                }
                if(b == 1) {
                    cursorStack = s.stack.copyWithCount(s.stack.getMaxStackSize());
                    return true;
                }
            }
            return false;
        }


        protected class NameDisplay {
            int x, y;
            Component name;

            public NameDisplay(int x, int y, Component displayName) {
                this.x = x;
                this.y = y;
                this.name = displayName;
            }

            public boolean isMouseOver(double mx, double my, int offsetY) {
                return mx >= x && mx < x + browserPanelWidth - 3 && my >= y - offsetY && my < y - offsetY + getFont().lineHeight + margin;
            }
            //? if <=1.21.11 {
            public void render(GuiGraphics c, int offsetY) {
                c.fill(x, y - offsetY + 1, x + browserPanelWidth, y - offsetY + getFont().lineHeight + margin, theme.body_background());
                c.drawString(getFont(), name, x + margin / 2, y - offsetY + margin / 2, theme.body_label(), false);
            }
             //?} else {
            /*public void render(GuiGraphicsExtractor c, int offsetY) {
                c.fill(x, y - offsetY + 1, x + browserPanelWidth, y - offsetY + getFont().lineHeight + margin, theme.body_background());
                c.text(getFont(), name, x + margin / 2, y - offsetY + margin / 2, theme.body_label(), false);
            }
            *///?}
        }

        protected class BrowserSlot {
            int x, y;
            ItemStack stack;

            public BrowserSlot(int x, int y, ItemStack stack) {
                this.x = x;
                this.y = y;
                this.stack = stack;
            }

            boolean isMouseOver(double mx, double my, int offsetY) {
                return mx >= x && my >= y - offsetY && mx < x + slotSize && my < y - offsetY + slotSize;
            }
            //? if <=1.21.11 {
            public void render(GuiGraphics c, double mx, double my, int offsetY) {
                int drawY = y - offsetY;

                // Skip if completely outside scissor area
                if (drawY + slotSize < ItemBrowser.this.y || drawY > ItemBrowser.this.y + ItemBrowser.this.h) {
                    return;
                }

                RenderUtils.drawBorder(c, x, drawY, slotSize, slotSize, theme.body_border());

                int bg = isMouseOver(mx, my, offsetY) ? (theme.accent() & 0x00FFFFFF | 0x33000000) : (theme.body_background() & 0x00FFFFFF | 0x55000000);

                c.fill(x, drawY, x + slotSize, drawY + slotSize, bg);

                int offset = (slotSize - 16) / 2;
                c.renderItem(stack, x + offset, drawY + offset);
            }
             //?} else {
            /*public void render(GuiGraphicsExtractor c, double mx, double my, int offsetY) {
                int drawY = y - offsetY;

                // Skip if completely outside scissor area
                if (drawY + slotSize < ItemBrowser.this.y || drawY > ItemBrowser.this.y + ItemBrowser.this.h) {
                    return;
                }

                RenderUtils.drawBorder(c, x, drawY, slotSize, slotSize, theme.body_border());

                int bg = isMouseOver(mx, my, offsetY) ? (theme.accent() & 0x00FFFFFF | 0x33000000) : (theme.body_background() & 0x00FFFFFF | 0x55000000);

                c.fill(x, drawY, x + slotSize, drawY + slotSize, bg);

                int offset = (slotSize - 16) / 2;
                c.item(stack, x + offset, drawY + offset);
            }
            *///?}
        }
    }

    protected class Group {
        Component displayName;
        Collection<ItemStack> displayStacks;
        List<ItemBrowser.BrowserSlot> slots = new ArrayList<>();
        ItemBrowser.NameDisplay displayCard;
        boolean expanded = false; // collapsed by default

        public Group(Component displayName, Collection<ItemStack> displayStacks) {
            this.displayName = displayName;
            this.displayStacks = displayStacks;
        }

        public Group setExpanded() {
            expanded = true;
            return this;
        }
    }


    protected class KitSlot {
        int x, y, slotId;
        ItemStack stack;

        KitSlot(int x, int y, int slotId, ItemStack stack) {
            this.x = x;
            this.y = y;
            this.slotId = slotId;
            this.stack = stack;
        }


        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack;
        }
        //? if <=1.21.11 {
        public void render(GuiGraphics c, int mx, int my) {
            RenderUtils.drawBorder(c, x, y, slotSize, slotSize, theme.body_border());
            int bg = isMouseOver(mx, my) ? (theme.accent() & 0x00FFFFFF | 0x33000000) : (theme.body_background() & 0x00FFFFFF | 0x55000000);
            c.fill(x, y, x + slotSize, y + slotSize, bg);

            int offset = (slotSize - 16) / 2;
            c.renderItem(stack, x + offset, y + offset);

            // If you want stack count text:
            c.renderItemDecorations(getFont(), stack, x + offset, y + offset);
        }
        //?} else {
        /*public void render(GuiGraphicsExtractor c, int mx, int my) {
            RenderUtils.drawBorder(c, x, y, slotSize, slotSize, theme.body_border());
            int bg = isMouseOver(mx, my) ? (theme.accent() & 0x00FFFFFF | 0x33000000) : (theme.body_background() & 0x00FFFFFF | 0x55000000);
            c.fill(x, y, x + slotSize, y + slotSize, bg);

            int offset = (slotSize - 16) / 2;
            c.item(stack, x + offset, y + offset);

            // If you want stack count text:
            c.itemDecorations(getFont(), stack, x + offset, y + offset);
        }
        *///?}

        boolean isMouseOver(double mx, double my) {
            return mx >= x && my >= y && mx < x + slotSize && my < y + slotSize;
        }
    }
}