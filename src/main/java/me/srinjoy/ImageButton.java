package me.srinjoy;

import me.srinjoy.graphics.Texture;
import me.srinjoy.graphics.Window;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class ImageButton extends Button<ImageButton> {
    String texture;
    boolean isHovering = false;
    public ImageButton(float x, float y, int width, int height, float[] colour, IButtonCallback<ImageButton> callback, String texture) {
        super(x, y, width, height, colour, callback);
        this.texture = texture;
    }

    @Override
    public List<Float> getVertices() {
        float textureSlot;
        if ((!texture.isEmpty())) {
            Texture tex = Window.TEXTURES.get(texture);
            tex.bind();
            textureSlot = tex.SLOT * 1f;
        } else {
            textureSlot = 0f;
        }
        float x_cpy = x + 2, y_cpy = y + 2, w_cpy = width - 4, h_cpy = height - 4;
        float hoverColour = isHovering? 1: 0;
        return List.of(
                x, y,                       hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                (x + width), y,             hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                (x + width), (y + height),  hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                (x + width), (y + height),  hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                x, (y + height),            hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,
                x, y,                       hoverColour, hoverColour, hoverColour, 1.0f,    0f, 0f,    0f,

                x_cpy, y_cpy,                       colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x_cpy + w_cpy), y_cpy,             colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x_cpy + w_cpy), (y_cpy + h_cpy),   colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x_cpy + w_cpy), (y_cpy + h_cpy),   colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                x_cpy, (y_cpy + h_cpy),             colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                x_cpy, y_cpy,                       colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,

                x_cpy, y_cpy,                       0f, 0f, 0f, 0f,     0f, 0f,    textureSlot,
                (x_cpy + w_cpy), y_cpy,             0f, 0f, 0f, 0f,     1f, 0f,    textureSlot,
                (x_cpy + w_cpy), (y_cpy + h_cpy),   0f, 0f, 0f, 0f,     1f, 1f,    textureSlot,
                (x_cpy + w_cpy), (y_cpy + h_cpy),   0f, 0f, 0f, 0f,     1f, 1f,    textureSlot,
                x_cpy, (y_cpy + h_cpy),             0f, 0f, 0f, 0f,     0f, 1f,    textureSlot,
                x_cpy, y_cpy,                       0f, 0f, 0f, 0f,     0f, 0f,    textureSlot
        );
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

    @Override
    boolean hover(double x, double y) {
        return isHovering = super.hover(x, y);
    }
}
