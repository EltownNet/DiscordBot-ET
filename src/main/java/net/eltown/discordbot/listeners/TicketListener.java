package net.eltown.discordbot.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.discordbot.Bot;
import net.eltown.discordbot.components.api.TicketAPI;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class TicketListener implements ReactionAddListener {

    final Bot bot;

    @SneakyThrows
    @Override
    public void onReactionAdd(final ReactionAddEvent event) {
        User user = null;
        try {
            user = event.requestUser().get();
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (!user.isBot()) {
            if (!event.getChannel().asPrivateChannel().isPresent()) {
                final String channel = event.getServerTextChannel().get().getName();
                if (String.valueOf(event.getMessageId()).equals("857669348704649237")) {
                    if (event.getEmoji().equalsEmoji("🎫")) {
                        event.removeReaction();
                        final Server server = event.getServer().get();
                        this.bot.getTicketAPI().createTicket(user, user, server);
                    }
                } else if (channel.startsWith("🔓-ticket")) {
                    if (event.getEmoji().equalsEmoji("🔐")) {
                        final TicketAPI.Ticket ticket = this.bot.getTicketAPI().cachedTickets.get(event.getChannel().getIdAsString());
                        if (String.valueOf(event.getMessageId()).equals(ticket.getStartMessage().getIdAsString())) {
                            event.removeReaction();
                            this.bot.getTicketAPI().closeTicket(event.getServerTextChannel().get(), this.bot.getTicketAPI().cachedTickets.get(event.getChannel().getIdAsString()), user);
                        }
                    }
                } else if (channel.startsWith("🔐-ticket")) {
                    final TicketAPI.Ticket ticket = this.bot.getTicketAPI().cachedTickets.get(event.getChannel().getIdAsString());
                    if (ticket.getCloseMessage() == null) return;
                    if (String.valueOf(event.getMessageId()).equals(ticket.getCloseMessage().getIdAsString())) {
                        if (event.getEmoji().equalsEmoji("🔴")) {
                            event.removeReaction();
                            this.bot.getTicketAPI().deleteTicket(event.getServerTextChannel().get());
                        } else if (event.getEmoji().equalsEmoji("🟢")) {
                            event.removeReaction();
                            this.bot.getTicketAPI().reopenTicket(event.getServerTextChannel().get(), user);
                        }
                    }
                }
            }
        }
    }

}