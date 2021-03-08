package net.eltown.discordbot.listeners;

import lombok.RequiredArgsConstructor;
import net.eltown.discordbot.Bot;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

@RequiredArgsConstructor
public class CommandListener implements MessageCreateListener {

    final Bot bot;

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) return;
        this.bot.getCommandService().handle(event);
    }

}
