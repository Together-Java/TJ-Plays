package com.togetherjava.tjplays.games.game2048;

import com.togetherjava.tjplays.services.chatgpt.ChatGptService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class TriviaManager {
    private final ChatGptService gpt;
    private final ObjectMapper mapper = new ObjectMapper();

    public TriviaManager(String openAiKey) {
        this.gpt = new ChatGptService(openAiKey);
    }

    public Optional<QuizQuestion> fetchRandomQuestion() {
        String prompt = "Provide one Java trivia question as a JSON object like"
                + " {\"question\":\"...\",\"choices\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":<index>}";
        Optional<String> raw = gpt.ask(prompt, "java quiz");
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            QuizQuestion question = mapper.readValue(raw.get(), QuizQuestion.class);
            return Optional.of(question);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
