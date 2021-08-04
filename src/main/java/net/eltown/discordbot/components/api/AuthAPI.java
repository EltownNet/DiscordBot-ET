package net.eltown.discordbot.components.api;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.eltown.discordbot.Bot;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
public class AuthAPI {

    private final MongoCollection<Document> authCollection;
    private final Bot bot;

    private final Map<String, String> cachedToken = new HashMap<>();

    public AuthAPI(final MongoDatabase mongoDatabase, final Bot bot) {
        this.authCollection = mongoDatabase.getCollection("auth");
        this.bot = bot;
    }

    public void isAlreadyAuthorized(final String userId, final Consumer<Boolean> is) {
        CompletableFuture.runAsync(() -> {
            final Document document = this.authCollection.find(new Document("_id", userId)).first();
            is.accept(document != null);
        });
    }

    public void createAuthToken(final String userId, final String token) {
        this.cachedToken.put(token, userId);
    }

    public boolean isTokenValid(final String token) {
        for (final String s : this.cachedToken.keySet()) {
            if (s.equals(token)) return true;
        }
        return false;
    }

    public void isIngameNameAuthorized(final String ingameName, final Consumer<Boolean> is) {
        CompletableFuture.runAsync(() -> {
            final Document document = this.authCollection.find(new Document("ingameName", ingameName)).first();
            is.accept(document != null);
        });
    }

    public void getDiscordIdByIngameName(final String ingameName, final Consumer<String> id) {
        CompletableFuture.runAsync(() -> {
            final Document document = this.authCollection.find(new Document("ingameName", ingameName)).first();
            assert document != null;
            id.accept(document.getString("_id"));
        });
    }

    public void solveAuthentification(final String token, final String ingameName) {
        CompletableFuture.runAsync(() -> {
            final String id = this.cachedToken.get(token);
            final Document insert = new Document("_id", id).append("ingameName", ingameName);
            this.authCollection.insertOne(insert);
            this.cachedToken.remove(token);
        });
    }

}
