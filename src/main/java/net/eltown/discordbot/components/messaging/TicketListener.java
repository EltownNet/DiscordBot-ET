package net.eltown.discordbot.components.messaging;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.discordbot.Bot;
import org.javacord.api.entity.user.User;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class TicketListener {

    private final Bot bot;

    @SneakyThrows
    public void startListening() {
        this.bot.getListener().callback((request -> {
            switch (TicketCalls.valueOf(request.getKey().toUpperCase())) {
                case REQUEST_OPEN_TICKET:
                    final String user = request.getData()[1];
                    final String subject = request.getData()[2];
                    final String section = request.getData()[3];
                    final String message = request.getData()[4];

                    this.bot.getAuthAPI().isIngameNameAuthorized(user, is -> {
                        if (is) {
                            this.bot.getAuthAPI().getDiscordIdByIngameName(user, id -> {
                                try {
                                    final User discordUser = this.bot.getDiscordApi().getUserById(id).get();
                                    if (this.bot.getTicketAPI().getTicketCount(discordUser.getIdAsString()) <= 3) {
                                        this.bot.getTicketAPI().createTicket(discordUser, discordUser, user, subject, section, message);
                                        request.answer(TicketCalls.CALLBACK_NULL.name(), "null");
                                    } else request.answer(TicketCalls.CALLBACK_TOO_MANY_TICKETS.name(), "null");
                                } catch (InterruptedException | ExecutionException e) {
                                    request.answer(TicketCalls.CALLBACK_USER_NULL.name(), "null");
                                }
                            });
                        } else request.answer(TicketCalls.CALLBACK_NO_AUTH.name(), "null");
                    });
                    break;
            }
        }), "DiscordBot/TicketAPI/Listener", "discord.bot.ticket");
    }

    public enum TicketCalls {

        REQUEST_OPEN_TICKET,
        CALLBACK_NO_AUTH,
        CALLBACK_TOO_MANY_TICKETS,
        CALLBACK_USER_NULL,
        CALLBACK_NULL

    }

}
