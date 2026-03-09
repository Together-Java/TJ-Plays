package com.togetherjava.tjplays.games.game2048;

import java.util.Optional;

public class javaquiz {
    public static void main(String[] args) {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            System.err.println("OPENAI_API_KEY is not set. Set it as an environment variable.");
            return;
        }

        TriviaManager trivia = new TriviaManager(key);
        Optional<QuizQuestion> question = trivia.fetchRandomQuestion();

        question.ifPresentOrElse(
                q -> {
                    System.out.println("Question: " + q.getQuestion());
                    var choices = q.getChoices();
                    for (int i = 0; i < choices.size(); i++) {
                        System.out.println("  " + (i + 1) + ") " + choices.get(i));
                    }
                    System.out.println("Correct answer: " + (q.getCorrectIndex() + 1));
                },
                () -> System.err.println("No response from ChatGPT."));
    }
}
