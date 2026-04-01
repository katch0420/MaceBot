package net.katch0420.macebot.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.katch0420.macebot.client.gui.widgets.EmptyWidget;
import net.katch0420.macebot.client.gui.widgets.LabelWidget;
import net.katch0420.macebot.client.gui.widgets.layout.VerticalLayout;
import net.katch0420.macebot.client.gui.widgets.panels.PanelWidget;
import net.katch0420.macebot.client.gui.widgets.buttons.ClickButtonWidget;
import net.katch0420.macebot.client.gui.widgets.buttons.ClickListButtonWidget;
import net.katch0420.macebot.client.gui.widgets.buttons.ClickToggleButtonWidget;
import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.client.ClientSideSettingsSyncHelper;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class OptionScreen extends Screen {

    Screen parent;

    private static final int MARGIN = 5;

    // Enum option lists derived from the enums themselves — never go out of sync
    private static final List<String> DIFFICULTIES = Arrays.stream(Controller.Difficulty.values())
            .map(e -> capitalize(e.name()))
            .collect(Collectors.toList());

    private static final List<String> MODES = Arrays.stream(Controller.Mode.values())
            .map(e -> capitalize(e.name()))
            .collect(Collectors.toList());

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private int titleHeight;
    private int activeCategory = 0;

    private PanelWidget bodyWidget;
    private PanelWidget categories;
    private PanelWidget options;

    // ── Category nav ──────────────────────────────────────────────────────────

    private final List<ChildWidget> cWidgets = List.of(
            ClickButtonWidget.builder().onClick(b -> selectOption(0)).label("MaceBot").build(),
            ClickButtonWidget.builder().onClick(b -> selectOption(1)).label("Player").build(),
            ClickButtonWidget.builder().onClick(b -> selectOption(2)).label("General").build(),
            EmptyWidget.builder().build(),
            ClickButtonWidget.builder().onClick(b -> close()).label("Control Panel").build()
    );

    // ── MaceBot tab ───────────────────────────────────────────────────────────

    private final List<ChildWidget> c1Widgets = List.of(

            // Difficulty — cycles through Controller.Difficulty values
            cycleEnum(SettingsKey.DIFFICULTY, DIFFICULTIES,
                    capitalize(ClientSideSettings.getDifficulty().name()),
                    s -> Controller.Difficulty.valueOf(s.toUpperCase(Locale.ROOT))),

            EmptyWidget.builder().build(),

            // Boolean toggles — all driven by SettingsKey, no switch needed
            boolToggle(SettingsKey.MACEBOT_CAN_DO_TRACKING,     "Allow Tracking"),
            boolToggle(SettingsKey.MACEBOT_CAN_USE_ELYTRA,      "Allow Elytra"),
            boolToggle(SettingsKey.MACEBOT_CAN_DO_ATTACK,       "Allow Attacks"),
            boolToggle(SettingsKey.MACEBOT_CAN_USE_SHIELD,      "Allow Shield §e[Experimental]"),
            boolToggle(SettingsKey.MACEBOT_CAN_DO_MACE_ATTACK,  "Allow Mace Attack"),
            boolToggle(SettingsKey.MACEBOT_CAN_DO_KB_HIT,       "Allow KnockBack Hits"),
            boolToggle(SettingsKey.MACEBOT_CAN_DO_CRIT_HIT,     "Allow Crit Hits"),

            EmptyWidget.builder().build(),

            boolToggle(SettingsKey.MACEBOT_AUTO_REFILL_ENABLED, "Auto Refill"),
            boolToggle(SettingsKey.MACEBOT_BUFFS_ENABLED,       "Buffs")
    );

    // ── Player tab ────────────────────────────────────────────────────────────

    private final List<ChildWidget> c2Widgets = List.of(
            boolToggle(SettingsKey.PLAYER_AUTO_REFILL_ENABLED, "Auto Refill"),
            boolToggle(SettingsKey.PLAYER_BUFFS_ENABLED,       "Buffs")
    );

    // ── General tab ───────────────────────────────────────────────────────────

    private final List<ChildWidget> c3Widgets = List.of(

            // Mode — cycles through Controller.Mode values
            cycleEnum(SettingsKey.MODE, MODES,
                    capitalize(ClientSideSettings.getMode().name()),
                    s -> Controller.Mode.valueOf(s.toUpperCase(Locale.ROOT))),

            EmptyWidget.builder().build(),

            boolToggle(SettingsKey.ACTION_BAR_MESSAGES_ENABLED, "Allow Action Bar Messages"),
            boolToggle(SettingsKey.CHAT_MESSAGES_ENABLED,        "Allow Chat Messages")
    );

    // ── Widget factories ──────────────────────────────────────────────────────

    /**
     * Boolean toggle. Reads initial state from ClientSideSettings via the key getter.
     * On click: applies locally + sends to server which rebroadcasts to all clients.
     */
    private static ChildWidget boolToggle(SettingsKey key, String label) {
        return ClickToggleButtonWidget.builder()
                .onClick(b -> changeSetting(key, ((ClickToggleButtonWidget) b).isToggled()))
                .label(label)
                .toggled((Boolean) key.getClientValue())
                .build();
    }

    /**
     * Enum cycle button (like Difficulty, Mode).
     * Cycles through the provided display-name list, parses back to the enum value,
     * then sends to server. Adding a new enum value just means updating the enum —
     * no code changes here needed.
     *
     * @param key         the SettingsKey (must be an enum type)
     * @param displayList capitalized display names matching enum ordinal order
     * @param initial     the currently active display name
     * @param parser      converts a display string back to the enum value
     */
    private static ChildWidget cycleEnum(SettingsKey key, List<String> displayList,
                                         String initial, java.util.function.Function<String, Object> parser) {
        return ClickListButtonWidget.builder()
                .onClick(b -> {
                    ClickListButtonWidget btn = (ClickListButtonWidget) b;
                    // Advance to next item in the cycle
                    int i = displayList.indexOf(btn.getCurrent().getString());
                    String next = displayList.get((i + 1) % displayList.size());
                    btn.setCurrent(Text.literal(next));
                    // Parse back to enum and send
                    changeSetting(key, parser.apply(next));
                })
                .label(key.name().charAt(0) + key.name().substring(1).toLowerCase().replace('_', ' '))
                .option(Text.literal(initial))
                .build();
    }

    // ── Setting change ────────────────────────────────────────────────────────

    /**
     * Apply locally (instant UI feedback) and send C2S.
     * Server applies + broadcasts back to every connected client.
     */
    private static void changeSetting(SettingsKey key, Object value) {
        ClientSideSettingsSyncHelper.setAndSend(key, value);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    public OptionScreen(Screen parent) { super(Text.of("Macebot Options")); this.parent = parent;}

    @Override
    protected void init() {
        initLayout();
        initBody();
        initCategory();
        initOptions();
    }

    private void initLayout() {
        titleHeight = getWindowHeight() / 15;

        addDrawableChild(LabelWidget.builder()
                .label(title.getString())
                .position(0, 0)
                .color(0x80111111)
                .size(getWindowWidth(), titleHeight)
                .build());

        bodyWidget = PanelWidget.builder()
                .position(0, titleHeight)
                .size(getWindowWidth(), getWindowHeight() - titleHeight)
                .color(0x00000000)
                .build();
        addDrawableChild(bodyWidget);
    }

    private void initBody() {
        int marginX = MARGIN * 3;

        categories = (PanelWidget) PanelWidget.builder()
                .size((getWindowWidth() - marginX) / 4, 80)
                .color(0x00000000)
                .layout(new VerticalLayout())
                .build()
                .setParent(bodyWidget)
                .setRelativePos(MARGIN, MARGIN);

        options = (PanelWidget) PanelWidget.builder()
                .size((getWindowWidth() - marginX) / 4 * 3,
                        getWindowHeight() - (MARGIN + titleHeight) * 2)
                .color(0x00000000)
                .layout(new VerticalLayout())
                .build()
                .setParent(bodyWidget)
                .setRelativePos(MARGIN * 2 + categories.getWidth(), MARGIN);
    }

    private void initCategory() {
        int width  = categories.getWidth();
        int height = textRenderer.fontHeight * 2;
        categories.clearChildren();
        for (ChildWidget child : cWidgets) {
            child.setSize(width, child instanceof EmptyWidget ? textRenderer.fontHeight : height);
            child.setParent(categories);
        }
        categories.setDynamicHeight();
    }

    private void initOptions() {
        int width  = options.getWidth();
        int height = textRenderer.fontHeight * 2;

        List<ChildWidget> active = switch (activeCategory) {
            case 1  -> c2Widgets;
            case 2  -> c3Widgets;
            default -> c1Widgets;
        };

        options.clearChildren();
        for (ChildWidget child : active) {
            child.setSize(width, child instanceof EmptyWidget ? textRenderer.fontHeight : height);
            child.setParent(options);
        }
        options.setDynamicHeight();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()) {
            close();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void selectOption(int id) {
        activeCategory = id;
        initOptions();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private int getWindowWidth()  { return mc.getWindow().getScaledWidth();  }
    private int getWindowHeight() { return mc.getWindow().getScaledHeight(); }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}