package net.katch0420.macebot.main.macebot.ai;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;


public class Analyzer {
    Core core;
    ServerPlayer enemy;
    AnalysisData analysisData;
    public Analyzer(Core core) {
        this.core = core;
        this.enemy = core.enemy;
        this.analysisData = core.analysisData;
    }

    public enum WeaponType{
        MELEE,
        RANGED,
        SLAM,
        UTIL,
        BLOCK,
        NONE
    }

    public WeaponType getOpponentWeaponType(){
        if(enemy == null) return null;
        ItemStack s = enemy.getMainHandItem();
        if(s.has(DataComponents.WEAPON) || s.getItem() instanceof AxeItem) return WeaponType.MELEE;
        if(s.getItem() instanceof MaceItem) return WeaponType.SLAM;
        if(s.getItem() instanceof CrossbowItem || s.getItem() instanceof BowItem) return WeaponType.RANGED;
        if(s.getItem() instanceof ShieldItem) return WeaponType.BLOCK;
        if(s.getItem() instanceof WindChargeItem || s.getItem() instanceof EnderpearlItem) return WeaponType.UTIL;
        return WeaponType.NONE;
    }
}
