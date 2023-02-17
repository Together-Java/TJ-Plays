package com.togetherjava.tjplays.games.gamesnake;

import com.togetherjava.tjplays.utils.CardinalDirection;
import com.togetherjava.tjplays.utils.GifSequenceWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GameSnake {
    private enum RunningState {
        RUNNING,
        LOST,
        WON
    }

    private record Pos(int x, int y) {
    }

    private record State(List<Pos> snake, Pos apple, RunningState runningState) {
        public State(List<Pos> snake, Pos apple) {
            this(snake, apple, RunningState.RUNNING);
        }
    }

    private static final int WIDTH = 25;
    private static final int HEIGHT = 15;
    private static final int TILE_SIZE = 10;
    public static final String IMAGE_FORMAT = "gif";
    private static final int TURN_PERIOD_MILLIS = 500;
    private State state;
    private CardinalDirection currentDirection = null;
    private int randomAppleCacheId = -1;
    private Pos randomAppleCache;
    private long timeMillis;

    public GameSnake(Instant eventTimeCreated) {
        Pos apple = new Pos(ThreadLocalRandom.current().nextInt(WIDTH),
                ThreadLocalRandom.current().nextInt(HEIGHT));
        Pos head = new Pos(ThreadLocalRandom.current().nextInt(WIDTH / 4, WIDTH / 4 * 3),
                ThreadLocalRandom.current().nextInt(HEIGHT / 4, HEIGHT / 4 * 3));
        state = new State(List.of(head), apple);
        currentDirection = CardinalDirection.values()[ThreadLocalRandom.current().nextInt(CardinalDirection.values().length)];
        timeMillis = eventTimeCreated.toEpochMilli();
    }

    public void onNewDirectionAction(Instant actionTime, CardinalDirection newDirection) {
        long now = actionTime.toEpochMilli();
        long turns = (now - timeMillis) / TURN_PERIOD_MILLIS;
        timeMillis = now;
        state = playSeveralTurns(state, (int) turns).skip(turns - 1)
                .findFirst()
                .orElseGet(() -> playSeveralTurns(state, (int) turns)
                        .dropWhile(s -> s.runningState() == RunningState.RUNNING)
                        .findFirst()
                        .orElseThrow());
        currentDirection = newDirection;
    }

    public byte[] generateCurrentAnimationBuffer() {
        return generateAnimationBuffer(state);
    }

    private byte[] generateAnimationBuffer(State state) {
        List<BufferedImage> images = renderSeveralTurns(state, Math.max(WIDTH, HEIGHT)).toList();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(32768);
        try (var imageStream = ImageIO.createImageOutputStream(baos);
             GifSequenceWriter writer = new GifSequenceWriter(imageStream,
                     images.get(0).getType(), TURN_PERIOD_MILLIS, false)) {
            for (BufferedImage img : images) {
                writer.writeToSequence(img);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    private State next(State state) {
        List<Pos> snake = state.snake();
        Pos head = snake.get(0);
        Pos newPos = nextPos(head);
        if (newPos.x() < 0 || newPos.x() >= WIDTH || newPos.y() < 0 || newPos.y() >= HEIGHT) {
            return new State(state.snake(), state.apple(), RunningState.LOST);
        }
        if (snake.subList(1, snake.size()).contains(head)) {
            return new State(state.snake(), state.apple(), RunningState.LOST);
        }
        if (newPos.equals(state.apple())) {
            List<Pos> newSnake = Stream.concat(Stream.of(newPos), snake.stream()).toList();
            if (newSnake.size() == WIDTH * HEIGHT) {
                return new State(state.snake(), state.apple(), RunningState.WON);
            }
            if (newSnake.size() > WIDTH * HEIGHT)
                throw new AssertionError();
            return generateNewApple(new State(newSnake, state.apple));
        } else {
            List<Pos> newSnake =
                    Stream.concat(Stream.of(newPos), snake.subList(0, snake.size() - 1).stream())
                            .toList();
            return new State(newSnake, state.apple());
        }
    }

    private Pos nextPos(Pos pos) {
        return switch (currentDirection) {
            case LEFT -> new Pos(pos.x() - 1, pos.y());
            case RIGHT -> new Pos(pos.x() + 1, pos.y());
            case UP -> new Pos(pos.x(), pos.y() - 1);
            case DOWN -> new Pos(pos.x(), pos.y() + 1);
        };
    }

    private Stream<State> playSeveralTurns(State state, int turns) {
        List<State> buffer = new ArrayList<>();
        State current = state;
        buffer.add(current);
        for (int i = 0; i < turns && current.runningState() == RunningState.RUNNING; i++) {
            current = next(current);
            buffer.add(current);
        }
        return buffer.stream();
    }

    private State generateNewApple(State state) {
        List<Pos> snake = state.snake();
        if (randomAppleCacheId == snake.size()) {
            return new State(snake, randomAppleCache);
        }
        List<Pos> pos = IntStream.range(0, HEIGHT)
                .mapToObj(y -> IntStream.range(0, WIDTH).mapToObj(x -> new Pos(x, y)))
                .flatMap(s -> s)
                .filter(p -> !snake.contains(p))
                .toList();
        int i = ThreadLocalRandom.current().nextInt(pos.size());
        randomAppleCacheId = snake.size();
        randomAppleCache = pos.get(i);
        return new State(snake, randomAppleCache);
    }

    private Stream<BufferedImage> renderSeveralTurns(State state, int turns) {
        return playSeveralTurns(state, turns).map(this::render);
    }

    private BufferedImage render(State state) {
        List<Pos> snake = state.snake();
        BufferedImage img = new BufferedImage(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();


        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Pos current = new Pos(x, y);

                Color color;
                if (current.equals(state.apple())) {
                    color = Color.RED;
                } else if (current.equals(snake.get(0))) {
                    color = Color.BLACK;
                } else if (snake.contains(current)) {
                    color = Color.GRAY;
                } else {
                    color = Color.WHITE;
                }
                g.setColor(color);

                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        if (state.runningState() == RunningState.WON) {
            g.setColor(Color.GREEN);
            drawCenteredString(g, "You won !", WIDTH * TILE_SIZE / 2F, HEIGHT * TILE_SIZE / 2F);
        } else if (state.runningState() == RunningState.LOST) {
            g.setColor(Color.RED);
            drawCenteredString(g, "You lost...", WIDTH * TILE_SIZE / 2F, HEIGHT * TILE_SIZE / 2F);
        }
        return img;
    }

    private void drawCenteredString(Graphics2D g, String string, float x, float y) {
        FontMetrics metrics = g.getFontMetrics();
        x -= metrics.stringWidth(string) / 2F;
        y += metrics.getHeight() / 2F + metrics.getAscent();
        g.drawString(string, x, y);
    }
}
