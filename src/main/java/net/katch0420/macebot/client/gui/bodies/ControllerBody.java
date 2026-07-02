package net.katch0420.macebot.client.gui.bodies;

import net.katch0420.macebot.client.gui.MaceBotTextures;
import net.katch0420.macebot.client.gui.layout.ResponsiveLayout;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.macebot.MaceBotCommandSender;
import net.katch0420.macebot.client.macebot.MacebotDataGetter;
import net.katch0420.macebot.main.macebot.control.Controller.Mode;
import net.katch0420.macebot.main.macebot.control.Difficulty;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.client.ClientSideSettingsSyncHelper;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.client.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Controller body: an entity-preview/stats panel on the left and a
 * spawn/control panel with quick-settings on the right.
 *
 * Responsive behaviour: below {@link #STACK_THRESHOLD} available width the
 * two panels stack vertically (entity panel on top, control panel below)
 * instead of squeezing side-by-side, so the GUI stays usable at small GUI
 * scales / low resolutions instead of clipping or overlapping.
 */
public class ControllerBody extends Body {
    /** Below this available width, switch from side-by-side to stacked layout. */
    private static final int STACK_THRESHOLD = 260;

    private boolean stacked;

    int margin;
    int entityPanelWidth;
    int entityPanelHeight;
    int entityDisplayWidth;
    int entityDisplayHeight;
    int entityStatsGap;
    int entityEditButtonHeight;
    int entityEditButtonWidth;
    int entitySettingButtonSize;

    int entityX, entityY;
    int textHeight;

    int entityDisplayX, entityDisplayY;
    int dataX, dataY;

    int entitySettingX, entitySettingY, entitySettingButtonY;

    int controlX, controlY;
    int controlPanelWidth;
    int controlPanelHeight;

    int controlButtonX, controlButtonY, controlButtonWidth, controlButtonHeight;

    int controlSettingsY;
    int controlSettingButtonY, controlSettingButtonSize;

    @Override
    public Text getLabel() {
        return Text.of("Controls");
    }

    @Override
    public void init() {
        super.init();
        margin = s(6, 4);
        textHeight = getTextRenderer().fontHeight;
        stacked = ResponsiveLayout.shouldStackVertically(availableWidth, STACK_THRESHOLD);

        if (stacked) {
            initEntityPanelStacked();
            initControlPanelStacked();
        } else {
            initEntityPanel();
            initControlPanel();
        }
    }

    // ------------------------------------------------------------------
    // Side-by-side layout (normal / wide GUI)
    // ------------------------------------------------------------------

    private void initEntityPanel() {
        entityPanelWidth = (availableWidth - 2) * 4 / 9;
        entityPanelHeight = availableHeight;
        layoutEntityPanelContents(x, y, entityPanelWidth, entityPanelHeight);
    }

    private void initControlPanel() {
        controlX = entityX + 1 + entityPanelWidth;
        controlY = entityY;
        controlPanelWidth = (availableWidth - 2) - entityPanelWidth;
        controlPanelHeight = availableHeight;
        layoutControlPanelContents(controlX, controlY, controlPanelWidth, controlPanelHeight);
    }

    // ------------------------------------------------------------------
    // Stacked layout (narrow GUI / low resolution / extreme GUI scale)
    // ------------------------------------------------------------------

    private void initEntityPanelStacked() {
        entityPanelWidth = availableWidth;
        entityPanelHeight = Math.max(s(140, 110), availableHeight * 5 / 9);
        layoutEntityPanelContents(x, y, entityPanelWidth, entityPanelHeight);
    }

    private void initControlPanelStacked() {
        controlX = x;
        controlY = y + entityPanelHeight + 1;
        controlPanelWidth = availableWidth;
        controlPanelHeight = Math.max(s(110, 90), availableHeight - entityPanelHeight - 1);
        layoutControlPanelContents(controlX, controlY, controlPanelWidth, controlPanelHeight);
    }

    // ------------------------------------------------------------------
    // Shared content layout - parameterized by panel rect so both layout
    // modes (side-by-side / stacked) reuse identical logic.
    // ------------------------------------------------------------------

    private void layoutEntityPanelContents(int panelX, int panelY, int panelWidth, int panelHeight) {
        entityX = panelX;
        entityY = panelY;

        entityDisplayWidth = (panelWidth - 3 * margin) * 3 / 11;
        entityDisplayHeight = (int) (entityDisplayWidth * 1.9);
        entityEditButtonHeight = s(20, 16);
        entityEditButtonWidth = Math.max(s(50, 40), panelWidth - entityDisplayWidth - 4 * margin);

        entityDisplayX = entityX + 2 * margin;
        entityDisplayY = entityY + 2 * margin + textHeight;

        List<Data> dataList = entityDataList();

        // FIX (was use-before-set): compute the inter-row gap BEFORE anything
        // that reads it (dataY, button Y positions, etc).
        entityStatsGap = Math.max(2, Math.min(s(15, 10),
                (entityDisplayHeight - dataList.size() * textHeight - 2 * entityEditButtonHeight - 3 * margin)
                        / Math.max(1, dataList.size() + 1)));

        dataX = entityDisplayX + entityDisplayWidth + margin;
        dataY = entityY + 2 * margin + textHeight + entityStatsGap;

        entitySettingX = entityX + margin;
        entitySettingY = entityDisplayY + entityDisplayHeight + margin;
        entitySettingButtonY = entitySettingY + textHeight + margin;

        List<Button> settingButtons = entitySettingButtonList();
        entitySettingButtonSize = Math.max(s(18, 14), Math.min(s(40, 28), Math.min(
                panelHeight - (entitySettingButtonY - entityY) - 2 * margin - textHeight,
                (panelWidth - (settingButtons.size() + 1) * margin) / Math.max(1, settingButtons.size()))));

        addDrawableChild(Button.builder()
                .position(dataX, dataY + dataList.size() * (textHeight + entityStatsGap) + 2 * margin)
                .size(entityEditButtonWidth, entityEditButtonHeight)
                .baseLabel(Text.of("Change Name"))
                .onClick(b -> openNameEditor())
                .backgroundColor(theme.body_button_background())
                .borderColor(theme.body_button_border())
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .foregroundColor(theme.body_button_foreground())
                .build());

        addDrawableChild(Button.builder()
                .position(dataX, dataY + dataList.size() * (textHeight + entityStatsGap) + 3 * margin + entityEditButtonHeight)
                .size(entityEditButtonWidth, entityEditButtonHeight)
                .baseLabel(Text.of("Change Skin"))
                .onClick(b -> openSkinEditor())
                .backgroundColor(theme.body_button_background())
                .borderColor(theme.body_button_border())
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .foregroundColor(theme.body_button_foreground())
                .build());

        int i = 0;
        for (Button b : entitySettingButtonList()) {
            b.setX(entitySettingX + margin + i++ * (margin + entitySettingButtonSize));
            b.setY(entitySettingButtonY);
            addDrawableChild(b);
        }
    }

    private void layoutControlPanelContents(int panelX, int panelY, int panelWidth, int panelHeight) {
        controlButtonX = panelX + 2 * margin;
        controlButtonY = panelY + 2 * margin + textHeight;
        controlButtonWidth = Math.max(s(60, 48), Math.min(s(80, 64), (panelWidth - 4 * margin) / 2));
        controlButtonHeight = s(20, 16);

        List<Button> controlButtons = controlButtons();
        int idx = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (idx >= controlButtons.size()) break;
                controlButtons.get(idx++).setPosition(
                        controlButtonX + col * (margin + controlButtonWidth),
                        controlButtonY + row * (margin + controlButtonHeight));
            }
        }
        controlButtons.forEach(this::addDrawableChild);

        controlSettingsY = controlButtonY + 3 * margin + 2 * controlButtonHeight;

        addDrawableChild(Button.builder()
                .position(controlButtonX, controlSettingsY + textHeight + margin)
                .size(controlButtonWidth, controlButtonHeight)
                .backgroundColor(theme.body_button_background())
                .borderColor(theme.body_button_border())
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .foregroundColor(theme.body_button_foreground())
                .baseLabel(Text.literal("Difficulty: ").append(ClientSideSettings.getDifficulty().displayText))
                .onClick(this::roleDifficulty)
                .build());

        addDrawableChild(Button.builder()
                .position(controlButtonX + margin + controlButtonWidth, controlSettingsY + textHeight + margin)
                .size(controlButtonWidth, controlButtonHeight)
                .backgroundColor(theme.body_button_background())
                .borderColor(theme.body_button_border())
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .foregroundColor(theme.body_button_foreground())
                .baseLabel(Text.literal("Mode: ").append(ClientSideSettings.getMode().displayText))
                .onClick(this::roleMode)
                .build());

        controlSettingButtonY = controlSettingsY + textHeight + 2 * margin + controlButtonHeight;

        List<Button> settingButtons = controlSettingButtonList();
        controlSettingButtonSize = Math.max(s(18, 14), Math.min(s(40, 28), Math.min(
                panelHeight - (controlSettingButtonY - panelY) - 2 * margin - textHeight,
                (panelWidth - (settingButtons.size() + 1) * margin) / Math.max(1, settingButtons.size()))));

        int i1 = 0;
        for (Button b : controlSettingButtonList()) {
            b.setX(controlButtonX + i1++ * (margin + controlSettingButtonSize));
            b.setY(controlSettingButtonY);
            addDrawableChild(b);
        }
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void roleDifficulty(Button b) {
        List<Difficulty> list = List.of(Difficulty.values());
        int currentIndex = list.indexOf(ClientSideSettings.getDifficulty());
        Difficulty value = list.get((currentIndex + 1) % list.size());
        ClientSideSettingsSyncHelper.setAndSend(SettingsKey.DIFFICULTY, value);
        b.setSingleLabel(Text.literal("Difficulty: ").append(value.displayText));
    }

    private void roleMode(Button b) {
        List<Mode> list = List.of(Mode.values());
        int currentIndex = list.indexOf(ClientSideSettings.getMode());
        Mode value = list.get((currentIndex + 1) % list.size());
        ClientSideSettingsSyncHelper.setAndSend(SettingsKey.MODE, value);
        b.setSingleLabel(Text.literal("Mode: ").append(value.displayText));
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        renderEntityPanel(c, mx, my);
        renderControlPanel(c, mx, my);
        super.render(c, mx, my, d);
    }

    private void renderControlPanel(DrawContext c, int mx, int my) {
        RenderUtils.setOrigin2d(c, controlX, controlY);
        c.fill(0, 0, controlPanelWidth, controlPanelHeight, theme.body_background());
        RenderUtils.resetOrigin(c);

        // FIX (was controlX, controlX - header rendered at the wrong Y):
        RenderUtils.setOrigin2d(c, controlX + margin, controlY + margin);
        drawSectionHeader(c, "Controls", controlPanelWidth, theme.body_foreground());
        RenderUtils.resetOrigin(c);

        RenderUtils.setOrigin2d(c, controlX + margin, controlSettingsY);
        drawSectionHeader(c, "Quick Settings", controlPanelWidth, theme.body_category_label());
        RenderUtils.resetOrigin(c);

        // FIX: this used to redraw the *entity* panel's labels a second time
        // here. Now it draws the control panel's own quick-setting labels,
        // under the control panel's own icon row.
        RenderUtils.setOrigin2d(c, controlButtonX, controlSettingButtonY + controlSettingButtonSize + margin);
        AtomicInteger i = new AtomicInteger(0);
        controlSettingButtonLabels().forEach(l ->
                c.drawCenteredTextWithShadow(getTextRenderer(), Text.of(l),
                        controlSettingButtonSize / 2 + i.getAndIncrement() * (margin + controlSettingButtonSize),
                        0, theme.body_label()));
        RenderUtils.resetOrigin(c);
    }

    private void renderEntityPanel(DrawContext c, int mx, int my) {
        RenderUtils.setOrigin2d(c, entityX, entityY);
        c.fill(0, 0, entityPanelWidth, entityPanelHeight, theme.body_background());
        RenderUtils.resetOrigin(c);

        RenderUtils.setOrigin2d(c, entityX + margin, entityY + margin);
        drawSectionHeader(c, "MaceBot", entityPanelWidth, theme.body_foreground());
        RenderUtils.resetOrigin(c);

        RenderUtils.setOrigin2d(c, entityDisplayX, entityDisplayY);
        c.fill(0, 0, entityDisplayWidth, entityDisplayHeight, theme.display_background());
        RenderUtils.resetOrigin(c);

        LivingEntity entity = MacebotDataGetter.getMacebot();
        LivingEntity finalEntity = entity != null ? entity : getArmorStandEntity();
        RenderUtils.drawEntity(c, entityDisplayX, entityDisplayY, entityDisplayX + entityDisplayWidth,
                entityDisplayY + entityDisplayHeight,
                (int) (entityDisplayHeight / finalEntity.getHeight() * 0.6), 0.0f, mx, my, finalEntity);

        RenderUtils.setOrigin2d(c, dataX, dataY);
        List<Data> dataList = entityDataList();
        AtomicInteger i = new AtomicInteger();
        dataList.forEach(dt -> {
            Text key = Text.empty().append(dt.key).append(":");
            int rowY = (textHeight + entityStatsGap) * i.get();
            c.drawText(getTextRenderer(), key, 0, rowY, theme.body_key(), true);
            c.drawText(getTextRenderer(), dt.value.get(), getTextRenderer().getWidth(key) + margin, rowY,
                    theme.body_value(), true);
            i.getAndIncrement();
        });
        RenderUtils.resetOrigin(c);

        RenderUtils.setOrigin2d(c, entitySettingX, entitySettingY);
        drawSectionHeader(c, "Quick Settings", entityPanelWidth, theme.body_category_label());
        RenderUtils.resetOrigin(c);

        RenderUtils.setOrigin2d(c, entitySettingX + margin, entitySettingButtonY + entitySettingButtonSize + margin);
        AtomicInteger j = new AtomicInteger(0);
        settingButtonLabels().forEach(l ->
                c.drawCenteredTextWithShadow(getTextRenderer(), Text.of(l),
                        entitySettingButtonSize / 2 + j.getAndIncrement() * (margin + entitySettingButtonSize),
                        0, theme.body_label()));
        RenderUtils.resetOrigin(c);
    }

    /** Draws the small "—— Label ——" section header used throughout both panels. */
    private void drawSectionHeader(DrawContext c, String label, int panelWidth, int color) {
        Text t = Text.of(label);
        int textWidth = getTextRenderer().getWidth(t);
        c.drawHorizontalLine(0, margin, textHeight / 2, color);
        c.drawText(getTextRenderer(), t, 2 * margin, 0, color, true);
        c.drawHorizontalLine(2 * margin + textWidth + margin,
                Math.max(2 * margin + textWidth + margin, panelWidth * 2 / 3 - margin), textHeight / 2, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public List<Data> entityDataList() {
        return List.of(
                new Data("Name", MacebotDataGetter::getDisplayName),
                new Data("Health", () -> Text.of((MacebotDataGetter.getHealth() == -1 ? "N/A" : MacebotDataGetter.getHealth())
                        + " / " + (MacebotDataGetter.getMaxHealth() == -1 ? 20 : MacebotDataGetter.getMaxHealth()))),
                new Data("Hunger", () -> Text.of((MacebotDataGetter.getHunger() == -1 ? "N/A" : MacebotDataGetter.getHunger()) + " / 20")),
                new Data("Target", () -> MacebotDataGetter.getTarget() == null ? Text.of("None") : MacebotDataGetter.getTarget().getDisplayName())
        );
    }

    public List<Button> controlButtons() {
        return List.of(
                newControlButton("Spawn", b -> MaceBotCommandSender.spawn(), () -> !ClientSideSettings.isMacebotOnline()),
                newControlButton("Kick", b -> MaceBotCommandSender.kick(), ClientSideSettings::isMacebotOnline),
                newControlButton("Play", b -> MaceBotCommandSender.play(), () -> ClientSideSettings.isMacebotOnline() && ClientSideSettings.getMode() == Mode.NPC),
                newControlButton("Stop", b -> MaceBotCommandSender.stop(), () -> ClientSideSettings.isMacebotOnline() && ClientSideSettings.getMode() != Mode.NPC)
        );
    }

    private Button newControlButton(String label, Consumer<Button> onClick, Supplier<Boolean> sup) {
        return Button.builder()
                .size(controlButtonWidth, controlButtonHeight)
                .backgroundColor(theme.body_button_background())
                .borderColor(theme.body_button_border())
                .hoverColor(theme.body_button_background() + 0xFF101010)
                .holdColor(theme.body_button_background() + 0xFF202020)
                .foregroundColor(theme.body_button_foreground())
                .activeSupplier(sup)
                .baseLabel(Text.of(label))
                .onClick(onClick)
                .build();
    }

    /** Quick-setting icon buttons for the entity panel - now wired to real textures. */
    public List<Button> entitySettingButtonList() {
        List<Button.Builder> builders = List.of(
                Button.builder().texture(MaceBotTextures.QUICK_KITS).onClick(b -> {}),
                Button.builder().toggleTexture(MaceBotTextures.QUICK_BUFF).toggleTexture(MaceBotTextures.QUICK_BUFF).toggled(ClientSideSettings.isMacebotBuffsEnabled()).onClick(b -> ClientSideSettingsSyncHelper.setAndSend(SettingsKey.MACEBOT_BUFFS_ENABLED,!ClientSideSettings.isMacebotBuffsEnabled())),
                Button.builder().toggleTexture(MaceBotTextures.QUICK_REFILL).toggleTexture(MaceBotTextures.QUICK_REFILL_DISABLED).toggled(ClientSideSettings.isMacebotAutoRefillEnabled()).onClick(b -> ClientSideSettingsSyncHelper.setAndSend(SettingsKey.MACEBOT_AUTO_REFILL_ENABLED,!ClientSideSettings.isMacebotAutoRefillEnabled())),
                Button.builder().texture(MaceBotTextures.QUICK_TELEPORT).onClick(b -> {}),
                Button.builder().texture(MaceBotTextures.QUICK_SETTINGS).onClick(b -> {})
        );
        List<Button> list = new ArrayList<>();
        for(Button.Builder b: builders){
            list.add(b.size(entitySettingButtonSize)
                    .backgroundColor(theme.body_button_background())
                    .borderColor(theme.body_button_border())
                    .hoverColor(theme.body_button_background() + 0xFF101010)
                    .holdColor(theme.body_button_background() + 0xFF202020)
                    .foregroundColor(theme.body_button_foreground()).build());
        }
        return list;
    }

    /** Quick-setting icon buttons for the control panel - mirrors the entity panel's set. */
    public List<Button> controlSettingButtonList() {
        List<Button.Builder> builders = List.of(
                Button.builder().texture(MaceBotTextures.QUICK_KITS).onClick(b -> {}),
                Button.builder().toggleTexture(MaceBotTextures.QUICK_BUFF).toggleTexture(MaceBotTextures.QUICK_BUFF_DISABLED).toggled(ClientSideSettings.isPlayerBuffsEnabled()).onClick(b -> ClientSideSettingsSyncHelper.setAndSend(SettingsKey.PLAYER_BUFFS_ENABLED,!ClientSideSettings.isPlayerBuffsEnabled())),
                Button.builder().toggleTexture(MaceBotTextures.QUICK_REFILL).toggleTexture(MaceBotTextures.QUICK_REFILL_DISABLED).toggled(ClientSideSettings.isPlayerAutoRefillEnabled()).onClick(b -> ClientSideSettingsSyncHelper.setAndSend(SettingsKey.PLAYER_AUTO_REFILL_ENABLED,!ClientSideSettings.isPlayerAutoRefillEnabled())),
                Button.builder().texture(MaceBotTextures.QUICK_TELEPORT).onClick(b -> {}),
                Button.builder().texture(MaceBotTextures.QUICK_SETTINGS).onClick(b -> {})
        );
        List<Button> list = new ArrayList<>();
        for(Button.Builder b: builders){
            list.add(b
                    .size(controlSettingButtonSize)
                    .backgroundColor(theme.body_button_background())
                    .borderColor(theme.body_button_border())
                    .hoverColor(theme.body_button_background() + 0xFF101010)
                    .holdColor(theme.body_button_background() + 0xFF202020)
                    .foregroundColor(theme.body_button_foreground()).build());
        }
        return list;
    }

    public List<String> settingButtonLabels() {
        return List.of("Kits", "Buff", "Refill", "Teleport", "Settings");
    }

    /** Labels for the control panel's quick-setting row (currently mirrors the entity panel's). */
    public List<String> controlSettingButtonLabels() {
        return List.of("Kits", "Buff", "Refill", "Teleport", "Settings");
    }

    ArmorStandEntity armorStand;

    public ArmorStandEntity getArmorStandEntity() {
        if (armorStand == null) {
            armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, parentScreen.getClient().world);
        }
        return armorStand;
    }

    public static class Data {
        public String key;
        public Supplier<Text> value;

        public Data(String key, Supplier<Text> value) {
            this.key = key;
            this.value = value;
        }
    }

    protected void openNameEditor() {
    }

    protected void openSkinEditor() {
    }
}
