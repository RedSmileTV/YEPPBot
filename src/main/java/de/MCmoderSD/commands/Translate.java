package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.other.OpenAI;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Translate {

    // Attributes
    private final int maxTokens;
    private final double temperature;

    // Constructor
    public Translate(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, OpenAI openAI, String botName) {

        // About
        String[] name = {"translator", "translate", "übersetzer", "übersetze"};
        String description = "Kann deine Sätze in jede erdenkliche Sprache übersetzen: " + commandHandler.getPrefix() + "translate <Sprache> <Frage>";

        // Load Config
        JsonNode config = openAI.getConfig();
        maxTokens = config.get("maxTokens").asInt();
        temperature = 0;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Check for language
                String language = args[0];

                // Process text
                String text = trimMessage(processArgs(args)).replace(language, "");
                String instruction = trimMessage("Please translate the following text into " + language + ":");

                // Send message
                String response = openAI.prompt(botName, instruction, text, maxTokens, temperature);
                chat.sendMessage(getChannel(event), response);

                // Log response
                if (mySQL != null) mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}