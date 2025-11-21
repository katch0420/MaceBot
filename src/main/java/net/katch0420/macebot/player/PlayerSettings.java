package net.katch0420.macebot.player;

public class PlayerSettings {
    public static boolean autoRefill = false;
    public static boolean buffs = false;

    public static boolean toggleAutoRefill(){
        autoRefill = !autoRefill;
        return autoRefill;
    }

    public static boolean toggleBuffs(){
        buffs = !buffs;
        return buffs;
    }
}
