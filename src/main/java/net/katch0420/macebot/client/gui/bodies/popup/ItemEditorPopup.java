package net.katch0420.macebot.client.gui.bodies.popup;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.client.gui.screens.popup.PopupScreen;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.utils.Scroller;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.client.Minecraft;
//? if <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

/**
 * Full item-meta editor for a single {@link ItemStack}: display name (colour
 * codes), stack count, enchantments, attribute modifiers and a raw
 * "Components" tab.
 * <p>
 * Enchantments are deliberately editable from TWO places — the dedicated
 * "Enchantments" tab (pick from a searchable list, the easy/direct way) and
 * the "Components" tab (edit the raw {@code "components"} JSON object
 * directly, the advanced/long way — since enchantments are technically just
 * one more data component). Both views read/write the same in-memory state
 * so they can never disagree with each other.
 * <p>
 * Enchantments live in the client's dynamic registry manager (they're
 * data-driven), so every lookup goes through
 * {@code MaceBotClient.getClientPlayer().getRegistryManager().get(RegistryKeys.ENCHANTMENT)}
 * rather than the static {@code Registries.ENCHANTMENT}.
 * <p>
 * The Components tab shows/edits exactly the JSON that would sit under the
 * {@code "components"} key of {@link ItemStack#CODEC}'s encoded form (NOT
 * {@code id}/{@code count} — those stay owned by the General tab). It is
 * kept in sync with the Enchantments/Attributes/General tabs in both
 * directions:
 * <ul>
 *     <li>Opening the tab (re)generates its text from the current working
 *     state, so it always reflects live edits made elsewhere.</li>
 *     <li>Editing the field and then switching tabs, pressing Enter, or
 *     hitting Save attempts to parse+decode the JSON via the vanilla codec.
 *     If it's valid, the decoded components are written back into the
 *     shared working state (enchantments/attributes/name/unbreakable).
 *     If it's <em>not</em> valid, nothing is touched — a warning is shown
 *     under the field, and the next time this tab is opened the field
 *     reverts to the last known-good JSON (since the field is always
 *     regenerated from the untouched working state).</li>
 * </ul>
 * On Save, the final {@link ItemStack} is produced by decoding straight from
 * that same components JSON via the codec, rather than re-applying each
 * component type by hand a second time.
 */
public class ItemEditorPopup extends PopupScreen {

    private enum Tab { GENERAL, ENCHANTMENTS, ATTRIBUTES, COMPONENTS }

    private final ItemStack original;
    private final Consumer<ItemStack> onSave;

    private Tab currentTab = Tab.GENERAL;

    // ── Working state (single source of truth, tabs just render/edit this) ──
    private String rawName;           // '&'-coded, mirrors StyledTextEditor convention
    private int count;
    private boolean unbreakable;
    private final LinkedHashMap<Holder<Enchantment>, Integer> enchantments = new LinkedHashMap<>();
    private final List<AttrEntry> attributes = new ArrayList<>();

    // ── Widgets shared across tabs ───────────────────────────────────────────
    private EditBox nameField;
    private EditBox countField;
    private EditBox componentsField; // raw "components" JSON editor
    private String componentsError;          // non-null => shown under the field, last parse/decode failed

    private final Scroller scroller = new Scroller();

    // ── Layout ────────────────────────────────────────────────────────────────
    private int margin;
    private int titleH;
    private int tabH;
    private int contentY;
    private int contentH;
    private int rowH;
    private int btnH;

    // General tab: computed once in initGeneralTab(), read directly by renderGeneralLabels()
    // so the label positions can never drift out of sync with the actual widget positions again.
    private int generalPreviewY;
    private int generalCountLabelY;

    /** Hard ceiling only — enchantments here are not limited to their vanilla max level. */
    private static final int ABSOLUTE_MAX_LEVEL = 255;

    public ItemEditorPopup(Screen parent, ItemStack stack, Consumer<ItemStack> onSave) {
        super(parent);
        this.original = stack.copy();
        this.onSave = onSave;
        this.title = Component.literal("Edit Item");
        count = original.getCount();
        loadComponentsOnly(original);
    }

    /** Dynamic-registry lookup for enchantments — see class javadoc. */
    static Registry<Enchantment> enchantmentRegistry() {
        return MaceBotClient.getClientPlayer().registryAccess()
                //? if >=1.21.2 {
                .lookupOrThrow
                //?} else
                //.registryOrThrow
                        (Registries.ENCHANTMENT);
    }

