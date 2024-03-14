package com.togetherjava.tjplays.listeners.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class GuessLanguageCommand extends SlashCommand{

    private static final String COMMAND_NAME = "guess-programming-language";
    public GuessLanguageCommand() {
        super(Commands.slash(COMMAND_NAME, "Try to guess the programming language"));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {

        event.reply("Hello").queue();

    }
}
