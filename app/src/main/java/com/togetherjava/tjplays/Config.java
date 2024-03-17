package com.togetherjava.tjplays;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record Config(
    @NotNull String botToken,
    @NotNull String openAIApiKey
) {
    private static final String DEFAULT_PATH = "bot-config.properties";

    public static Config readConfig(@Nullable Path configPath) throws IOException {
        Properties properties = new Properties();

        configPath = configPath == null ? Path.of(DEFAULT_PATH) : configPath;
        properties.load(new FileInputStream(configPath.toString()));

        return new Config(
            properties.getProperty("BOT_TOKEN"),
            properties.getProperty("OPENAI_API_KEY")
        );
    }
}