    private static RegistryOps<JsonElement> clientOps() {
        return RegistryOps.create(JsonOps.INSTANCE, MaceBotClient.getClientPlayer().registryAccess());
    }

    // ── Load / build ──────────────────────────────────────────────────────────

    /** Reads name/unbreakable/enchantments/attributes off {@code stack} into the working state. Count is NOT touched here. */
    private void loadComponentsOnly(ItemStack stack) {
        if (stack.get(DataComponents.ITEM_NAME) != null) {
            rawName = stack.get(DataComponents.ITEM_NAME).getString().replace('§', '&');
        } else {
            rawName = stack.getDisplayName().getString().replace('§', '&');
        }
        System.out.println(rawName);
        unbreakable = stack.get(DataComponents.UNBREAKABLE) != null;

        enchantments.clear();
        ItemEnchantments enchComp = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchComp.entrySet()) {
            enchantments.put(entry.getKey(), enchComp.getLevel(entry.getKey()));
        }

        attributes.clear();
        ItemAttributeModifiers attrComp = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry e : attrComp.modifiers()) {
            attributes.add(new AttrEntry(
                    BuiltInRegistries.ATTRIBUTE.getKey(e.attribute().value()),
                    e.modifier().amount(),
                    e.modifier().operation(),
                    e.slot()
            ));
        }

        if (nameField != null) nameField.setValue(rawName);
    }

    /** Builds an ItemStack straight from the current Java-side working state. */
    private ItemStack buildStackFromState(int withCount) {
        ItemStack result = original.copy();
        result.setCount(Math.max(1, withCount));

        String trimmed = rawName == null ? "" : rawName.trim();
        if (trimmed.isEmpty()) {
            result.remove(DataComponents.ITEM_NAME);
        } else {
            result.set(DataComponents.ITEM_NAME, LegacyText.parse(trimmed));
        }

        if (unbreakable) {
            KitStack.addUnbreakableComponent(result);
        } else {
            result.remove(DataComponents.UNBREAKABLE);
        }

        if (enchantments.isEmpty()) {
            result.remove(DataComponents.ENCHANTMENTS);
        } else {
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.forEach(builder::set);
            result.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
        }

        if (attributes.isEmpty()) {
            result.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        } else {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            for (AttrEntry a : attributes) {
                builder.add(
                        BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE
                                //? if >=1.21.2 {
                                .getValue
                                //?} else
                                //.get
                                        (a.attributeId))),
                        new AttributeModifier(a.modifierId,a.amount,a.operation),
                        a.slot
                        );
            }
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }

        return result;
    }

    /** Encodes the current working state's components as the exact JSON that sits under ItemStack.CODEC's "components" key. */
    private JsonObject buildComponentsJsonFromState() {
        ItemStack temp = buildStackFromState(1);
        JsonElement encoded = ItemStack.CODEC.encodeStart(clientOps(), temp)
                .resultOrPartial(err -> MaceBot.LOGGER.warn("[ItemEditor] component encode error: " + err))
                .orElse(null);
        if (encoded != null && encoded.isJsonObject() && encoded.getAsJsonObject().has("components")) {
            JsonElement comps = encoded.getAsJsonObject().get("components");
            if (comps.isJsonObject()) return comps.getAsJsonObject();
        }
        return new JsonObject();
    }

    /** Decodes a full stack from {id, count, components} via the vanilla codec, exposing the raw result so callers can inspect *why* a decode failed. */
    private DataResult<ItemStack> decodeStackResult(JsonObject componentsJson, int withCount) {
        JsonObject full = new JsonObject();
        full.addProperty("id", BuiltInRegistries.ITEM.getKey(original.getItem()).toString());
        full.addProperty("count", Math.max(1, withCount));
        full.add("components", componentsJson);
        return ItemStack.CODEC.parse(clientOps(), full);
    }

    /** Convenience wrapper for callers that only care whether decode succeeded. */
    private ItemStack decodeStackFromComponentsJson(JsonObject componentsJson, int withCount) {
        return decodeStackResult(componentsJson, withCount)
                .resultOrPartial(err -> MaceBot.LOGGER.warn("[ItemEditor] component decode error: " + err))
                .orElse(null);
    }

    /** Builds a fresh ItemStack from current working state and hands it to the caller. */
    private void applyAndSave() {
        syncGeneralFieldsToState();
        if (currentTab == Tab.COMPONENTS) {
            syncMapFromComponentsField();
            if (componentsError != null) {
                // BUGFIX: this used to fall through to a save+close regardless of sync outcome,
                // which silently discarded a bad (or seemingly-good-but-rejected) edit AND never
                // gave the warning a chance to render, since the popup closed immediately after.
                // Now: stop, keep the popup open, and actually show why it wasn't applied.
                repositionElements();
                return;
            }
        }

        // The components JSON is the single source of truth by this point (kept in sync by every
        // tab above) - decode straight from it instead of re-applying each component type by hand.
        JsonObject componentsJson = buildComponentsJsonFromState();
        ItemStack result = decodeStackFromComponentsJson(componentsJson, count);
        if (result == null) {
            // Should only happen if something upstream produced genuinely invalid data; fall back
            // to the direct builder so Save never silently no-ops.
            result = buildStackFromState(count);
        }

        onSave.accept(result);
        onClose();
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        margin = 8;
        btnH   = 16;
        rowH   = font.lineHeight + margin;
        titleH = font.lineHeight + margin * 2;
        tabH   = 18;

        popupW = Math.max(300, Math.min(width * 3 / 5, 460));
        popupH = Math.max(260, Math.min(height * 3 / 4, 340));

        popupX = (width  - popupW) / 2;
        popupY = (height - popupH) / 2;

        contentY = popupY + titleH + tabH + margin;
        contentH = popupY + popupH - contentY - btnH - margin * 2;

        initTabButtons();

        switch (currentTab) {
            case GENERAL      -> initGeneralTab();
            case ENCHANTMENTS -> initEnchantTab();
            case ATTRIBUTES   -> initAttributeTab();
            case COMPONENTS   -> initComponentsTab();
        }

        // Save / Cancel — always present, bottom of popup
        int bW = (popupW - margin * 3) / 2;
        int bY = popupY + popupH - btnH - margin;
        addRenderableWidget(Button.builder()
                .position(popupX + margin, bY).size(bW, btnH)
                .baseLabel(Component.nullToEmpty("Save"))
                .backgroundColor(theme.success()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.success() + 0xFF101010).holdColor(theme.success() + 0xFF202020)
                .onClick(b -> applyAndSave()).build());
        addRenderableWidget(Button.builder()
                .position(popupX + margin * 2 + bW, bY).size(bW, btnH)
                .baseLabel(Component.nullToEmpty("Cancel"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> onClose()).build());
    }

    private void initTabButtons() {
        Tab[] tabs = Tab.values();
        String[] labels = { "General", "Enchants", "Attributes", "Components" };
        int tw = popupW / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            boolean active = t == currentTab;
            int bg = active ? theme.accent() : theme.header_background();
            addRenderableWidget(Button.builder()
                    .position(popupX + i * tw, popupY + titleH)
                    .size(i == tabs.length - 1 ? popupW - tw * (tabs.length - 1) : tw, tabH)
                    .baseLabel(Component.nullToEmpty(labels[i]))
                    .backgroundColor(bg)
                    .foregroundColor(active ? 0xFFFFFFFF : theme.header_foreground())
                    .borderColor(-1)
                    .hoverColor(bg + 0xFF101010).holdColor(bg + 0xFF202020)
                    .onClick(b -> switchTab(t))
                    .build());
        }
    }

    private void switchTab(Tab t) {
        if (currentTab == Tab.GENERAL) syncGeneralFieldsToState();
        if (currentTab == Tab.COMPONENTS) syncMapFromComponentsField();
        currentTab = t;
        scroller.setArea(popupX, contentY, contentH, 0);
        repositionElements();
    }

    // ── GENERAL tab ───────────────────────────────────────────────────────────

    private void initGeneralTab() {
        int y = contentY;

        int nameFieldY = y + font.lineHeight + margin / 2;
        nameField = new EditBox(font, popupX + margin, nameFieldY,
                popupW - margin * 2, font.lineHeight + 8, Component.literal("Name"));
        nameField.setMaxLength(256);
        nameField.setValue(rawName);
        addRenderableWidget(nameField);

        // A few px below the name field's own bottom edge - guaranteed no overlap since it's
        // derived directly from nameFieldY instead of a separately-recomputed chain.
        generalPreviewY = nameFieldY + font.lineHeight + 8 + 4;

        int stepW = 20;
        generalCountLabelY = generalPreviewY + font.lineHeight + margin;
        // BUGFIX: this used to place the count FIELD using a duplicate offset chain in this method
        // while the "Count" LABEL was placed using a *different* chain in renderGeneralLabels() -
        // the two drifted apart and the field ended up overlapping/hiding the label. Now the field
        // is positioned directly below the stored label Y, guaranteeing a clean gap every time.
        int countFieldY = generalCountLabelY + font.lineHeight + 2;
        countField = new EditBox(font, popupX + margin + stepW, countFieldY,
                popupW - margin * 2 - stepW * 2, font.lineHeight + 8, Component.literal("Count"));
        countField.setMaxLength(3);
        countField.setValue(String.valueOf(count));
        addRenderableWidget(countField);

        addRenderableWidget(Button.builder()
                .position(popupX + margin, countFieldY).size(stepW, font.lineHeight + 8)
                .baseLabel(Component.nullToEmpty("-"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> { count = Math.max(1, count - 1); countField.setValue(String.valueOf(count)); })
                .build());
        addRenderableWidget(Button.builder()
                .position(popupX + popupW - margin - stepW, countFieldY).size(stepW, font.lineHeight + 8)
                .baseLabel(Component.nullToEmpty("+"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> { count = Math.min(99, count + 1); countField.setValue(String.valueOf(count)); })
                .build());

        int cbY = countFieldY + font.lineHeight + 8 + margin;
        int cbSize = 11;
        addRenderableWidget(Button.builder()
                .position(popupX + margin, cbY)
                .size(cbSize + 4 + font.width("Unbreakable"), cbSize + 2)
                .baseLabel(Component.nullToEmpty((unbreakable ? "[x] " : "[ ] ") + "Unbreakable"))
                .backgroundColor(0).foregroundColor(theme.body_label()).borderColor(0)
                .hoverColor(theme.body_button_background() & 0x33FFFFFF).holdColor(theme.body_button_background() & 0x55FFFFFF)
                .onClick(b -> { unbreakable = !unbreakable; b.setLabelIfClickButton(Component.nullToEmpty((unbreakable ? "[x] " : "[ ] ") + "Unbreakable")); })
                .build());
    }

    private void syncGeneralFieldsToState() {
        if (nameField != null) rawName = nameField.getValue();
        if (countField != null) {
            try { count = Math.max(1, Integer.parseInt(countField.getValue().trim())); }
            catch (NumberFormatException ignored) { /* keep previous value */ }
        }
    }

    // ── ENCHANTMENTS tab ──────────────────────────────────────────────────────

    private void initEnchantTab() {
        scroller.setArea(popupX, contentY, contentH - rowH - margin, enchantments.size() * rowH);
        int offset = scroller.getOffset();

        int y = contentY - offset;
        int i = 0;
        for (Map.Entry<Holder<Enchantment>, Integer> e : enchantments.entrySet()) {
            int rowTop = y + i * rowH;
            // BUGFIX: this needs to be an "any overlap" test, not "fully contained" - the latter
            // (rowTop >= contentY && rowTop+rowH <= boundary) makes partially-visible rows at the
            // top/bottom edge vanish entirely instead of scrolling smoothly.
            if (rowTop + rowH >= contentY && rowTop <= contentY + (contentH - rowH - margin)) {
                addEnchantRow(e.getKey(), e.getValue(), rowTop);
            }
            i++;
        }

        int addY = contentY + (contentH - rowH - margin) + margin;
        addRenderableWidget(Button.builder()
                .position(popupX + margin, addY).size(popupW - margin * 2, btnH)
                .baseLabel(Component.nullToEmpty("+ Add Enchantment"))
                .backgroundColor(theme.accent()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.accent() + 0xFF101010).holdColor(theme.accent() + 0xFF202020)
                .onClick(b -> openEnchantPicker())
                .build());
    }

    private void addEnchantRow(Holder<Enchantment> entry, int level, int rowTop) {
        int stepW = 16;
        int removeW = 16;
        int rowRight = popupX + popupW - margin;

        addRenderableWidget(Button.builder()
                .position(rowRight - removeW, rowTop).size(removeW, rowH - 2)
                .baseLabel(Component.nullToEmpty("x"))
                .backgroundColor(theme.danger()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.danger() + 0xFF101010).holdColor(theme.danger() + 0xFF202020)
                .onClick(b -> { enchantments.remove(entry); repositionElements(); })
                .build());

        addRenderableWidget(Button.builder()
                .position(rowRight - removeW - margin / 2 - stepW, rowTop).size(stepW, rowH - 2)
                .baseLabel(Component.nullToEmpty("+"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .activeSupplier(() -> level < ABSOLUTE_MAX_LEVEL)
                .onClick(b -> { enchantments.put(entry, Math.min(ABSOLUTE_MAX_LEVEL, level + 1)); repositionElements(); })
                .build());

        addRenderableWidget(Button.builder()
                .position(rowRight - removeW - margin - stepW * 2, rowTop).size(stepW, rowH - 2)
                .baseLabel(Component.nullToEmpty("-"))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .activeSupplier(() -> level > 1)
                .onClick(b -> { enchantments.put(entry, Math.max(1, level - 1)); repositionElements(); })
                .build());
    }

    private void openEnchantPicker() {
        //? if <26.2 {
        Minecraft.getInstance().setScreen(new EnchantPickerPopup(this, enchantments.keySet(), enchantments::put));
        //?} else
        //Minecraft.getInstance().setScreenAndShow(new EnchantPickerPopup(this, enchantments.keySet(), enchantments::put));
    }

    // ── ATTRIBUTES tab ────────────────────────────────────────────────────────

    private void initAttributeTab() {
        scroller.setArea(popupX, contentY, contentH - rowH - margin, attributes.size() * rowH);
        int offset = scroller.getOffset();

        int y = contentY - offset;
        for (int i = 0; i < attributes.size(); i++) {
            int rowTop = y + i * rowH;
            // Same "any overlap" fix as the Enchantments tab above.
            if (rowTop + rowH >= contentY && rowTop <= contentY + (contentH - rowH - margin)) {
                addAttributeRow(attributes.get(i), rowTop);
            }
        }

        int addY = contentY + (contentH - rowH - margin) + margin;
        addRenderableWidget(Button.builder()
                .position(popupX + margin, addY).size(popupW - margin * 2, btnH)
                .baseLabel(Component.nullToEmpty("+ Add Attribute Modifier"))
                .backgroundColor(theme.accent()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.accent() + 0xFF101010).holdColor(theme.accent() + 0xFF202020)
                .onClick(b -> {
                    attributes.add(new AttrEntry(
                            Identifier.fromNamespaceAndPath("minecraft", "generic.max_health"),
                            1.0,
                            AttributeModifier.Operation.ADD_VALUE,
                            EquipmentSlotGroup.ANY));
                    repositionElements();
                })
                .build());
    }

    private void addAttributeRow(AttrEntry entry, int rowTop) {
        int rowRight = popupX + popupW - margin;
        int removeW = 16;
        int opW = 60;
        int amtW = 60;
        int idW = popupW - margin * 2 - removeW - opW - amtW - margin * 3;

        // Attribute id — click to cycle through the registry (simple, no separate picker popup)
        addRenderableWidget(Button.builder()
                .position(popupX + margin, rowTop).size(idW, rowH - 2)
                .baseLabel(Component.nullToEmpty(shortId(entry.attributeId, idW)))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> { cycleAttributeId(entry, 1); repositionElements(); })
                .onRightClick(b -> { cycleAttributeId(entry, -1); repositionElements(); })
                .build());

        // Amount
        EditBox amt = new EditBox(font, popupX + margin + idW + margin, rowTop,
                amtW, rowH - 2, Component.literal("Amount"));
        amt.setValue(trimAmount(entry.amount));
        amt.setResponder(s -> {
            try { entry.amount = Double.parseDouble(s.trim()); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(amt);

        // Operation — cycle
        addRenderableWidget(Button.builder()
                .position(popupX + margin + idW + margin + amtW + margin, rowTop).size(opW, rowH - 2)
                .baseLabel(Component.nullToEmpty(opLabel(entry.operation)))
                .backgroundColor(theme.body_button_background()).foregroundColor(theme.body_button_foreground()).borderColor(-1)
                .hoverColor(theme.body_button_background() + 0xFF101010).holdColor(theme.body_button_background() + 0xFF202020)
                .onClick(b -> { entry.operation = nextOperation(entry.operation); repositionElements(); })
                .build());

        addRenderableWidget(Button.builder()
                .position(rowRight - removeW, rowTop).size(removeW, rowH - 2)
                .baseLabel(Component.nullToEmpty("x"))
                .backgroundColor(theme.danger()).foregroundColor(0xFFFFFFFF).borderColor(-1)
                .hoverColor(theme.danger() + 0xFF101010).holdColor(theme.danger() + 0xFF202020)
                .onClick(b -> { attributes.remove(entry); repositionElements(); })
                .build());
    }

    private void cycleAttributeId(AttrEntry entry, int dir) {
        List<Identifier> ids = BuiltInRegistries.ATTRIBUTE.stream()
                .map(BuiltInRegistries.ATTRIBUTE::getKey)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();
        if (ids.isEmpty()) return;
        int idx = ids.indexOf(entry.attributeId);
        idx = (idx < 0 ? 0 : idx + dir + ids.size()) % ids.size();
        entry.attributeId = ids.get(idx);
    }

    private AttributeModifier.Operation nextOperation(AttributeModifier.Operation op) {
        AttributeModifier.Operation[] ops = AttributeModifier.Operation.values();
        return ops[(op.ordinal() + 1) % ops.length];
    }

    private String opLabel(AttributeModifier.Operation op) {
        return switch (op) {
            case ADD_VALUE -> "Add";
            case ADD_MULTIPLIED_BASE -> "Mult Base";
            case ADD_MULTIPLIED_TOTAL -> "Mult Total";
        };
    }

    private String trimAmount(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    // ── COMPONENTS tab (the "long way") ──────────────────────────────────────

    private void initComponentsTab() {
        int y = contentY;

        // Always regenerated from the current (valid) working state - this is what makes an
        // invalid edit "revert" automatically: if the last sync attempt failed, state was never
        // touched, so re-deriving text from state here reproduces the last known-good JSON.
        componentsField = new EditBox(font, popupX + margin, y + font.lineHeight + margin / 2,
                popupW - margin * 2, font.lineHeight + 8, Component.literal("{}"));
        componentsField.setMaxLength(1024);
        componentsField.setValue(buildComponentsJsonFromState().toString());
        // Distinct amber "code" color so this field visually reads as raw JSON, not a plain label.
        componentsField.setTextColorUneditable(0xFFE8B04B);
        componentsField.setTextColor(0xFFE8B04B);
        addRenderableWidget(componentsField);
        componentsError = null;
    }

    /** Parses the "components" JSON field and, if valid, writes it back into the shared working state. */
    private void syncMapFromComponentsField() {
        if (componentsField == null) return;
        String raw = componentsField.getValue().trim();

        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(raw.isEmpty() ? "{}" : raw);
        } catch (Exception ex) {
            componentsError = "Invalid JSON: " + truncate(ex.getMessage());
            return;
        }
        if (!parsed.isJsonObject()) {
            componentsError = "Must be a JSON object, e.g. {\"minecraft:enchantments\": {...}}";
            return;
        }

        DataResult<ItemStack> decodeResult = decodeStackResult(parsed.getAsJsonObject(), count);
        Optional<ItemStack> decoded = decodeResult.result();
        if (decoded.isEmpty()) {
            String detail = decodeResult.error().map(DataResult.Error::message).orElse("unrecognized component data");
            componentsError = "Rejected: " + truncate(detail);
            MaceBot.LOGGER.warn("[ItemEditor] component decode error: " + detail);
            return;
        }

        loadComponentsOnly(decoded.get());
        componentsError = null;
    }

    /** Keeps error text from overflowing the popup width - it's rendered on a single line. */
    private String truncate(String s) {
        if (s == null) return "unknown error";
        return s.length() > 110 ? s.substring(0, 110) + "…" : s;
    }

    private String shortId(Identifier id, int width) {
        return id.getPath().length() > width - 2 * margin ? id.getPath().substring(0, 14) + "…" : id.getPath();
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if ((currentTab == Tab.ENCHANTMENTS || currentTab == Tab.ATTRIBUTES)
                && scroller.mouseScrolled(mx, my, popupX, popupW, v, rowH)) {
            repositionElements();
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
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (currentTab == Tab.GENERAL) syncGeneralFieldsToState();
            if (currentTab == Tab.COMPONENTS) {
                syncMapFromComponentsField();
                // Re-init so a successful sync refreshes every tab's view of the new state,
                // and a failed sync shows the warning immediately instead of only on next switch.
                repositionElements();
                return true;
            }
            applyAndSave();
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
    //? if >1.21.11 {
    /*@Override
    public void renderPopupScreen(GuiGraphicsExtractor c, int mx, int my) {
        // Title bar
        c.fill(popupX, popupY, popupX + popupW, popupY + titleH, theme.header_background());
        c.text(font, title,
                popupX + margin, popupY + (titleH - font.lineHeight) / 2,
                theme.header_foreground(), false);

        // Body
        c.fill(popupX, popupY + titleH + tabH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH + tabH, popupX + popupW, popupY + titleH + tabH + 1, theme.panel_separator());

        switch (currentTab) {
            case GENERAL -> renderGeneralLabels(c);
            case ENCHANTMENTS -> renderEnchantLabels(c);
            case ATTRIBUTES -> renderAttributeLabels(c, mx, my);
            case COMPONENTS -> renderComponentsLabels(c);
        }

        if (currentTab == Tab.ENCHANTMENTS || currentTab == Tab.ATTRIBUTES) {
            scroller.render(c, popupX + popupW - 5, 3, theme.panel_separator(), theme.accent());
        }

        drawPopupBorder(c, theme.accent());
    }

    private void renderGeneralLabels(GuiGraphicsExtractor c) {
        c.text(font, Component.nullToEmpty("Name  (use & for colour codes, e.g. &6&lLegendary Sword)"),
                popupX + margin, contentY, theme.body_label(), false);

        String preview = "Preview: ";
        c.text(font, Component.nullToEmpty(preview), popupX + margin, generalPreviewY, theme.body_key(), false);
        c.text(font, LegacyText.parse(nameField != null ? nameField.getValue() : rawName),
                popupX + margin + font.width(preview), generalPreviewY, 0xFFFFFFFF, false);

        c.text(font, Component.nullToEmpty("Count"), popupX + margin, generalCountLabelY, theme.body_label(), false);
    }

    private void renderEnchantLabels(GuiGraphicsExtractor c) {
        if (enchantments.isEmpty()) {
            c.text(font, Component.nullToEmpty("No enchantments. Click below to add one."),
                    popupX + margin, contentY, theme.body_value() & 0x88FFFFFF, false);
        } else {
            c.enableScissor(popupX, contentY, popupX + popupW, contentY + (contentH - rowH - margin));
            int offset = scroller.getOffset();
            int y = contentY - offset;
            int i = 0;
            for (Map.Entry<Holder<Enchantment>, Integer> e : enchantments.entrySet()) {
                Component name = Enchantment.getFullname(e.getKey(), e.getValue());
                c.text(font, name, popupX + margin, y + i * rowH + (rowH - font.lineHeight) / 2,
                        theme.body_value(), false);
                i++;
            }
            c.disableScissor();
        }
    }

    private void renderAttributeLabels(GuiGraphicsExtractor c, int mx, int my) {
        if (attributes.isEmpty()) {
            c.text(font, Component.nullToEmpty("No attribute modifiers. Click below to add one."),
                    popupX + margin, contentY, theme.body_value() & 0x88FFFFFF, false);
        }
        // Per-row labels are drawn by the row buttons themselves (baseLabel); nothing extra needed here.
    }

    private void renderComponentsLabels(GuiGraphicsExtractor c) {
        int y = contentY;
        c.text(font, Component.nullToEmpty("Components (raw JSON) — synced with Enchantments/Attributes/General"),
                popupX + margin, y, theme.body_label(), false);

        int belowFieldY = y + font.lineHeight + margin / 2 + font.lineHeight + 8 + 2;
        if (componentsError != null) {
            c.text(font, Component.nullToEmpty(componentsError), popupX + margin, belowFieldY, theme.danger(), false);
        } else {
            c.text(font, Component.nullToEmpty("Press Enter, or switch tabs, to apply an edit."),
                    popupX + margin, belowFieldY, theme.body_value() & 0x77FFFFFF, false);
        }

        int noteY = belowFieldY + font.lineHeight + margin;
        c.text(font, Component.nullToEmpty("Invalid edits are discarded — reopening this tab reverts to the last valid JSON."),
                popupX + margin, noteY, theme.body_value() & 0x88FFFFFF, false);
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

        // Body
        c.fill(popupX, popupY + titleH + tabH, popupX + popupW, popupY + popupH, popupBodyColor());
        c.fill(popupX, popupY + titleH + tabH, popupX + popupW, popupY + titleH + tabH + 1, theme.panel_separator());

        switch (currentTab) {
            case GENERAL -> renderGeneralLabels(c);
            case ENCHANTMENTS -> renderEnchantLabels(c);
            case ATTRIBUTES -> renderAttributeLabels(c, mx, my);
            case COMPONENTS -> renderComponentsLabels(c);
        }

        if (currentTab == Tab.ENCHANTMENTS || currentTab == Tab.ATTRIBUTES) {
            scroller.render(c, popupX + popupW - 5, 3, theme.panel_separator(), theme.accent());
        }

        drawPopupBorder(c, theme.accent());
    }

    private void renderGeneralLabels(GuiGraphics c) {
        c.drawString(font, Component.nullToEmpty("Name  (use & for colour codes, e.g. &6&lLegendary Sword)"),
                popupX + margin, contentY, theme.body_label(), false);

        String preview = "Preview: ";
        c.drawString(font, Component.nullToEmpty(preview), popupX + margin, generalPreviewY, theme.body_key(), false);
        c.drawString(font, LegacyText.parse(nameField != null ? nameField.getValue() : rawName),
                popupX + margin + font.width(preview), generalPreviewY, 0xFFFFFFFF, false);

        c.drawString(font, Component.nullToEmpty("Count"), popupX + margin, generalCountLabelY, theme.body_label(), false);
    }

    private void renderEnchantLabels(GuiGraphics c) {
        if (enchantments.isEmpty()) {
            c.drawString(font, Component.nullToEmpty("No enchantments. Click below to add one."),
                    popupX + margin, contentY, theme.body_value() & 0x88FFFFFF, false);
        } else {
            c.enableScissor(popupX, contentY, popupX + popupW, contentY + (contentH - rowH - margin));
            int offset = scroller.getOffset();
            int y = contentY - offset;
            int i = 0;
            for (Map.Entry<Holder<Enchantment>, Integer> e : enchantments.entrySet()) {
                Component name = Enchantment.getFullname(e.getKey(), e.getValue());
                c.drawString(font, name, popupX + margin, y + i * rowH + (rowH - font.lineHeight) / 2,
                        theme.body_value(), false);
                i++;
            }
            c.disableScissor();
        }
    }

    private void renderAttributeLabels(GuiGraphics c, int mx, int my) {
        if (attributes.isEmpty()) {
            c.drawString(font, Component.nullToEmpty("No attribute modifiers. Click below to add one."),
                    popupX + margin, contentY, theme.body_value() & 0x88FFFFFF, false);
        }
        // Per-row labels are drawn by the row buttons themselves (baseLabel); nothing extra needed here.
    }

    private void renderComponentsLabels(GuiGraphics c) {
        int y = contentY;
        c.drawString(font, Component.nullToEmpty("Components (raw JSON) — synced with Enchantments/Attributes/General"),
                popupX + margin, y, theme.body_label(), false);

        int belowFieldY = y + font.lineHeight + margin / 2 + font.lineHeight + 8 + 2;
        if (componentsError != null) {
            c.drawString(font, Component.nullToEmpty(componentsError), popupX + margin, belowFieldY, theme.danger(), false);
        } else {
            c.drawString(font, Component.nullToEmpty("Press Enter, or switch tabs, to apply an edit."),
                    popupX + margin, belowFieldY, theme.body_value() & 0x77FFFFFF, false);
        }

        int noteY = belowFieldY + font.lineHeight + margin;
        c.drawString(font, Component.nullToEmpty("Invalid edits are discarded — reopening this tab reverts to the last valid JSON."),
                popupX + margin, noteY, theme.body_value() & 0x88FFFFFF, false);
    }
    //?}

    // ── Data holders ──────────────────────────────────────────────────────────

    private static final class AttrEntry {
        Identifier attributeId;
        double amount;
        AttributeModifier.Operation operation;
        EquipmentSlotGroup slot;
        final Identifier modifierId;

        AttrEntry(Identifier attributeId, double amount, AttributeModifier.Operation operation,
                  EquipmentSlotGroup slot) {
            this.attributeId = attributeId;
            this.amount = amount;
            this.operation = operation;
            this.slot = slot;
            this.modifierId = Identifier.fromNamespaceAndPath("bot", "modifier_" + UUID.randomUUID());
        }
    }
}