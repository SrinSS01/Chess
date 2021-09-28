package me.srinjoy;

import me.srinjoy.graphics.Window;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@SuppressWarnings("unused")
public class TextButton extends Button<TextButton> {
    String text;
    Font font;
    boolean isHovering = false;
    Window.Pair<Float, Float> font_width_height;
    public TextButton(float x, float y, int width, int height, float[] colour, IButtonCallback<TextButton> callback, String text, Font font) {
        super(x, y, width, height, colour, callback);
        this.text = text;
        this.font = font;
        font_width_height = font.get_width_and_height(text, 0.5f);
    }
    void setText(Font font, String text) {
        this.text = text;
        this.font = font;
        font_width_height = font.get_width_and_height(text, 0.5f);
    }
    @Override
    public List<Float> getVertices() {
        float x_cpy = x + 2, y_cpy = y + 2, w_cpy = width - 4, h_cpy = height - 4;
        float hoverColour = isHovering? 1: 0;
        return List.of(
                x, y,                       hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                (x + width), y,             hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                (x + width), (y + height),  hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                (x + width), (y + height),  hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                x, (y + height),            hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                x, y,                       hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,

                (x + 1.5f), (y + 1.5f),                             1f, 1f, 1f, 1f,    0f, 0f,    0f,
                (x + 1.5f + width - 2), (y + 1.5f),                 1f, 1f, 1f, 1f,    0f, 0f,    0f,
                (x + 1.5f + width - 2), (y + 1.5f + height - 2),    1f, 1f, 1f, 1f,    0f, 0f,    0f,
                (x + 1.5f + width - 2), (y + 1.5f + height - 2),    1f, 1f, 1f, 1f,    0f, 0f,    0f,
                (x + 1.5f), (y + 1.5f + height - 2),                1f, 1f, 1f, 1f,    0f, 0f,    0f,
                (x + 1.5f), (y + 1.5f),                             1f, 1f, 1f, 1f,    0f, 0f,    0f,

                x_cpy, y_cpy,                       colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x_cpy + w_cpy), y_cpy,             colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x_cpy + w_cpy), (y_cpy + h_cpy),   colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x_cpy + w_cpy), (y_cpy + h_cpy),   colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                x_cpy, (y_cpy + h_cpy),             colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                x_cpy, y_cpy,                       colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f
        );
    }

    @Override
    void render() {
        if (!text.isEmpty()) {
            font.render_text(text, x + (width - font_width_height.first()) / 2, y + (height - font_width_height.second()) / 2, 0.5f, new Vector3f(1, 1, 1));
        }
    }

    @Override
    boolean hover(double x, double y) {
        return isHovering = super.hover(x, y);
    }
    @Override
    public void click(double x, double y, int action) {
        if (hover(x, y)) {
            if (action == GLFW_PRESS) {
                callback.invoke(this);
                isHovering = false;
            } else isHovering = true;
        }
    }
}
