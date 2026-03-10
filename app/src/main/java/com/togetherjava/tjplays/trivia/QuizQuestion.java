package com.togetherjava.tjplays.trivia;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a single quiz question for Java trivia.
 * Used by TriviaManager and JavaQuizCommand to display questions and validate answers.
 * Future contributors can use this for any quiz/trivia feature needing a question, choices, and answer index.
 */
public record QuizQuestion(
    @JsonProperty("question") String question,
    @JsonProperty("choices") List<String> choices,
    @JsonProperty("correct") int correctAnswerIndex
) {
    @JsonCreator
    public QuizQuestion {}

    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    /**
     * Returns the index of the correct answer in the choices list.
     */
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
}
