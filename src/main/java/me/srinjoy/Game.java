package me.srinjoy;

import me.srinjoy.chess_engine.Chess;
import me.srinjoy.graphics.Window;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class Game {
    Chess chess = new Chess("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", 'w', "-", "-");
    Window window = new Window(600, 400, "Chess");
    static final float[] BLACK = normalise(119.0f, 149.0f, 86.0f);
    static final float[] WHITE = normalise(238.0f, 238.0f, 210.0f);
    static final float[] YELLOW = normalise(246f, 246f, 105f);
    static final float[] ORANGE = normalise(255f, 165f, 0f);
    static final float[] BLUE = normalise(88f, 101f, 242f);
    static final float[] NULL = { 0f, 0f, 0f };
    final Box<ChessButton> BOARD = new Box<>(0, 0, true);
    final ArrayList<Integer> PATH = new ArrayList<>();
    final ArrayList<ChessButton> SELECTED_PIECES = new ArrayList<>();
    char turn = 'w';

    public Game() throws IOException {
        Window.loadTextures(1, "r", "download");
        Window.loadTextures(2, "n");
        Window.loadTextures(3, "b");
        Window.loadTextures(4, "q");
        Window.loadTextures(5, "k");
        Window.loadTextures(6, "R_");
        Window.loadTextures(7, "N_");
        Window.loadTextures(8, "B_");
        Window.loadTextures(9, "Q_");
        Window.loadTextures(10, "K_");
        Window.loadTextures(11, "p");
        Window.loadTextures(12, "P_");
        Window.loadTextures(13, "dot");
        Window.loadTextures(14, "target");
        for (int i = 0; i < 64; i++) {
            var rank = i >>> 3;
            var file = i & 7;
            BOARD.addButton(new ChessButton(file * 50f, rank * 50f, 50, 50, (rank + file) % 2 == 0? BLACK: WHITE, current -> {
                var prev = BOARD.previousButton;
                var current_index = current.INDEX;
                if (prev != null && prev.piece != 0 && (!Chess.is_friendly_piece(prev.piece, current.piece) || prev.hasTarget(current))) {
                    hide(prev);
                    var was_en_passant = current_index == chess.getEn_passant();
                    if (chess.move(prev.INDEX, current_index)) {
                        if (was_en_passant) {
                            int offset = prev.piece == 'P' ? -1 : +1;
                            var button = BOARD.get((rank + offset) * 8 + file);
                            button.piece = 0;
                        }
                        set_chess_piece_from(prev, current);
                    } else unselect();
                    BOARD.previousButton = null;
                } else {
                    if (prev != null) hide(prev);
                    unselect();
                    var p = chess.show(turn, current_index);
                    if (!p.isEmpty()) {
                        PATH.addAll(p);
                        show(current);
                    }
                    if (current.piece != 0) {
                        current.colour = YELLOW;
                        SELECTED_PIECES.add(current);
                    }
                    BOARD.previousButton = current;
                }
            }, chess.get(i)));
        }
        var fontBufferIS = Main.class.getResourceAsStream("fonts/RobotoMono-Light.ttf");

        var download_button_box = new Box<ImageButton>(405 + 200 - 10 - 30, 10, true);
        download_button_box.addButton(new ImageButton(0, 0, 30, 30, NULL, button -> System.out.println(chess.getFen()), "download"));
        window.addBox(download_button_box);

        if (fontBufferIS != null) {
            var fontBuffer = fontBufferIS.readAllBytes();
            var reset_button_box = new Box<TextButton>(405, 10, true);
            reset_button_box.addButton(new TextButton(0, 0, 200 - 15 - 30, 30, NULL, button -> reset(), "reset", new Font(window.FONT_SHADER, fontBuffer)));
            window.addBox(reset_button_box);
        } else System.err.println("unable to find fonts/RobotoMono-Light.ttf");
        window.addBox(BOARD);
    }

    private void unselect() {
        if (!SELECTED_PIECES.isEmpty()) {
            for (var button : SELECTED_PIECES) {
                button.colour = button.colourCopy;
            }
            SELECTED_PIECES.clear();
        }
    }
    void reset() {
        System.out.println("resetting...");
        BOARD.previousButton = null;
        turn = 'w';
        unselect();
        PATH.clear();
        chess = new Chess("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", 'w', "-", "-");
        for (int i = 0; i < 64; i++) {
            int rank = i >>> 3;
            int file = i & 7;
            var piece = BOARD.get(rank * 8 + file);
            piece.piece = chess.get(rank * 8 + file);
            piece.clearTargets();
        }
    }

    private void set_chess_piece_from(ChessButton from, ChessButton to) {
        turn = turn == 'w'? 'b': 'w';
        to.piece = from.piece;
        from.piece = 0;
        SELECTED_PIECES.get(0).colour = ORANGE;
        to.colour = YELLOW;
        SELECTED_PIECES.add(to);
    }
    void hide(ChessButton button) {
        button.clearTargets();
        for (var it : PATH) {
            if (it >>> 6 != 1) BOARD.get(it).piece = 0;
        }
        PATH.clear();
    }
    void show(ChessButton button) {
        for (var it: PATH) {
            ChessButton piece;
            if (it >>> 6 != 0) {
                piece = BOARD.get(it & 63);
                button.addTarget(piece);
            } else {
                piece = BOARD.get(it);
                piece.piece = 'd';
            }
        }
    }

    public void launch() {
        while (!window.shouldClose()) window.render();
        window.delete();
    }
    static float[] normalise(float... colour) {
        colour[0] /= 225.0f;
        colour[1] /= 225.0f;
        colour[2] /= 225.0f;
        return colour;
    }
}
