package net.katch0420.macebot.main.settings;

public class Flags {
    public static final Flag SETTING_EXPERIMENTAL = new Flag("setting_experimental");
    public static final Flag SETTING_CAN_NOT_MODIFY = new Flag("setting_can_not_modify");
    public static final Flag SETTING_RESTRICTED = new Flag("setting_restricted");
    public static final Flag SETTING_CLIENT_ONLY = new Flag("setting_client_only");
    public static final Flag SETTING_SERVER_ONLY = new Flag("setting_server_only");
    public static final Flag SETTING_SYNC_NOT_NECESSARY = new Flag("setting_sync_not_necessary");
    public static final Flag CATEGORY_DO_NOT_DISPLAY = new Flag("category_do_not_display");

    public record Flag(String flag){}
}
