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
    // messageId -> QuizQuestion
    private final ConcurrentHashMap<String, QuizQuestion> activeQuestions = new ConcurrentHashMap<>();
    // userId -> messageId
    private final ConcurrentHashMap<String, String> userActiveQuiz = new ConcurrentHashMap<>();

    public JavaQuizCommand(String openAiKey) {
        super(Commands.slash(COMMAND_NAME, "Get a random Java trivia question"));
        this.triviaManager = new TriviaManager(openAiKey);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        // Prevent new quiz if user has an active one
        if (userActiveQuiz.containsKey(userId)) {
            event.reply("You already have an active quiz. Please answer it before starting a new one.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        Optional<QuizQuestion> question = triviaManager.fetchRandomQuestion();
        if (question.isEmpty()) {
            event.getHook().editOriginal("Could not fetch a quiz question. Try again later.").queue();
            return;
        }

        QuizQuestion quizQuestion = question.get();
        List<String> choices = quizQuestion.getChoices();

        StringBuilder sb = new StringBuilder();
        sb.append("**Java Quiz**\n\n");
        sb.append(quizQuestion.getQuestion()).append("\n\n");
        for (int i = 0; i < choices.size(); i++) {
            sb.append("`").append(i + 1).append("`) ").append(choices.get(i)).append("\n");
        }

        String messageId = event.getHook().editOriginal(sb.toString())
                .setActionRow(
                    Button.primary(COMMAND_NAME + "-1-" + userId, "1"),
                    Button.primary(COMMAND_NAME + "-2-" + userId, "2"),
                    Button.primary(COMMAND_NAME + "-3-" + userId, "3"),
                    Button.primary(COMMAND_NAME + "-4-" + userId, "4")
                )
                .complete()
                .getId();

        activeQuestions.put(messageId, quizQuestion);
        userActiveQuiz.put(userId, messageId);
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

        String userId = event.getUser().getId();
        QuizQuestion quizQuestion = activeQuestions.remove(event.getMessageId());
        // Remove user's active quiz
        userActiveQuiz.remove(userId);
        if (quizQuestion == null) {
            event.reply("This quiz has already been answered.").setEphemeral(true).queue();
            return;
        }

        int chosen = Character.getNumericValue(buttonId.charAt(COMMAND_NAME.length() + 1)) - 1;
        boolean correct = chosen == quizQuestion.getCorrectAnswerIndex();

        String correctAnswer = quizQuestion.getChoices().get(quizQuestion.getCorrectAnswerIndex());
        String result = correct
            ? "Correct! The answer is: **" + correctAnswer + "**"
            : "Wrong! The correct answer was: **" + correctAnswer + "**";

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
