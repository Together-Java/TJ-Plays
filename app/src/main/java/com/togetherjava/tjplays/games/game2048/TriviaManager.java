package com.togetherjava.tjplays.games.game2048;

import com.togetherjava.tjplays.services.chatgpt.ChatGptService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


public class TriviaManager {
    private final ChatGptService gpt;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper();

    public TriviaManager(String openAiKey) {
        this.gpt = new ChatGptService(openAiKey);
    }

   
    public Optional<QuizQuestion> fetchRandomQuestion() {
        String prompt = "Provide five different Java trivia questions in JSON array;"
                + " each element should look like {\"question\":...,\"choices\":[...],\"correct\":<index>}";
        Optional<String> raw = gpt.ask(prompt, "java quiz");
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        List<QuizQuestion> list = parseArray(raw.get());
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(random.nextInt(list.size())));
    }

    private List<QuizQuestion> parseArray(String raw) {
        try {
            JsonNode arr = mapper.readTree(raw);
            List<QuizQuestion> questions = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    parseNode(node).ifPresent(questions::add);
                }
            }
            return questions;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Optional<QuizQuestion> parseNode(JsonNode node) {
        try {
            String q = node.get("question").asText();
            List<String> choices = new ArrayList<>();
            for (JsonNode c : node.get("choices")) {
                choices.add(c.asText());
            }
            int correct = node.get("correct").asInt();
            return Optional.of(new QuizQuestion(q, choices, correct));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
