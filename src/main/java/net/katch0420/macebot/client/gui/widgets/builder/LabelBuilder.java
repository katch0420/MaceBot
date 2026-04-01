package net.katch0420.macebot.client.gui.widgets.builder;

import net.minecraft.text.Text;

public class LabelBuilder<T extends LabelBuilder<T>> extends ChildBuilder<T>{
    public Text label;
    public int color = 0xAA101010;
    public int textColor = 0xFFEEEEEE;

    public T label(String label){
        this.label = Text.literal(label);
        return (T) this;
    }

    public T color(int color){
        this.color = color;
        return (T) this;
    }

    public T textColor(int textColor){
        this.textColor = textColor;
        return (T) this;
    }
}
