package net.eltown.discordbot.components.api;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.eltown.discordbot.Bot;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TicketAPI {

    private final MongoCollection<Document> ticketCollection;
    private final Bot bot;

    public final Map<String, Ticket> cachedTickets = new HashMap<>();

    public TicketAPI(final MongoDatabase mongoDatabase, final Bot bot) {
        this.ticketCollection = mongoDatabase.getCollection("tickets");
        this.bot = bot;

        this.ticketCollection.find().forEach((Block<? super Document>) document -> {
            try {
                final String status = document.getString("status");
                final TextChannel channel = this.bot.getDiscordApi().getServerTextChannelById(document.getString("ticket")).get();
                final Message startMessage = this.bot.getDiscordApi().getMessageById(document.getString("panel"), channel).get();
                Message closeMessage = null;
                if (!document.getString("close").equals("null")) closeMessage = this.bot.getDiscordApi().getMessageById(document.getString("close"), channel).get();
                final User ticketUser = this.bot.getDiscordApi().getUserById(document.getString("user")).get();
                final User creatorUser = this.bot.getDiscordApi().getUserById(document.getString("creator")).get();
                final List<User> memberUsers = new ArrayList<>();
                final Message finalCloseMessage = closeMessage;
                document.getList("members", String.class).forEach(e -> {
                    try {
                        memberUsers.add(this.bot.getDiscordApi().getUserById(e).get());
                    } catch (final InterruptedException | ExecutionException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    this.cachedTickets.put(channel.getIdAsString(), new Ticket(channel, startMessage, finalCloseMessage, status, ticketUser, creatorUser, memberUsers));
                });
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public void createTicket(final User user, final User creator, final Server server) {
        CompletableFuture.runAsync(() -> {
            final String id = this.getTicketName();

            final ServerTextChannel channel = new ServerTextChannelBuilder(server)
                    .setCategory(server.getChannelCategoryById("794986203753611315").get())
                    .setName("\uD83D\uDD13-ticket-" + id)
                    .addPermissionOverwrite(user, Permissions.fromBitmask(PermissionType.SEND_MESSAGES.getValue() + PermissionType.READ_MESSAGES.getValue()))
                    .addPermissionOverwrite(server.getRoleById("856910901193736203").get(), Permissions.fromBitmask(PermissionType.SEND_MESSAGES.getValue() + PermissionType.READ_MESSAGES.getValue()))
                    .addPermissionOverwrite(server.getEveryoneRole(), Permissions.fromBitmask(0, PermissionType.READ_MESSAGES.getValue() + PermissionType.MENTION_EVERYONE.getValue() +
                            PermissionType.USE_EXTERNAL_EMOJIS.getValue() + PermissionType.ADD_REACTIONS.getValue()))
                    .setSlowmodeDelayInSeconds(5)
                    .create()
                    .join();
            channel.sendMessage("<@" + user.getId() + ">").thenAccept(message -> {
                message.delete();

                final EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Ticket #ticket-" + id + " eröffnet.")
                        .setDescription("Hi <@" + user.getId() + ">!")
                        .addField("\uD83C\uDDE9\uD83C\uDDEA", "Schreibe dein Anliegen bitte hier in den Chat. Ein Supporter oder Admin wird sich um dich kümmern, sobald er/sie Zeit hat." +
                                "\nUm das Ticket zu schließen, klicke auf das Schloss-Symbol.", true)
                        .setColor(Color.ORANGE);
                channel.sendMessage(embed).thenAccept(embedMessage -> {
                    final List<String> members = new ArrayList<>();
                    final List<User> membersUsers = new ArrayList<>();
                    members.add(user.getIdAsString());
                    membersUsers.add(user);
                    final Document document = new Document("ticket", channel.getIdAsString())
                            .append("status", "open")
                            .append("panel", embedMessage.getIdAsString())
                            .append("close", "null")
                            .append("user", user.getIdAsString())
                            .append("creator", creator.getIdAsString())
                            .append("members", members);
                    this.ticketCollection.insertOne(document);

                    embedMessage.addReaction("🔐");
                    if (user != creator) {
                        final EmbedBuilder embed2 = new EmbedBuilder()
                                .setDescription("Dieses Ticket wurde von <@" + creator.getId() + "> eröffnet.")
                                .setColor(Color.GRAY);
                        channel.sendMessage(embed2);
                    }

                    if (this.cachedTickets.values().size() >= 5) {
                        final EmbedBuilder delayEmbed = new EmbedBuilder()
                                .setTitle("Achtung: Verzögerung")
                                .setDescription("Ticket " + channel.getName().split("-")[2])
                                .addField("\uD83C\uDDE9\uD83C\uDDEA", "Aktuell haben wir viele Tickets offen, weshalb sich dadurch die Antwortzeiten verlängern. Wir bitten dies zu entschuldigen.", true)
                                .setColor(Color.RED);
                        channel.sendMessage(delayEmbed);
                    }

                    this.cachedTickets.put(channel.getIdAsString(), new Ticket(
                            channel,
                            embedMessage,
                            null,
                            "open",
                            user,
                            creator,
                            membersUsers
                    ));
                });
            });
        });
    }

    public void closeTicket(final ServerTextChannel channel, final Ticket ticket, final User closer) {
        CompletableFuture.runAsync(() -> {
            final String channelRaw = channel.getName();
            final String[] splitChannel = channelRaw.split("-");

            final EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("Ticket **#" + splitChannel[1] + "-" + splitChannel[2] + "** wurde von <@" + closer.getId() + "> geschlossen.")
                    .setColor(Color.RED);
            channel.sendMessage(embed).thenAccept(message -> {

                final Document found = this.ticketCollection.find(new Document("ticket", channel.getIdAsString())).first();
                final Bson newEntrySet = new Document("$set", new Document("status", "closed").append("close", message.getIdAsString()));
                assert found != null;
                this.ticketCollection.updateOne(found, newEntrySet);

                final ServerTextChannelUpdater updater = channel.createUpdater();
                updater.setName("🔐-" + splitChannel[1] + "-" + splitChannel[2]);
                ticket.getMembers().forEach(updater::removePermissionOverwrite);
                updater.update();

                ticket.setCloseMessage(message);
                ticket.setStatus("closed");
                message.removeAllReactions();
                message.addReactions("🔴", "🟢");

                this.cachedTickets.put(channel.getIdAsString(), ticket);
            });
        });
    }

    public void addMember(final ServerTextChannel channel, final Ticket ticket, final User target) {
        CompletableFuture.runAsync(() -> {
            final ServerTextChannelUpdater updater = channel.createUpdater();
            updater.addPermissionOverwrite(target, Permissions.fromBitmask(PermissionType.SEND_MESSAGES.getValue() + PermissionType.READ_MESSAGES.getValue()));
            updater.update();

            final List<User> members = ticket.getMembers();
            if (!members.contains(target)) {
                members.add(target);
                final Document found = this.ticketCollection.find(new Document("ticket", channel.getIdAsString())).first();
                final Bson newEntrySet = new Document("$set", new Document("members", members));
                assert found != null;
                this.ticketCollection.updateOne(found, newEntrySet);
            } else {
                final EmbedBuilder embed2 = new EmbedBuilder()
                        .setDescription("<@" + target.getId() + "> wurde bereits diesem Ticket hinzugefügt.")
                        .setColor(Color.GRAY);
                channel.sendMessage(embed2);
            }

            final EmbedBuilder embed2 = new EmbedBuilder()
                    .setDescription("<@" + target.getId() + "> wurde zu disem Ticket hinzugefügt.")
                    .setColor(Color.GRAY);
            channel.sendMessage(embed2);

            ticket.setMembers(members);
            this.cachedTickets.put(channel.getIdAsString(), ticket);
        });
    }

    public void deleteTicket(final ServerTextChannel channel) {
        CompletableFuture.runAsync(() -> {
            this.ticketCollection.findOneAndDelete(new Document("ticket", channel.getIdAsString()));
            channel.delete();

            this.cachedTickets.remove(channel.getIdAsString());
        });
    }

    public void reopenTicket(final ServerTextChannel channel, final User opener) {
        CompletableFuture.runAsync(() -> {
            final Document found = this.ticketCollection.find(new Document("ticket", channel.getIdAsString())).first();
            final Bson newEntrySet = new Document("$set", new Document("status", "open").append("close", "null"));
            assert found != null;
            this.ticketCollection.updateOne(found, newEntrySet);

            final Ticket ticket = this.cachedTickets.get(channel.getIdAsString());
            final String channelRaw = channel.getName();
            final String[] splitChannel = channelRaw.split("-");
            final ServerTextChannelUpdater updater = channel.createUpdater();

            updater.setName("🔓-" + splitChannel[1] + "-" + splitChannel[2]);
            ticket.getMembers().forEach(member -> {
                updater.addPermissionOverwrite(member, Permissions.fromBitmask(PermissionType.SEND_MESSAGES.getValue() + PermissionType.READ_MESSAGES.getValue()));
            });
            updater.update();

            final Message closeMessage = ticket.getCloseMessage();
            closeMessage.removeAllReactions();

            final Message startMessage = ticket.getStartMessage();
            startMessage.addReaction("🔐");

            final EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("Ticket **#" + splitChannel[1] + "-" + splitChannel[2] + "** wurde von <@" + opener.getId() + "> erneut geöffnet.")
                    .setColor(Color.GREEN);
            channel.sendMessage(embed);

            ticket.setCloseMessage(null);
            ticket.setStatus("open");
            this.cachedTickets.put(channel.getIdAsString(), ticket);
        });
    }

    private String getTicketName() {
        final String chars = "1234567890";
        final StringBuilder stringBuilder = new StringBuilder();
        final Random rnd = new Random();
        while (stringBuilder.length() < 5) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Ticket {

        private final Channel channel;
        private final Message startMessage;
        private Message closeMessage;
        private String status;
        private final User ticketUser;
        private final User creatorUser;
        private List<User> members;

    }

}
