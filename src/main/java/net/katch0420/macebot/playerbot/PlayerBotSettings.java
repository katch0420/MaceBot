package net.katch0420.macebot.playerbot;

public class PlayerBotSettings {
    public static boolean elytra = true;
    public static boolean autoRefill = true;
    public static boolean crits = true;
    public static boolean mace = true;
    public static boolean attack = true;

    public static boolean toggleElytra(){
        elytra = !elytra;
        return elytra;
    }

    public static boolean toggleAutoRefill(){
        autoRefill = !autoRefill;
        return autoRefill;
    }

    public static boolean toggleCrits(){
        crits = !crits;
        return crits;
    }

    public static boolean toggleMace(){
        mace = !mace;
        return mace;
    }

    public static boolean toggleAttack(){
        attack = !attack;
        return attack;
    }
}
