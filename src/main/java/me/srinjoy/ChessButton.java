package me.srinjoy;

import me.srinjoy.graphics.Window;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@SuppressWarnings("unused")
public class ChessButton extends Button<ChessButton> {
    float[] colourCopy;
    boolean isTarget = false;
    private final ArrayList<ChessButton> TARGETS = new ArrayList<>();
    public final int INDEX = (int) y / 50 * 8 + (int) x / 50;
    char piece;
    public ChessButton(float x, float y, int width, int height, float[] colour, IButtonCallback<ChessButton> callback, char piece) {
        super(x, y, width, height, colour, callback);
        colourCopy = colour;
        this.piece = piece;
    }

    @Override
    public List<Float> getVertices() {
        var targetSlot = 0f;
        if (isTarget) {
            var texture = Window.TEXTURES.get("target");
            texture.bind();
            targetSlot = texture.SLOT;
        }
        var pieceSlot = 0f;
        if (piece != 0) {
            var texture = Window.TEXTURES.get(Window.PIECES_MAP.get(piece));
            texture.bind();
            pieceSlot = texture.SLOT;
        }
        return List.of(
                x, y,                       colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x + width), y,             colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x + width), (y + height),  colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                (x + width), (y + height),  colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                x, (y + height),            colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,
                x, y,                       colour[0], colour[1], colour[2], 1.0f,    0f, 0f,    0f,

                x, y,                         0f, 0f, 0f, 0f,       0.0f, 0.0f,   targetSlot,
                (x + width), y,               0f, 0f, 0f, 0f,       1.0f, 0.0f,   targetSlot,
                (x + width), (y + height),    0f, 0f, 0f, 0f,       1.0f, 1.0f,   targetSlot,
                (x + width), (y + height),    0f, 0f, 0f, 0f,       1.0f, 1.0f,   targetSlot,
                x, (y + height),              0f, 0f, 0f, 0f,       0.0f, 1.0f,   targetSlot,
                x, y,                         0f, 0f, 0f, 0f,       0.0f, 0.0f,   targetSlot,

                x, y,                         0f, 0f, 0f, 0f,       0.0f, 0.0f,   pieceSlot,
                (x + width), y,               0f, 0f, 0f, 0f,       1.0f, 0.0f,   pieceSlot,
                (x + width), (y + height),    0f, 0f, 0f, 0f,       1.0f, 1.0f,   pieceSlot,
                (x + width), (y + height),    0f, 0f, 0f, 0f,       1.0f, 1.0f,   pieceSlot,
                x, (y + height),              0f, 0f, 0f, 0f,       0.0f, 1.0f,   pieceSlot,
                x, y,                         0f, 0f, 0f, 0f,       0.0f, 0.0f,   pieceSlot
        );
    }

    @Override
    public void click(double x, double y, int action) {
        if (hover(x, y) && action == GLFW_PRESS) callback.invoke(this);
    }

    boolean hasTarget(ChessButton button) {
        return TARGETS.contains(button);
    }
    void addTarget(ChessButton button) {
        button.isTarget = true;
        TARGETS.add(button);
    }
    void clearTargets() {
        for (ChessButton target : TARGETS) {
            target.isTarget = false;
        }
        TARGETS.clear();
    }
}
