package com.togetherjava.tjplays.listeners.commands;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class SlashCommand extends ListenerAdapter {
    SlashCommandData data;

    public SlashCommand(SlashCommandData data) {
        this.data = data;
    }

    public SlashCommandData getData() {
        return data;
    }

    @Override
    abstract public void onSlashCommandInteraction(SlashCommandInteractionEvent event);

    abstract public String getName();

    // Should not be implemented
    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {}

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {}
}
