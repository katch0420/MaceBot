package net.katch0420.macebot.client.gui.widgets.builder;

public class Builder<T extends Builder<T>> {
    protected int x = 0;
    protected int y = 0;
    protected int width = 20;
    protected int height = 20;

    @SuppressWarnings("unchecked")
    public T x(int x) {
        this.x = x;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T y(int y) {
        this.y = y;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T width(int width) {
        this.width = width;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T height(int height) {
        this.height = height;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T size(int width, int height) {
        this.width = width;
        this.height = height;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T size(int w) {
        this.width = w;
        this.height = w;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T position(int x, int y) {
        this.x = x;
        this.y = y;
        return (T) this;
    }
}
