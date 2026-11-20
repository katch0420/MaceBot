package net.katch0420.macebot.client.gui.widgets.builder;

import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.gui.widgets.buttons.IntegerSliderButton;
import net.katch0420.macebot.main.settings.Setting;
import net.katch0420.macebot.main.settings.SettingManager;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

/** Type-based setting editor registry with per-setting presentation metadata. */
public final class SettingControlRegistry {
    private static final Map<Class<?>, ControlFactory<?>> FACTORIES = new HashMap<>();
    private static final Map<String, BooleanLabels> BOOLEAN_LABELS = new HashMap<>();
    private static final Map<String, IntegerRange> INTEGER_RANGES = new HashMap<>();

    static {
        register(Boolean.class, (setting, context) -> {
            boolean value = Boolean.TRUE.equals(setting.getValue());
            BooleanLabels labels = BOOLEAN_LABELS.getOrDefault(setting.getId(), BooleanLabels.DEFAULT);
            int color = value ? context.theme().success() : context.theme().danger();
            return Button.builder()
                    .baseLabel(value ? labels.enabled() : labels.disabled())
                    .backgroundColor(color)
                    .foregroundColor(0xFFFFFFFF)
                    .borderColor(context.theme().body_button_border())
                    .hoverColor(color + 0xFF101010)
                    .holdColor(color + 0xFF202020)
                    .onClick(button -> {
                        setting.toggleAndGet();
                        context.refresh().run();
                    })
                    .build();
        });
        register(Integer.class, (setting, context) -> {
            IntegerRange range = INTEGER_RANGES.get(setting.getId());
            if (range == null) {
                return Button.builder()
                        .baseLabel(Component.literal(String.valueOf(setting.getValue())))
                        .backgroundColor(context.theme().body_button_background())
                        .foregroundColor(context.theme().body_button_foreground())
                        .borderColor(context.theme().body_button_border())
                        .disabled()
                        .build();
            }
            Setting<Integer> integerSetting = SettingManager.getSetting(setting.getId());
            int clampedValue = Math.max(range.min(), Math.min(range.max(), integerSetting.getValue()));
            if (clampedValue != integerSetting.getValue()) integerSetting.update(clampedValue);
            return new IntegerSliderButton(0, 0, 1, 1, range.min(), range.max(), range.step(),
                    clampedValue, context.theme(), integerSetting::update);
        });

        registerIntegerSlider(Settings.SKILL.getId(), 10, 100, 1);
        setBooleanLabels(Settings.CHAT_MESSAGES.getId(), Component.literal("Enabled"), Component.literal("Disabled"));
    }

    private SettingControlRegistry() {}

    public static <T> void register(Class<T> type, Factory<T> factory) {
        registerInternal(type, factory);
    }

    public static void setBooleanLabels(String settingId, Component enabled, Component disabled) {
        BOOLEAN_LABELS.put(settingId, new BooleanLabels(enabled, disabled));
    }

    public static void registerIntegerSlider(String settingId, int min, int max, int step) {
        INTEGER_RANGES.put(settingId, new IntegerRange(min, max, step));
    }

    public static Button create(Setting<?> setting, Theme theme, Runnable refresh) {
        ControlFactory<?> factory = FACTORIES.get(setting.getType());
        return factory == null ? null : createTyped(factory, setting, new Context(theme, refresh));
    }

    @SuppressWarnings("unchecked")
    private static <T> Button createTyped(ControlFactory<?> factory, Setting<?> setting, Context context) {
        if (setting.getType() != factory.type()) return null;
        return ((ControlFactory<T>) factory).create((Setting<T>) setting, context);
    }

    public record Context(Theme theme, Runnable refresh) {}
    public record BooleanLabels(Component enabled, Component disabled) {
        private static final BooleanLabels DEFAULT = new BooleanLabels(Component.literal("On"), Component.literal("Off"));
    }
    public record IntegerRange(int min, int max, int step) {
        public IntegerRange {
            if (min > max) throw new IllegalArgumentException("Minimum must not exceed maximum");
            if (step < 1) throw new IllegalArgumentException("Step must be positive");
        }
    }

    @FunctionalInterface
    public interface Factory<T> {
        Button create(Setting<T> setting, Context context);
    }

    private record ControlFactory<T>(Class<T> type, Factory<T> factory) {
        private Button create(Setting<T> setting, Context context) {
            return factory.create(setting, context);
        }
        }

    private static <T> void registerInternal(Class<T> type, Factory<T> factory) {
        FACTORIES.put(type, new ControlFactory<>(type, factory));
    }
}
