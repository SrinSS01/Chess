package me.srinjoy.chess_engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.*;

@SuppressWarnings("unused")
public class Chess {
    char turn;
    String castle;
    final ArrayList<Integer> PATH = new ArrayList<>();
    final char[] BOARD = new char[64];
    final int[] OFFSETS = { 8, -8, 1, -1, 7, -7, 9, -9 };
    final int[][] KNIGHT_OFFSETS = { { 2, -1 }, { -2, 1 }, { -1, -2 }, { 1, 2 }, { 2, 1 }, { -2, -1 }, { 1, -2 }, { -1, 2 } };
    final int[][] KING_OFFSETS = { { 1, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { -1, 1 }, { 1, 1 }, { -1, -1 } };
    final int[][] numberOfSquaresToEdgeCache = new int[64][8];
    int en_passant;
    public Chess(String fen, char turn, String castle, String en_passant) {
        this.turn = turn;
        this.castle = castle;
        this.en_passant = cellToIndex(en_passant);
        int file = 0, rank = 7;
        for (var it : fen.toCharArray()) {
            if (it == '/') { rank--; file = 0; }
            else if (Character.isDigit(it)) {
                file += it - '0';
            } else {
                BOARD[rank * 8 + file] = it;
                file++;
            }
        }
        for (int i = 0; i < 64; i++) {
            int r = i >>> 3;
            int f = i & 7;
            int north = 7 - r;
            int east = 7 - f;
            numberOfSquaresToEdgeCache[i] = new int[] {
                north, r, east, f, min(north, f), min(r, east), min(north, east), min(r, f)
            };
        }
    }
    int cellToIndex(String cell) {
        if (cell.equals("-")) return  -1;
        int rank = cell.charAt(1) - '0' - 1;
        int file = cell.charAt(0) - 'a';
        return rank * 8 + file;
    }
    boolean isSlidingPiece(char piece) {
        piece = Character.toUpperCase(piece);
        return piece == 'R' || piece == 'B' || piece == 'Q';
    }
    public String getFen() {
        final var fen = new StringBuilder();
        for (int i = 7; i >= 0; --i) {
            int blank = 0;
            for (int j = 0; j < 8; ++j) {
                var piece = BOARD[i * 8 + j];
                if (piece != 0) {
                    if (blank != 0) fen.append(blank);
                    fen.append(piece);
                    blank = 0;
                } else if (j + 1 == 8 && blank != 0)
                    fen.append(blank + 1);
                else blank++;
            } fen.append(i == 0? " ": "/");
        } fen.append(turn).append(" - ");
        if (en_passant > 0) {
            var rank = en_passant >>> 3;
            var file = en_passant & 7;
            fen.append((char) ('a' + file)).append((char) (rank + '1'));
        } else fen.append("-");
        return fen.toString();
    }
    public char get(int index) {
        return BOARD[index];
    }
    List<Integer> get_pawn_piece_moves(int cell, char piece) {
        ArrayList<Integer> moves = new ArrayList<>();
        var rank = cell >>> 3;
        var file = cell & 7;
        int[] directions = { piece == 'P'? 4: 5, piece == 'P'? 6: 7 };
        int n = (piece == 'p' && 7 - rank == 1) || (piece == 'P' && rank == 1)? 2: 1;
        for (int i = 1; i <= n; ++i) {
            int target_cell = cell + OFFSETS[piece == 'P'? 0: 1] * i;
            if (BOARD[target_cell] != 0) break;
            moves.add(target_cell);
        }
        for (final var direction : directions) {
            int target_cell = cell + OFFSETS[direction];
            char piece_at_target_cell = BOARD[target_cell];
            if ((piece_at_target_cell != 0 && !is_friendly_piece(piece, piece_at_target_cell)) || target_cell == en_passant)
                moves.add(target_cell | 64);
        }
        return moves;
    }
    List<Integer> get_non_sliding_piece_moves(int cell, char piece) {
        ArrayList<Integer> moves = new ArrayList<>();
        var rank = cell >>> 3;
        var file = cell & 7;
        var is_knight = piece == 'n' || piece == 'N';
        for (int direction = 0; direction < 8; ++direction) {
            int target_cell;
            var offset = is_knight? KNIGHT_OFFSETS[direction]: KING_OFFSETS[direction];
            int r = rank + offset[0];
            int f = file + offset[1];
            if (r < 0 || r > 7 || f < 0 || f > 7) continue;
            target_cell = r * 8 + f;
            char piece_at_target_cell = BOARD[target_cell];
            if (piece_at_target_cell != 0 && is_friendly_piece(piece, piece_at_target_cell)) continue;
            if (piece_at_target_cell != 0 && !is_friendly_piece(piece, piece_at_target_cell)) {
                moves.add(target_cell | 64);
                continue;
            }
            moves.add(target_cell);
        }
        return moves;
    }
    List<Integer> get_sliding_piece_moves(int cell, char piece) {
        int start_direction_index = piece == 'b' || piece == 'B'? 4: 0;
        int end_direction_index = piece == 'r' || piece == 'R'? 4: 8;
        ArrayList<Integer> moves = new ArrayList<>();
        for (int direction = start_direction_index; direction < end_direction_index; ++direction) {
            for (int number_of_squares = 0; number_of_squares < numberOfSquaresToEdgeCache[cell][direction]; ++number_of_squares) {
                int target_cell = cell + OFFSETS[direction] * (number_of_squares + 1);
                char piece_at_target_cell = BOARD[target_cell];
                if (piece_at_target_cell != 0 && is_friendly_piece(piece, piece_at_target_cell)) break;
                else if (piece_at_target_cell != 0 && !is_friendly_piece(piece, piece_at_target_cell)) {
                    moves.add(target_cell | 64);
                    break;
                }
                else moves.add(target_cell);
            }
        }
        return moves;
    }
    public List<Integer> show(char player, int start_index) {
        List<Integer> moves = List.of();
        if (player != turn) return moves;
        char start_piece = BOARD[start_index];
        if ((turn == 'w' && !Character.isUpperCase(start_piece)) || (turn == 'b' && Character.isUpperCase(start_piece))) return moves;
        if (start_piece == 0) return moves;
        if (isSlidingPiece(start_piece)) {
            moves = get_sliding_piece_moves(start_index, start_piece);
        } else if (start_piece == 'p' || start_piece == 'P') {
            moves = get_pawn_piece_moves(start_index, start_piece);
        } else {
            moves = get_non_sliding_piece_moves(start_index, start_piece);
        }
        PATH.clear();
        PATH.addAll(moves);
        return moves;
    }
    public boolean move(int start_index, int end_index) {
        var moves = new ArrayList<>(PATH);
        PATH.clear();
        if (moves.isEmpty()) return false;
        char start_piece = BOARD[start_index];
        char end_piece = BOARD[end_index];
        if (end_piece != 0 && is_friendly_piece(start_piece, end_piece)) return false;
        if (start_piece == 'p' || start_piece == 'P') {
            int start_rank = start_index >>> 3;
            int end_rank = end_index >>> 3;
            if (abs(end_rank - start_rank) == 2) {
                en_passant = start_index + OFFSETS[start_piece == 'P'? 0: 1];
            } else {
                if (end_index == en_passant) {
                    int offset = OFFSETS[start_piece == 'P'? 1: 0];
                    System.out.printf("en_passant_move, piece at %d = %c\n", end_index + offset, BOARD[end_index + offset]);
                    BOARD[end_index + offset] = 0;
                    System.out.printf("and now it is %c\n", BOARD[end_index + offset]);
                }
                en_passant = -1;
            }
        } else en_passant = -1;
        if (!moves.stream().map(item -> {
            if (item >>> 6 != 0) {
                return item & 63;
            } else return item;
        }).collect(Collectors.toList()).contains(end_index)) return false;
        BOARD[end_index] = start_piece;
        BOARD[start_index] = 0;
        turn = turn == 'w'? 'b': 'w';
        return true;
    }

    public int getEn_passant() {
        return en_passant;
    }

    public static boolean is_friendly_piece(char piece1, char piece2) {
        return (piece1 != 'd' && piece2 != 'd') && (!Character.isUpperCase(piece1) && !Character.isUpperCase(piece2)) || (Character.isUpperCase(piece1) && Character.isUpperCase(piece2));
    }
}
