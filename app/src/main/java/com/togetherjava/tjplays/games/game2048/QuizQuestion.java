package com.togetherjava.tjplays.games.game2048;

import java.util.List;

public class QuizQuestion {
    private final String question;
    private final List<String> choices;
    private final int correctIndex;

    public QuizQuestion(String question, List<String> choices, int correctIndex) {
        this.question = question;
        this.choices = choices;
        this.correctIndex = correctIndex;
    }

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