package com.togetherjava.tjplays.games.game2048;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public final class Game2048 {
    public static final int ROWS = 4;
    public static final int COLUMNS = 4;
    private static final Random random = new Random();

    private int[][] board;
    private GameState state;
    private int score;

    public Game2048() {
        board = new int[ROWS][COLUMNS];
        state = GameState.ONGOING;
        score = 0;

        for (int i = 0; i < 2; i++) spawnNewBlock();
    }

    public int getTileAt(int i, int j) {
        return board[i][j];
    }

    public GameState getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public void move(Move move) {
        if (state != GameState.ONGOING) return;

        BiFunction<Integer, Integer, Boolean> moveHandler = switch (move) {
            case UP -> this::moveUp;
            case DOWN -> this::moveDown;
            case LEFT -> this::moveLeft;
            case RIGHT -> this::moveRight;
            default -> throw new IllegalArgumentException("Unexpected move " + move);
        };

        boolean valid = false;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (board[i][j] == 0) continue;
                if (moveHandler.apply(i, j)) valid = true;
            }
        }

        if (valid) spawnNewBlock();
        reloadGameState();
    }

    private boolean moveUp(int i, int j) {
        boolean valid = false;

        for (int k = i - 1; k >= 0; k--) {
            if (board[k][j] != 0 && board[k][j] != board[k + 1][j]) break;
            valid = true;

            int[] a = new int[]{ k, j };
            int[] b = new int[]{ k + 1, j };
            mergeOrSwap(a, b);
        }

        return valid;
    }

    private boolean moveDown(int i, int j) {
        boolean valid = false;

        for (int k = i + 1; k < ROWS; k++) {
            if (board[k][j] != 0 && board[k][j] != board[k - 1][j]) break;
            valid = true;

            int[] a = new int[]{ k, j };
            int[] b = new int[]{ k - 1, j };
            mergeOrSwap(a, b);
        }
        if (i > 0 && board[i - 1][j] != 0)
            for (int l = i - 1; l >= 0; l--) moveDown(l, j);

        return valid;
    }

    private boolean moveLeft(int i, int j) {
        boolean valid = false;

        for (int k = j - 1; k >= 0; k--) {
            if (board[i][k] != 0 && board[i][k] != board[i][k + 1]) break;
            valid = true;

            int[] a = new int[]{ i, k };
            int[] b = new int[]{ i, k + 1 };
            mergeOrSwap(a, b);
        }

        return valid;
    }

    private boolean moveRight(int i, int j) {
        boolean valid = false;

        for (int k = j + 1; k < COLUMNS; k++) {
            if (board[i][k] != 0 && board[i][k] != board[i][k - 1]) break;
            valid = true;

            int[] a = new int[]{ i, k };
            int[] b = new int[]{ i, k - 1 };
            mergeOrSwap(a, b);
        }

        if (j > 0 && board[i][j - 1] != 0)
            for (int l = j - 1; l >= 0; l--) moveRight(i, l);

        return valid;
    }

    private void mergeOrSwap(int[] a, int[] b) {
        if (board[a[0]][a[1]] == board[b[0]][b[1]]) mergeBlocks(a, b);
        else swapBlocks(a, b);
    }

    private void mergeBlocks(int[] a, int[] b) {
        board[a[0]][a[1]] *= 2;
        board[b[0]][b[1]] = 0;
        score += board[a[0]][a[1]];
    }

    private void swapBlocks(int[] a, int[] b) {
        board[a[0]][a[1]] = board[b[0]][b[1]];
        board[b[0]][b[1]] = 0;
    }   

    private void spawnNewBlock() {
        List<int[]> emptyBlocks = new ArrayList<>();

        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLUMNS; j++)
                if (board[i][j] == 0) emptyBlocks.add(new int[]{ i, j });

        if (emptyBlocks.size() <= 0) return;

        int[] emptyBlock = emptyBlocks.get(random.nextInt(emptyBlocks.size()));
        board[emptyBlock[0]][emptyBlock[1]] = new int[]{ 2, 4 }[random.nextInt(2)];
    }

    private void reloadGameState() {
        boolean hasEmptyBlocks = false;
        boolean hasPossibleMove = false;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (board[i][j] == 2048) {
                    state = GameState.WON;
                    return;
                }

                if (board[i][j] == 0) hasEmptyBlocks = true;

                if (!hasEmptyBlocks && !hasPossibleMove) {
                    if (i > 0 && board[i][j] == board[i - 1][j]) hasPossibleMove = true;
                    if (i < ROWS - 1 && board[i][j] == board[i + 1][j]) hasPossibleMove = true;
                    if (j > 0 && board[i][j] == board[i][j - 1]) hasPossibleMove = true;
                    if (j < COLUMNS - 1 && board[i][j] == board[i][j + 1]) hasPossibleMove = true;
                }
            }
        }

        if (!hasPossibleMove && !hasEmptyBlocks) state = GameState.LOST;
    }
}
