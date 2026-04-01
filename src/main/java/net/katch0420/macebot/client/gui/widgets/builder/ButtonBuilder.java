package net.katch0420.macebot.client.gui.widgets.builder;

import net.katch0420.macebot.client.gui.widgets.core.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;

import java.util.function.Consumer;

public class ButtonBuilder<T extends ButtonBuilder<T>> extends LabelBuilder<T>{
    public Consumer<ButtonWidget> onClick;
    public Tooltip tooltip;

    public T onClick(Consumer<ButtonWidget> onClick){
        this.onClick = onClick;
        return (T) this;
    }

    public T tooltip(Tooltip tooltip){
        this.tooltip = tooltip;
        return (T) this;
    }
}
