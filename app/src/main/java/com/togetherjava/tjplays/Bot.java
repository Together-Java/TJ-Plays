package com.togetherjava.tjplays;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import com.togetherjava.tjplays.listeners.commands.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class Bot {
    public static void main(String[] args) throws IOException {
        Config config = Config.readConfig(args.length == 0 ? null : Path.of(args[0]));

        createBot(config);
    }

    private static JDA createBot(Config config) {
        JDA jda = JDABuilder.createDefault(config.botToken()).build();

        List<SlashCommand> commands = getCommands(config);
        commands.forEach(command -> jda.addEventListener(command));

        List<SlashCommandData> commandDatas = commands.stream()
            .map(SlashCommand::getData)
            .toList();

        jda.updateCommands().addCommands(commandDatas).queue();

        return jda;
    }

    private static List<SlashCommand> getCommands(Config config) {
        return List.of(
            new PingCommand(),
            new Game2048Command()
        );
    }
}
