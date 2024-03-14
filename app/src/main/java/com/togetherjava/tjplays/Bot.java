package com.togetherjava.tjplays;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.togetherjava.tjplays.listeners.commands.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class Bot {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();

        String configPath = args.length == 0 ? "bot-config.properties" : args[0];
        properties.load(new FileInputStream(configPath));

        String token = properties.getProperty("BOT_TOKEN");
        JDA jda = JDABuilder.createDefault(token).build();

        List<SlashCommand> commands = getCommands();
        commands.forEach(command -> jda.addEventListener(command));

        List<SlashCommandData> commandDatas = commands.stream()
            .map(SlashCommand::getData)
            .toList();

        jda.updateCommands().addCommands(commandDatas).queue();
    }

    private static List<SlashCommand> getCommands() {
        return List.of(
            new PingCommand(),
            new Game2048Command()
        );
    }
}
