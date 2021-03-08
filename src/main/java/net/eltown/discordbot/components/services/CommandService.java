package net.eltown.discordbot.components.services;

import net.eltown.discordbot.components.data.Command;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.*;

public class CommandService {

    private final Map<String, Command> commands = new HashMap<>();

    public void register(final Command... commands) {
        for (Command command : commands)
            this.commands.put(command.getName(), command);
    }

    public void handle(final MessageCreateEvent event) {
        final String prefix = ">";
        final String message = event.getMessage().getContent();
        final String[] raw = message.split(" ");

        if (!message.startsWith(prefix)) return;
        final String search = raw[0].substring(prefix.length());

        final List<String> argsList = new ArrayList<>(Arrays.asList(raw));
        argsList.remove(0);

        final String[] args = argsList.toArray(new String[0]);

        final Command command = this.commands.get(search);

        final Command.ExecuteData data = new Command.ExecuteData(event);

        if (command != null) {
            command.execute(data, args);
        } else {
            this.commands.values().forEach((cmd) -> {
                if (cmd.getAliases().contains(search))
                    command.execute(data, args);
            });
        }
    }

}
