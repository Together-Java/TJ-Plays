package com.togetherjava.tjplays.games.game2048;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public final class Game2048 {
    private static final int ROWS = 4;
    private static final int COLUMNS = 4;
    private static Random random = new Random();
    private int[][] board = new int[ROWS][COLUMNS];
    private GameState state = GameState.ONGOING;
    private int score = 0;

    public Game2048() {
        for (int i = 0; i < 2; i++) spawnNewBlock();
    }

    public int[][] getBoard() {
        return board;
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
                    if (i > 0) hasPossibleMove = board[i][j] == board[i - 1][j];
                    if (i < ROWS - 1) hasPossibleMove = board[i][j] == board[i + 1][j];
                    if (j > 0) hasPossibleMove = board[i][j] == board[i][j - 1];
                    if (j < COLUMNS - 1) hasPossibleMove = board[i][j] == board[i][j + 1];
                }
            }
        }

        if (!hasPossibleMove && !hasEmptyBlocks) state = GameState.LOST;
    }
}
