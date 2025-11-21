package net.katch0420.macebot.playerbot;

public class PlayerBotSettings {
    public static boolean elytra = true;
    public static boolean autoRefill = false;

    public static boolean toggleElytra(){
        elytra = !elytra;
        return elytra;
    }

    public static boolean toggleAutoRefill(){
        autoRefill = !autoRefill;
        return autoRefill;
    }
}
