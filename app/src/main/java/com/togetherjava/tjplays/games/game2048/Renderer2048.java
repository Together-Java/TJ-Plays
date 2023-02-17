package com.togetherjava.tjplays.games.game2048;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public final class Renderer2048 {
    public static final String IMAGE_FORMAT = "png";

    private static final int TILE_SIDE = 200;
    private static final int PADDING = 20;
    private static Map<Integer, Color> tileColorMap = new HashMap<>();

    private final Game2048 game;
    private final BufferedImage image;

    static {
        tileColorMap.put(0, new Color(205, 190, 180));
        tileColorMap.put(2, new Color(240, 230, 220));
        tileColorMap.put(4, new Color(240, 225, 200));
        tileColorMap.put(8, new Color(240, 180, 120));
        tileColorMap.put(16, new Color(250, 150, 100));
        tileColorMap.put(32, new Color(250, 125, 95));
        tileColorMap.put(64, new Color(250, 95, 60));
        tileColorMap.put(128, new Color(240, 210, 115));
        tileColorMap.put(256, new Color(240, 205, 100));
        tileColorMap.put(512, new Color(235, 200, 90));
        tileColorMap.put(1024, new Color(230, 195, 90));
        tileColorMap.put(2048, new Color(230, 190, 80));
    }

    public Renderer2048(Game2048 game) {
        this.game = game;
        this.image = new BufferedImage(
                TILE_SIDE * Game2048.ROWS + PADDING * (Game2048.ROWS + 1),
                TILE_SIDE * Game2048.COLUMNS + PADDING * (Game2048.COLUMNS + 1),
                BufferedImage.TYPE_INT_RGB
            );
    }

    public Game2048 getGame() {
        return game;
    }

    public byte[] getData() {
        Graphics2D graphics = image.createGraphics();

        // Add the background
        graphics.setColor(new Color(190, 170, 160));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        // Add the tiles
        for (int i = 0; i < Game2048.ROWS; i++) {
            for (int j = 0; j < Game2048.COLUMNS; j++) {
                int tileValue = game.getTileAt(i, j);
                int x = (j + 1) * PADDING + j * TILE_SIDE;
                int y = (i + 1) * PADDING + i * TILE_SIDE;

                graphics.setColor(tileColorMap.get(tileValue));
                graphics.fillRect(x, y, TILE_SIDE, TILE_SIDE);

                if (tileValue == 0) continue;
                graphics.setFont(new Font("Arial", Font.BOLD, 60));
                graphics.setColor(Color.BLACK);
                graphics.drawString(String.valueOf(tileValue), x, y + TILE_SIDE);
            }
        }

        return getByteArray();
    }

    private byte[] getByteArray() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            return new byte[]{ -1 };
        }
    }
}
