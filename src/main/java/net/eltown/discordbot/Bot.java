package net.eltown.discordbot;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.eltown.discordbot.commands.AuthCommand;
import net.eltown.discordbot.commands.ticketsystem.TicketCommand;
import net.eltown.discordbot.components.api.AuthAPI;
import net.eltown.discordbot.components.api.TicketAPI;
import net.eltown.discordbot.components.messaging.AuthListener;
import net.eltown.discordbot.components.services.CommandService;
import net.eltown.discordbot.components.tinyrabbit.TinyRabbitListener;
import net.eltown.discordbot.listeners.CommandListener;
import net.eltown.discordbot.listeners.TicketListener;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class Bot {

    /**
     * Bot
     */
    private final DiscordApi discordApi;
    private final CommandService commandService;
    private final ExecutorService executorService;

    /**
     * API
     */
    private AuthAPI authAPI;
    private TicketAPI ticketAPI;

    /**
     * Database
     */
    private MongoClient databaseClient;
    private MongoDatabase database;

    /**
     * Messaging
     */
    private TinyRabbitListener listener;
    private AuthListener authListener;

    public Bot(final String token, final CommandService commandService) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        commandService.register(
                new AuthCommand(this),
                new TicketCommand(this)
        );
        this.discordApi = new DiscordApiBuilder()
                .setToken(token)
                .addListener(new CommandListener(this))
                .addListener(new TicketListener(this))
                .login().join();
        System.out.println("[bot] Bot status: Online");
        this.discordApi.updateStatus(UserStatus.DO_NOT_DISTURB);
        this.discordApi.updateActivity(ActivityType.PLAYING, "auf Eltown.net");
        this.connectDatabase();
        this.authAPI = new AuthAPI(this.database, this);
        this.authListener = new AuthListener(this);
        this.authListener.startListening();
        this.ticketAPI = new TicketAPI(this.database, this);
        this.commandService = commandService;
        System.out.println("[bot] All API Components successfully initialized.");
    }

    private void connectDatabase() {
        try {
            final MongoClientURI clientURI = new MongoClientURI("mongodb://root:e67bLwYNdv45g6smn3H9p32JzfsdgzYt6hNnYK323wdL@45.138.50.23:27017/admin?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false");
            this.databaseClient = new MongoClient(clientURI);
            this.database = databaseClient.getDatabase("eltown_bot");
            final Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.OFF);

            this.listener = new TinyRabbitListener("localhost");

            System.out.println("[bot] Connected to database!");
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("[bot] Failed to connect to database!");
        }
    }

}
