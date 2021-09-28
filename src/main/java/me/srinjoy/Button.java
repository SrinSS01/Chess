package me.srinjoy;

import java.util.List;

public abstract class Button<T extends Button<T>> {
    final IButtonCallback<T> callback;
    float x, y;
    int width, height;
    float[] colour;

    public Button(float x, float y, int width, int height, float[] colour, IButtonCallback<T> callback) {
        this.callback = callback;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colour = colour;
    }
    abstract public List<Float> getVertices();
    public abstract void click(double x, double y, int action);
    boolean hover(double x, double y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }
    void addOffsets(float _x, float _y) {
        x += _x;
        y += _y;
    }
    void render() {}

    @FunctionalInterface
    interface IButtonCallback<T> {
        void invoke(T button);
    }
}
