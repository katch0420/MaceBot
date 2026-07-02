package net.katch0420.macebot.client.gui.widgets.builder;

import net.katch0420.macebot.client.gui.widgets.core.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonBuilder<B extends ButtonWidget<B>, T extends ButtonBuilder<B,T>> extends LabelBuilder<T>{
    protected Consumer<B> onClick;
    /** New: optional right-click handler, wired through Button.Builder#build(). */
    protected Consumer<B> onRightClick;
    /** New: optional keyboard-activation (Enter/Space while focused) handler. */
    protected Consumer<B> onKeyActivate;
    protected Supplier<Boolean> activeSupplier = () -> true;
    protected Tooltip tooltip;

    @SuppressWarnings("unchecked")
    public T onClick(Consumer<B> onClick) {
        this.onClick = onClick;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T onRightClick(Consumer<B> onRightClick) {
        this.onRightClick = onRightClick;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T onKeyActivate(Consumer<B> onKeyActivate) {
        this.onKeyActivate = onKeyActivate;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T tooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T activeSupplier(Supplier<Boolean> activeSupplier) {
        this.activeSupplier = activeSupplier;
        return (T) this;
    }

    /** Convenience over activeSupplier(...) for a constant enabled/disabled state. */
    @SuppressWarnings("unchecked")
    public T disabled() {
        this.activeSupplier = () -> false;
        return (T) this;
    }
}
