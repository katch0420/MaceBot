package net.katch0420.macebot.main.macebot.ai;

import static net.katch0420.macebot.main.settings.Settings.SKILL;

public class Skill {
    public Core core;

    public double skill0;
    public Skill(Core core) {
        this.core = core;
    }

    public void tick(){
        skill0 = getSkillFloat();
    }

    public boolean isSkillChanged(){
        return skill0 != getSkillFloat();
    }

    public boolean canHit() {
        return Math.random() <= getSkillFloat() + 0.8;
    }

    public boolean missHit(){return (1.2f - getSkillFloat()) < Math.random();}

    public boolean canBlock() {
        return Math.random() <= getSkillFloat() + 0.3;
    }

    public boolean canDeflect() {
        return Math.random() <= getSkillFloat() + 0.2;
    }

    public double getChancePercentage(int percentage){
        return Math.max(1, 1 - (double) percentage / 100 + getSkillFloat());
    }

    public float getSkillFloat(){
        return (float) SKILL.getValue() / 100;
    }

    public int getSkillPercentage(){
        return SKILL.getValue();
    }
}
