package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Help {

    // Attributes
    private final CommandHandler commandHandler;
    private final JsonNode whitelist;
    private final JsonNode blacklist;

    // Constructor
    public Help(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, JsonNode whitelist, JsonNode blacklist) {

        // Syntax
        String prefix = commandHandler.getPrefix();
        String syntax = prefix + "help commands oder " + prefix + "help <Befehl>";

        // About
        String[] name = {"help", "hilfe"};
        String description = "Um die verfügbaren Befehle zu sehen, schreibe: " + syntax;

        // Init Attributes
        this.commandHandler = commandHandler;
        this.whitelist = whitelist;
        this.blacklist = blacklist;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);
                String arg = args.length > 0 ? args[0].toLowerCase() : "";

                // Help Commands
                String response;
                if (Arrays.asList("commands", "command", "befehle", "befehl").contains(arg)) response = helpCommands(event, mySQL); // Help Commands
                else if (getCommandDescription(channel, arg) != null) response = getCommandDescription(channel, arg); // Command Description
                else response = getDescription(); // Help Description (Default)

                // Send message
                chat.sendMessage(channel, response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Gets all available commands
    private String helpCommands(ChannelMessageEvent event, MySQL mySQL) {
        StringBuilder message = new StringBuilder("Available commands: ");

        // Get channel
        String channel = getChannel(event);

        // Get prefix
        String prefix = commandHandler.getPrefix();

        // Get commands
        HashMap<String, Command> commands = commandHandler.getCommands();

        // Filter available commands
        for (String command : commands.keySet()) {
            if (whitelist.has(command.toLowerCase())) {
                if (Arrays.stream(whitelist.get(command.toLowerCase()).asText().toLowerCase().split("; ")).toList().contains(channel))
                    message.append(prefix).append(command).append(", ");
            } else if (blacklist.has(command.toLowerCase())) {
                if (!Arrays.stream(blacklist.get(command.toLowerCase()).asText().toLowerCase().split("; ")).toList().contains(channel))
                    message.append(prefix).append(command).append(", ");
            } else message.append(prefix).append(command).append(", ");
        }

        // Add custom commands
        for (String command : mySQL.getCommands(event, false)) message.append(prefix).append(command).append(", ");

        // Return message
        return message.substring(0, message.length() - 2) + '.';
    }

    // Gets the description of a command
    private String getCommandDescription(String channel, String command) {

        // Get command
        if (commandHandler.getAliases().containsKey(command)) command = commandHandler.getAliases().get(command);

        // Get description if command is available
        //todo: eh unclean
        if (whitelist.has(command.toLowerCase())) {
            if (Arrays.stream(whitelist.get(command.toLowerCase()).asText().toLowerCase().split("; ")).toList().contains(channel))
                return commandHandler.getCommands().get(command).getDescription();
        } else if (blacklist.has(command.toLowerCase())) {
            if (!Arrays.stream(blacklist.get(command.toLowerCase()).asText().toLowerCase().split("; ")).toList().contains(channel))
                return commandHandler.getCommands().get(command).getDescription();
        } else if (commandHandler.getCommands().containsKey(command))
            return commandHandler.getCommands().get(command).getDescription();
        return null;
    }
}