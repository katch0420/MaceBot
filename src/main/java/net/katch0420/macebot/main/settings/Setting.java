package net.katch0420.macebot.main.settings;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.network.C2SPacketManager;
import net.katch0420.macebot.main.network.S2CPacketManager;
import net.katch0420.macebot.main.network.packets.c2s.SettingC2SPacket;
import net.katch0420.macebot.main.network.packets.s2c.SettingS2CPacket;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class Setting<T> {

    private final Class<T> type;
    private Component displayName;
    private final String id;
    private final SettingsCategories.SettingCategory category;
    private T value;

    private final Function<String, T> parser;
    private final Function<T, String> serializer;

    private final FlagContainer flags = new FlagContainer();
    private int integerMin = Integer.MIN_VALUE;
    private int integerMax = Integer.MAX_VALUE;

    public Setting(
            Class<T> type,
            Component displayName,
            String id,
            SettingsCategories.SettingCategory category,
            T value
    ) {
        this(type, displayName, id, category, value, null, null);
    }

    public Setting(
            Class<T> type,
            Component displayName,
            String id,
            SettingsCategories.SettingCategory category,
            T value,
            Function<String, T> parser,
            Function<T, String> serializer
    ) {
        this.type = type;
        this.displayName = displayName;
        this.id = id;
        this.category = category;
        this.value = value;

        this.parser = parser;
        this.serializer = serializer;
    }

    public String getId() {
        return id;
    }

    public T getValue() {
        return value;
    }

    public SettingsCategories.SettingCategory getCategory() {
        return category;
    }

    public Class<T> getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public void setValue(T value) {
        if (type == Integer.class && value != null) {
            int integerValue = (Integer) value;
            value = (T) Integer.valueOf(Math.clamp(integerValue, integerMin, integerMax));
        }
        this.value = value;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }

    public boolean isBoolean() {
        return type == Boolean.class;
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public Setting<T> setIntegerRange(int min, int max) {
        if (type != Integer.class) {
            throw new IllegalStateException("Integer ranges can only be set on Integer settings");
        }
        if (min > max) {
            throw new IllegalArgumentException("Minimum must not exceed maximum");
        }
        integerMin = min;
        integerMax = max;
        if (value != null) setValue(value);
        return this;
    }

    public int getIntegerMin() {
        if (type != Integer.class) throw new IllegalStateException("Setting is not an Integer setting");
        return integerMin;
    }

    public int getIntegerMax() {
        if (type != Integer.class) throw new IllegalStateException("Setting is not an Integer setting");
        return integerMax;
    }

    @SuppressWarnings("unchecked")
    public boolean getAndToggle() {
        if (!isBoolean()) {
            throw new IllegalStateException(
                    "Setting '" + id + "' is not a Boolean setting"
            );
        }

        boolean old = (Boolean) value;
        update((T) Boolean.valueOf(!old));

        return old;
    }

    @SuppressWarnings("unchecked")
    public boolean toggleAndGet() {
        if (!isBoolean()) {
            throw new IllegalStateException(
                    "Setting '" + id + "' is not a Boolean setting"
            );
        }

        update((T) Boolean.valueOf(!(Boolean) value));

        return (Boolean) value;
    }

    public T getAndRoll() {
        if (!isEnum()) {
            throw new IllegalStateException(
                    "Setting '" + id + "' is not an Enum setting"
            );
        }

        T old = value;
        update(nextEnumValue());

        return old;
    }

    public T rollAndGet() {
        if (!isEnum()) {
            throw new IllegalStateException(
                    "Setting '" + id + "' is not an Enum setting"
            );
        }

        T next = nextEnumValue();
        update(next);

        return value;
    }

    public FlagContainer getFlags() {
        return flags;
    }

    public Setting<T> setFlags(Flags.Flag... flags) {
        this.flags.clear();
        this.flags.addFlags(flags);
        return this;
    }

    public Setting<T> addFlags(Flags.Flag... flags) {
        this.flags.addFlags(flags);
        return this;
    }

    public boolean hasFlag(Flags.Flag flag) {
        return flags.contains(flag);
    }

    public T nextEnumValue() {
        if (!isEnum()) {
            throw new IllegalStateException(
                    "Setting '" + id + "' is not an Enum setting"
            );
        }

        T[] constants = type.getEnumConstants();

        Enum<?> current = (Enum<?>) value;

        return constants[
                (current.ordinal() + 1) % constants.length
                ];
    }

    public String serialize() {
        if (serializer != null) {
            return serializer.apply(value);
        }

        return String.valueOf(value);
    }

    public T parse(String raw) {
        if (parser == null) {
            throw new IllegalStateException(
                    "No parser registered for setting '" + id + "'"
            );
        }

        return parser.apply(raw);
    }

    public void update(T val) {
        switch (Env.getEnv()) {

            case CLIENT -> {
                if (hasFlag(Flags.SETTING_SERVER_ONLY)) {
                    MaceBot.debugLog(
                            "Server-only setting edited on client: " + id
                    );
                } else {
                    setValue(val);
                    updateToServer();
                }
            }

            case SERVER -> {
                if (hasFlag(Flags.SETTING_CLIENT_ONLY)) {
                    MaceBot.debugLog(
                            "Client-only setting edited on server: " + id
                    );
                } else {
                    setValue(val);
                    updateToClients();
                }
            }

            case COMMON -> setValue(val);
        }
    }

    public void updateToClients() {
        if(hasFlag(Flags.SETTING_SERVER_ONLY)) return;
        S2CPacketManager.sendToAll(
                new SettingS2CPacket(
                        getId(),
                        serialize()
                )
        );
    }

    public void updateToServer() {
        if(hasFlag(Flags.SETTING_CLIENT_ONLY)) return;
        C2SPacketManager.send(
                new SettingC2SPacket(
                        getId(),
                        serialize()
                )
        );
    }
}