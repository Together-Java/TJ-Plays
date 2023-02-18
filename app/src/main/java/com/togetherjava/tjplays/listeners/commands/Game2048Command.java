package com.togetherjava.tjplays.listeners.commands;

import java.util.HashMap;
import java.util.Map;

import com.togetherjava.tjplays.games.game2048.Game2048;
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
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public final class Game2048Command extends SlashCommand {
    private static final String COMMAND_NAME = "2048";
    private Map<String, Renderer2048> sessions = new HashMap<>();

    public Game2048Command() {
        super(Commands.slash(COMMAND_NAME, "Game 2048"));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(COMMAND_NAME)) return;

        Game2048 game = new Game2048();
        Renderer2048 gameRenderer = new Renderer2048(game);

        MessageCreateData messageData = new MessageCreateBuilder()
            .addContent(scoreMessage(game))
            .addFiles(FileUpload.fromData(gameRenderer.getData(), "frame." + Renderer2048.IMAGE_FORMAT))
            .addActionRow(Button.primary(" ", "i").asDisabled(),
                    Button.primary(COMMAND_NAME + " up", Emoji.fromUnicode("⬆️")),
                    Button.primary("i", "i").asDisabled())
            .addActionRow(Button.primary(COMMAND_NAME + " left", Emoji.fromUnicode("⬅️")),
                    Button.primary(COMMAND_NAME + " down", Emoji.fromUnicode("⬇️")),
                    Button.primary(COMMAND_NAME + " right", Emoji.fromUnicode("➡️")))
            .build();

        event.reply(messageData)
            .queue(hook -> hook.retrieveOriginal().queue(message -> sessions.put(message.getId(), gameRenderer)));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().startsWith(COMMAND_NAME)) return;

        Move move = null;

        if (event.getButton().getId().contains("up")) move = Move.UP;
        else if (event.getButton().getId().contains("down")) move = Move.DOWN;
        else if (event.getButton().getId().contains("left")) move = Move.LEFT;
        else if (event.getButton().getId().contains("right")) move = Move.RIGHT;

        Renderer2048 gameRenderer = sessions.get(event.getMessageId());
        gameRenderer.getGame().move(move);

        MessageEditData messageEditData = new MessageEditBuilder()
            .setContent(scoreMessage(gameRenderer.getGame()))
            .setAttachments(FileUpload.fromData(gameRenderer.getData(), "frame." + Renderer2048.IMAGE_FORMAT))
            .build();

        event.editMessage(messageEditData).queue();
    }

    private String scoreMessage(Game2048 game) {
        return "__**Score:**__ " + game.getScore();
    }
}
