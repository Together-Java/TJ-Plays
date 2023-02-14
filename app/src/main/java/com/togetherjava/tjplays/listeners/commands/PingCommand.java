package com.togetherjava.tjplays.listeners.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public final class PingCommand extends SlashCommand {
    private static final String COMMAND_NAME = "ping";
    
    public PingCommand() {
        super(Commands.slash(COMMAND_NAME, "Replies with pong"));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(COMMAND_NAME)) return;

        event.reply("Pong!").queue();
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }
}
