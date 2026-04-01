package net.katch0420.macebot.client.gui.widgets.builder;

public class Builder<T extends Builder<T>> {
    public int x = 0;
    public int y = 0;
    public int width = 20;
    public int height = 20;

    public T x(int x){
        this.x = x;
        return (T) this;
    }

    public T y(int y){
        this.y = y;
        return (T) this;
    }

    public T width(int width){
        this.width = width;
        return (T) this;
    }

    public T height(int height){
        this.height = height;
        return (T) this;
    }

    public T size(int width, int height){
        this.width = width;
        this.height = height;
        return (T) this;
    }

    public T position(int x, int y){
        this.x = x;
        this.y = y;
        return (T) this;
    }
}
