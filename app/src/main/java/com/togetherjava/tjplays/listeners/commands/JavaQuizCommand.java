package com.togetherjava.tjplays.listeners.commands;

import com.togetherjava.tjplays.games.game2048.QuizQuestion;
import com.togetherjava.tjplays.games.game2048.TriviaManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class JavaQuizCommand extends SlashCommand {
    private static final String COMMAND_NAME = "javaquiz";

    private final TriviaManager triviaManager;
    private final ConcurrentHashMap<String, QuizQuestion> activeQuestions = new ConcurrentHashMap<>();

    public JavaQuizCommand(String openAiKey) {
        super(Commands.slash(COMMAND_NAME, "Get a random Java trivia question"));
        this.triviaManager = new TriviaManager(openAiKey);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Optional<QuizQuestion> question = triviaManager.fetchRandomQuestion();
        if (question.isEmpty()) {
            event.getHook().editOriginal("Could not fetch a quiz question. Try again later.").queue();
            return;
        }

        QuizQuestion q = question.get();
        List<String> choices = q.getChoices();

        StringBuilder sb = new StringBuilder();
        sb.append("**Java Quiz**\n\n");
        sb.append(q.getQuestion()).append("\n\n");
        for (int i = 0; i < choices.size(); i++) {
            sb.append("`").append(i + 1).append(")` ").append(choices.get(i)).append("\n");
        }

        String messageId = event.getHook().editOriginal(sb.toString())
                .setActionRow(
                    Button.primary(COMMAND_NAME + "-1-" + event.getUser().getId(), "1"),
                    Button.primary(COMMAND_NAME + "-2-" + event.getUser().getId(), "2"),
                    Button.primary(COMMAND_NAME + "-3-" + event.getUser().getId(), "3"),
                    Button.primary(COMMAND_NAME + "-4-" + event.getUser().getId(), "4")
                )
                .complete()
                .getId();

        activeQuestions.put(messageId, q);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId == null || !buttonId.startsWith(COMMAND_NAME)) {
            return;
        }

        if (!buttonId.contains(event.getUser().getId())) {
            event.reply("This isn't your quiz!").setEphemeral(true).queue();
            return;
        }

        QuizQuestion q = activeQuestions.remove(event.getMessageId());
        if (q == null) {
            event.reply("This quiz has already been answered.").setEphemeral(true).queue();
            return;
        }

        int chosen = Character.getNumericValue(buttonId.charAt(COMMAND_NAME.length() + 1)) - 1;
        boolean correct = chosen == q.getCorrectIndex();

        String result = correct
                ? "Correct! The answer is: **" + q.getChoices().get(q.getCorrectIndex()) + "**"
                : "Wrong! The correct answer was: **" + q.getChoices().get(q.getCorrectIndex()) + "**";

        String userId = event.getUser().getId();

        event.editMessage(event.getMessage().getContentRaw() + "\n\n" + result)
                .setActionRow(
                    Button.primary(COMMAND_NAME + "-1-" + userId, "1").asDisabled(),
                    Button.primary(COMMAND_NAME + "-2-" + userId, "2").asDisabled(),
                    Button.primary(COMMAND_NAME + "-3-" + userId, "3").asDisabled(),
                    Button.primary(COMMAND_NAME + "-4-" + userId, "4").asDisabled()
                )
                .queue();
    }
}
