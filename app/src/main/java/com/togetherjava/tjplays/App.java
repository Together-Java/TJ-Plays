package com.togetherjava.tjplays;

import java.util.List;

import com.togetherjava.tjplays.listeners.commands.PingCommand;
import com.togetherjava.tjplays.listeners.commands.SlashCommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public final class App {
    public static void main(String[] args) {
        JDA jda = JDABuilder.createDefault(args[0]).build();

        List<SlashCommand> commands = List.of(new PingCommand());
        commands.forEach(command -> jda.addEventListener(command));

        CommandListUpdateAction commandUpdateList = jda.updateCommands();

        commands.stream()
            .map(SlashCommand::getData)
            .forEach(commandUpdateList::addCommands);

        commandUpdateList.queue();
    }
}
