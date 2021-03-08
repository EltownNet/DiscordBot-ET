package net.eltown.discordbot;

import lombok.Getter;
import net.eltown.discordbot.components.services.CommandService;

public class Main {

    @Getter
    private static Bot bot;

    public static void main(final String[] args) {
        final CommandService commandService = new CommandService();
        bot = new Bot("Nzk1MjczNDAzNjQ4NzA0NTIy.X_G-Eg.4p99BzKDzw7uPjUntYMXb-u54Wg", commandService);
    }

}
