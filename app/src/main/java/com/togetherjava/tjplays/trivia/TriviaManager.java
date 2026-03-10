package com.togetherjava.tjplays.trivia;

import com.togetherjava.tjplays.services.chatgpt.ChatGptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

/**
 * Manages Java trivia questions using ChatGptService.
 * Used by quiz commands to fetch questions for users.
 * Future contributors can use this for any trivia/quiz feature.
 */
public class TriviaManager {
    private final ChatGptService chatGptService;
    private final ObjectMapper mapper = new ObjectMapper();

    public TriviaManager(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    public Optional<QuizQuestion> fetchRandomQuestion() {
        String prompt = "Provide one Java trivia question as a JSON object like"
                + " {\"question\":\"...\",\"choices\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":<index>}";
        Optional<String> raw = chatGptService.ask(prompt, "java quiz");
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
