package net.katch0420.macebot.main.settings.main;

public class Flags {
    public static final Flag EXPERIMENTAL = new Flag("experimental");
    public static final Flag RESTRICTED = new Flag("restricted");

    public record Flag(String flag){}
}
