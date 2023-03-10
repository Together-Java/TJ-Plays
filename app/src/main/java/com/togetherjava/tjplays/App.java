package com.togetherjava.tjplays;

import java.util.List;

import com.togetherjava.tjplays.listeners.commands.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class App {
    public static void main(String[] args) {
        JDA jda = JDABuilder.createDefault(args[0]).build();

        List<SlashCommand> commands = List.of(new PingCommand(), new Game2048Command());
        commands.forEach(command -> jda.addEventListener(command));

        List<SlashCommandData> commandDatas = commands.stream()
            .map(SlashCommand::getData)
            .toList();

        jda.updateCommands().addCommands(commandDatas).queue();
    }
}
