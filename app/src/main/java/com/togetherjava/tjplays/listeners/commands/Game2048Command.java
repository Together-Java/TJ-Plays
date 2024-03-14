package com.togetherjava.tjplays.listeners.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private Map<String, Renderer2048> sessions = new HashMap<>();

    public Game2048Command() {
        super(Commands.slash(COMMAND_NAME, "Game 2048"));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        Renderer2048 gameRenderer = new Renderer2048(new Game2048());

        event.reply(gameMessage(gameRenderer, event.getUser().getId()))
            .queue(hook -> {
                hook.retrieveOriginal().queue(message -> sessions.put(message.getId(), gameRenderer));
                hook.retrieveOriginal().queueAfter(10, TimeUnit.HOURS, message -> sessions.remove(message.getId()));
            });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (!buttonId.startsWith(COMMAND_NAME)) return;

        if (!buttonId.contains(event.getUser().getId())) {
            event.reply("You can't interact with this game.").setEphemeral(true).queue();
            return;
        }

        if (buttonId.contains("reset"))
            sessions.get(event.getMessageId()).setGame(new Game2048());
        else if (buttonId.contains("delete")) {
            sessions.remove(event.getMessageId());
            event.getMessage().delete().queue();
            return;
        }

        Move move = null;

        if (buttonId.contains("up")) move = Move.UP;
        else if (buttonId.contains("down")) move = Move.DOWN;
        else if (buttonId.contains("left")) move = Move.LEFT;
        else if (buttonId.contains("right")) move = Move.RIGHT;

        Renderer2048 gameRenderer = sessions.get(event.getMessageId());
        if (move != null)
            gameRenderer.getGame().move(move);

        event.editMessage(MessageEditData.fromCreateData(gameMessage(gameRenderer, event.getUser().getId()))).queue();
    }

    private MessageCreateData gameMessage(Renderer2048 gameRenderer, String playerId) {
        Button resetButton = Button.success(COMMAND_NAME + " " + playerId + " reset", Emoji.fromUnicode("🔃"));
        Button upButton = Button.primary(COMMAND_NAME + " " + playerId + " up", Emoji.fromUnicode("⬆️"));
        Button deleteButton = Button.danger(COMMAND_NAME + " " + playerId + " delete", Emoji.fromUnicode("🗑️"));

        Button leftButton = Button.primary(COMMAND_NAME + " " + playerId + " left", Emoji.fromUnicode("⬅️"));
        Button downButton = Button.primary(COMMAND_NAME + " " + playerId + " down", Emoji.fromUnicode("⬇️"));
        Button rightButton = Button.primary(COMMAND_NAME + " " + playerId + " right", Emoji.fromUnicode("➡️"));

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
