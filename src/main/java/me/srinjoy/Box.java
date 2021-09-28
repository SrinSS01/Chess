package me.srinjoy;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

@SuppressWarnings("unused")
public class Box<T extends Button<T>> {
    public boolean isActive;
    float x, y;
    T previousButton = null;
    final ArrayList<T> BUTTONS = new ArrayList<>();

    public Box(float x, float y, boolean isActive) {
        this.isActive = isActive;
        this.x = x;
        this.y = y;
    }
    public void addButton(T button) {
        button.addOffsets(x, y);
        BUTTONS.add(button);
    }
    public T get(int index) {
        return BUTTONS.get(index);
    }
    public void render() {
        if (!BUTTONS.isEmpty()) {
            var buffer = new ArrayList<Float>();
            for (T button : BUTTONS) {
                var vertices = button.getVertices();
                buffer.addAll(vertices);
            }
            var typedArrayBuffer = new float[buffer.size()]; int[] index = { 0 };
            buffer.forEach(f -> typedArrayBuffer[index[0]++] = f);
            glBufferSubData(GL_ARRAY_BUFFER, 0, typedArrayBuffer);
            glDrawArrays(GL_TRIANGLES, 0, typedArrayBuffer.length);
            BUTTONS.forEach(Button::render);
        }
    }
    public void click(double x, double y, int action) {
        for (T button : BUTTONS) {
            button.click(x, y, action);
        }
    }

    public void hover(double xpos, double ypos) {
        for (T button : BUTTONS) {
            button.hover(xpos, ypos);
        }
    }
}