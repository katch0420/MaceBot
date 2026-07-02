package net.katch0420.macebot.client.gui.widgets.builder;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class LabelBuilder<T extends LabelBuilder<T>> extends ChildBuilder<T>{
    protected List<Text> label = new ArrayList<>();
    protected int backgroundColor = 0xFF000000;
    protected int foregroundColor = 0xFFFFFFFF;
    protected int holdColor = backgroundColor + 0x80888888;
    protected int hoverColor = backgroundColor + 0x80666666;
    protected int borderColor = 0;

    @SuppressWarnings("unchecked")
    public T label(String label){
        this.label.add(Text.literal(label));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T label(Text label){
        this.label.add(label);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T backgroundColor(int backgroundColor){
        this.backgroundColor = backgroundColor;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T foregroundColor(int foregroundColor){
        this.foregroundColor = foregroundColor;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T holdColor(int holdColor){
        this.holdColor = holdColor;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T hoverColor(int hoverColor){
        this.hoverColor = hoverColor;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T borderColor(int borderColor){
        this.borderColor = borderColor;
        return (T) this;
    }
}
