package com.togetherjava.tjplays.listeners.commands;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.togetherjava.tjplays.games.game2048.Game2048;
import com.togetherjava.tjplays.games.game2048.GameState;
import com.togetherjava.tjplays.games.game2048.Move;
import com.togetherjava.tjplays.games.game2048.Renderer2048;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public final class Game2048Command extends SlashCommand {
    private static final String COMMAND_NAME = "2048";
    private static final Emoji RESET_EMOJI = Emoji.fromUnicode("🔃");
    private static final Emoji UP_EMOJI = Emoji.fromUnicode("⬆️");
    private static final Emoji DELETE_EMOJI = Emoji.fromUnicode("🗑️");
    private static final Emoji LEFT_EMOJI = Emoji.fromUnicode("⬅️");
    private static final Emoji DOWN_EMOJI = Emoji.fromUnicode("⬇️");
    private static final Emoji RIGHT_EMOJI = Emoji.fromUnicode("➡️");

    private final Cache<String, Renderer2048> sessionsCache;

    public Game2048Command() {
        super(Commands.slash(COMMAND_NAME, "Game 2048"));

        sessionsCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        Renderer2048 gameRenderer = new Renderer2048(new Game2048());

        event.reply(gameMessage(gameRenderer, event.getUser().getId()))
            .flatMap(hook -> hook.retrieveOriginal())
            .onSuccess(message -> sessionsCache.put(message.getId(), gameRenderer))
            .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (!buttonId.startsWith(COMMAND_NAME)) return;

        if (!buttonId.contains(event.getUser().getId())) {
            event.reply("You can't interact with this game.").setEphemeral(true).queue();
            return;
        }

        if (buttonId.contains("delete")) {
            sessionsCache.invalidate(event.getMessageId());
            event.deferEdit().queue();
            event.getHook().deleteOriginal().queue();
            return;
        }

        Renderer2048 gameRenderer = sessionsCache.getIfPresent(event.getMessageId());

        if (buttonId.contains("reset")) {
            gameRenderer.setGame(new Game2048());
        } else {
            Move move = null;

            if (buttonId.contains("up")) move = Move.UP;
            else if (buttonId.contains("down")) move = Move.DOWN;
            else if (buttonId.contains("left")) move = Move.LEFT;
            else if (buttonId.contains("right")) move = Move.RIGHT;

            gameRenderer.getGame().move(move);
        }

        event.editMessage(MessageEditData.fromCreateData(gameMessage(gameRenderer, event.getUser().getId()))).queue();
    }

    private MessageCreateData gameMessage(Renderer2048 gameRenderer, String playerId) {
        Button resetButton = Button.success(COMMAND_NAME + " " + playerId + " reset", RESET_EMOJI);
        Button upButton = Button.primary(COMMAND_NAME + " " + playerId + " up", UP_EMOJI);
        Button deleteButton = Button.danger(COMMAND_NAME + " " + playerId + " delete", DELETE_EMOJI);

        Button leftButton = Button.primary(COMMAND_NAME + " " + playerId + " left", LEFT_EMOJI);
        Button downButton = Button.primary(COMMAND_NAME + " " + playerId + " down", DOWN_EMOJI);
        Button rightButton = Button.primary(COMMAND_NAME + " " + playerId + " right", RIGHT_EMOJI);

        if (gameRenderer.getGame().getState() != GameState.ONGOING) {
            upButton = upButton.asDisabled();
            leftButton = leftButton.asDisabled();
            downButton = downButton.asDisabled();
            rightButton = rightButton.asDisabled();
        }

        return new MessageCreateBuilder()
            .setContent(scoreMessage(gameRenderer.getGame()))
            .addFiles(FileUpload.fromData(gameRenderer.getData(), "frame." + Renderer2048.IMAGE_FORMAT))
            .addActionRow(resetButton, upButton, deleteButton)
            .addActionRow(leftButton, downButton, rightButton)
            .build();
    }

    private String scoreMessage(Game2048 game) {
        return "__**Score:**__ " + game.getScore();
    }
}
