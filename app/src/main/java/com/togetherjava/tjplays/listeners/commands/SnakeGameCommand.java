package com.togetherjava.tjplays.listeners.commands;

import com.togetherjava.tjplays.games.gamesnake.GameSnake;
import com.togetherjava.tjplays.utils.CardinalDirection;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class SnakeGameCommand extends SlashCommand {
    private static final String SNAKE_BUTTONS_PREFIX = UUID.randomUUID().toString();
    private static final String COMMAND_NAME = "snake";
    private static final String NO_WIDTH_WHITESPACE = "\u200B";
    private GameMessageId gameMessageId = null;
    private GameSnake game;

    public SnakeGameCommand() {
        super(Commands.slash(COMMAND_NAME, "Play the famous snake game !"));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(COMMAND_NAME)) return;
        event.reply("Started a game").queue();
        game = new GameSnake(event.getTimeCreated().toInstant());
        byte[] gifData = game.generateCurrentAnimationBuffer();
        FileUpload fileUpload = FileUpload.fromData(gifData, "game." + GameSnake.IMAGE_FORMAT);
        event.getChannel()
                .sendMessage("Snake game")
                .setFiles(fileUpload)
                .addActionRow(createButtons(NO_WIDTH_WHITESPACE, "⬆", NO_WIDTH_WHITESPACE))
                .addActionRow(createButtons("⬅", NO_WIDTH_WHITESPACE, "➡"))
                .addActionRow(createButtons(NO_WIDTH_WHITESPACE, "⬇", NO_WIDTH_WHITESPACE))
                .queue(this::recordGameMessage);
    }

    private List<Button> createButtons(String... texts) {
        return Stream.of(texts)
                .map(text -> {
                    Button b = Button.of(ButtonStyle.SECONDARY, SNAKE_BUTTONS_PREFIX + " " + UUID.randomUUID() + " " + text, text);
                    return text.equals(NO_WIDTH_WHITESPACE) ? b.asDisabled() : b;
                })
                .toList();
    }

    private void recordGameMessage(Message gameMessage) {
        gameMessageId = GameMessageId.fromMessage(gameMessage);
    }

    private record GameMessageId(long channelId, long messageId) {
        static GameMessageId fromMessage(Message message) {
            return new GameMessageId(message.getChannel().getIdLong(), message.getIdLong());
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getComponentId().startsWith(SNAKE_BUTTONS_PREFIX)) {
            return;
        }
        String buttonPressed = event.getComponentId().split(" ")[2];
        if(NO_WIDTH_WHITESPACE.equals(buttonPressed)) {
            event.deferEdit().queue();
            return;
        }
        CardinalDirection direction = switch (buttonPressed) {
            case "⬆" -> CardinalDirection.UP;
            case "⬅" -> CardinalDirection.LEFT;
            case "➡" -> CardinalDirection.RIGHT;
            case "⬇" -> CardinalDirection.DOWN;
            default -> throw new AssertionError("unknown");
        };
        game.onNewDirectionAction(event.getTimeCreated().toInstant(), direction);
        byte[] gifData = game.generateCurrentAnimationBuffer();
        FileUpload fileUpload = FileUpload.fromData(gifData, "game." + GameSnake.IMAGE_FORMAT);
        MessageEditData message = new MessageEditBuilder().setAttachments(fileUpload).build();
        TextChannel channel = event.getJDA().getTextChannelById(gameMessageId.channelId);
        Objects.requireNonNull(channel).editMessageById(gameMessageId.messageId, message).queue();
        event.deferEdit().queue();
    }
}
