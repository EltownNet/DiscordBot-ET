package net.eltown.discordbot;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.eltown.discordbot.components.services.CommandService;
import net.eltown.discordbot.listeners.CommandListener;
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


    /**
     * Database
     */
    private MongoClient databaseClient;
    private MongoDatabase database;

    public Bot(final String token, final CommandService commandService) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        commandService.register(
        );
        this.discordApi = new DiscordApiBuilder()
                .setToken(token)
                .addListener(new CommandListener(this))
                .login().join();
        System.out.println("[bot] Bot status: Online");
        this.discordApi.updateStatus(UserStatus.DO_NOT_DISTURB);
        this.discordApi.updateActivity(ActivityType.PLAYING, "auf Eltown.net");
        this.connectDatabase();
        //api init
        this.commandService = commandService;
        System.out.println("[bot] All API Components successfully initialized.");
    }

    private void connectDatabase() {
        try {
            final MongoClientURI clientURI = new MongoClientURI("mongodb://root:e67b!LwYNdv45g6smn3H9p!32JzfsdgzYt6hNnYK323!wdL@185.223.28.34:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false");
            this.databaseClient = new MongoClient(clientURI);
            this.database = databaseClient.getDatabase("eltown_bot");
            final Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.OFF);
            System.out.println("[bot] Connected to database!");
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("[bot] Failed to connect to database!");
        }
    }

}
