package net.eltown.discordbot.commands.ticketsystem;

import net.eltown.discordbot.Bot;
import net.eltown.discordbot.components.api.TicketAPI;
import net.eltown.discordbot.components.data.Command;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class TicketCommand extends Command {

    final Bot bot;

    public TicketCommand(final Bot bot) {
        super("ticket", new HashSet<>());
        this.bot = bot;
    }

    @Override
    public void execute(final ExecuteData data, final String[] args) {
        data.getMessage().delete();
        final User user = data.getAuthor().asUser().get();
        final ServerTextChannel channel = this.bot.getDiscordApi().getServerTextChannelById(data.getChannel().getIdAsString()).get();
        final Server server = data.getMessage().getServer().get();
        if (user.getRoles(server).contains(server.getRoleById("856910901193736203").get())) {
            if (args.length == 1) {
                final TicketAPI.Ticket ticket = this.bot.getTicketAPI().cachedTickets.get(channel.getIdAsString());
                if (args[0].equalsIgnoreCase("info")) {
                    if (ticket != null) {
                        final EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("Ticket " + channel.getName().split("-")[2])
                                .setDescription("[> Zur Startnachricht](https://discord.com/channels/794937648178397194/" + ticket.getChannel().getIdAsString() + "/" + ticket.getStartMessage().getIdAsString() + ")")
                                .addField("Nutzer des Tickets", "<@" + ticket.getTicketUser().getIdAsString() + ">", true)
                                .addField("Ersteller des Tickets", "<@" + ticket.getCreatorUser().getIdAsString() + ">", true);
                        channel.sendMessage(embed);
                    }
                } else if (args[0].equalsIgnoreCase("close")) {
                    if (ticket != null) {
                        this.bot.getTicketAPI().closeTicket(channel, ticket, user);
                    }
                } else if (args[0].equalsIgnoreCase("panel")) {
                    channel.sendMessage(new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setTitle("Ticket Support")
                            .setDescription("Spielerbeschwerden | Fehlermeldungen | Fragen | Vorschläge")
                            .addField("\uD83C\uDDE9\uD83C\uDDEA", "Wenn du dich über einen Spieler beschweren möchtest, einen Fehler melden möchtest, Fragen hast oder Vorschläge hast, dann eröffne einfach ein Ticket. Klicke dafür das Ticket-Symbol an.", true))
                            .thenAccept(embedMessage -> {
                                embedMessage.addReaction("🎫");
                            });
                }
            } else if (args.length == 2) {
                final TicketAPI.Ticket ticket = this.bot.getTicketAPI().cachedTickets.get(channel.getIdAsString());
                if (args[0].equalsIgnoreCase("create")) {
                    try {
                        final User target = this.bot.getDiscordApi().getUserById(args[1]).get();
                        this.bot.getTicketAPI().createTicket(target, user);
                        final EmbedBuilder embed2 = new EmbedBuilder()
                                .setDescription("Ein Ticket für <@" + target.getId() + "> wurde soeben erstellt..")
                                .setColor(Color.GRAY);
                        channel.sendMessage(embed2);
                    } catch (final InterruptedException | ExecutionException e) {
                        final EmbedBuilder embed2 = new EmbedBuilder()
                                .setDescription("Der Nutzer **" + args[1] + "** konnte anhand der ID nicht gefunden werden.")
                                .setColor(Color.RED);
                        channel.sendMessage(embed2);
                    }
                } else if (args[0].equalsIgnoreCase("add")) {
                    if (ticket != null) {
                        try {
                            final User target = this.bot.getDiscordApi().getUserById(args[1]).get();
                            this.bot.getTicketAPI().addMember(channel, ticket, target);
                        } catch (final InterruptedException | ExecutionException e) {
                            final EmbedBuilder embed2 = new EmbedBuilder()
                                    .setDescription("Der Nutzer **" + args[1] + "** konnte nicht gefunden werden.")
                                    .setColor(Color.RED);
                            channel.sendMessage(embed2);
                        }
                    }
                }
            }
        }
    }

}