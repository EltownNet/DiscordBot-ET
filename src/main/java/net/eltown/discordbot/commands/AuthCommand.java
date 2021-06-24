package net.eltown.discordbot.commands;

import net.eltown.discordbot.Bot;
import net.eltown.discordbot.components.data.Command;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class AuthCommand extends Command {

    private final Bot bot;

    public AuthCommand(final Bot bot) {
        super("auth", new HashSet<>(Arrays.asList("link", "verknüpfen")));
        this.bot = bot;
    }

    @Override
    public void execute(ExecuteData data, String[] args) {
        final User sender = data.getAuthor().asUser().get();
        this.bot.getAuthAPI().isAlreadyAuthorized(sender.getIdAsString(), is -> {
            if (!is) {
                final String id = this.createId(6);
                this.bot.getAuthAPI().createAuthToken(sender.getIdAsString(), id);
                sender.sendMessage(new EmbedBuilder()
                        .setTitle("Minecraft-Account mit Discord-Server verknüpfen")
                        .setDescription("**1.** Joine auf den Server.\n**2.**Führe ingame den Befehl **/auth " + id + "** aus, um dich erfolgreich zu verifizieren.\n**3.** Du bist erfolgreich verknüpft.")
                        .setColor(Color.CYAN));
            }
        });
    }

    private String createId(final int i) {
        final String chars = "1234567890";
        final StringBuilder stringBuilder = new StringBuilder();
        final Random rnd = new Random();
        while (stringBuilder.length() < i) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

}
