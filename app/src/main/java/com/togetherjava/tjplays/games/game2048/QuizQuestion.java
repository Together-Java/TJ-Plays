package com.togetherjava.tjplays.games.game2048;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QuizQuestion(
        @JsonProperty("question") String question,
        @JsonProperty("choices") List<String> choices,
        @JsonProperty("correct") int correctIndex
) {
    @JsonCreator
    public QuizQuestion {}

    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }
}