package net.katch0420.macebot.main.settings.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.katch0420.macebot.main.settings.main.Flags.Flag;

public class FlagContainer {
    private final List<Flag> flags = new ArrayList<>();

    public boolean contains(Flag flag){
        return flags.contains(flag);
    }

    public void add(Flag flag){
        flags.add(flag);
    }

    public void clear(){
        flags.clear();
    }

    public List<Flag> getFlags(){
        return flags;
    }

    FlagContainer(Flag... flags){
        this.flags.addAll(Arrays.asList(flags));
    }

    public void addFlags(Flag[] flags) {
        this.flags.addAll(Arrays.asList(flags));
    }
}
