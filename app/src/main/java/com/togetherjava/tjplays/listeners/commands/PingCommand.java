package com.togetherjava.tjplays.listeners.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public final class PingCommand extends SlashCommand {
    private static final String COMMAND_NAME = "ping";
    
    public PingCommand() {
        super(Commands.slash(COMMAND_NAME, "Replies with pong"));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }
}
