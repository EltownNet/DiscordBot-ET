package net.eltown.discordbot.components.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class Command {

    private final String name;
    private final Set<String> aliases;

    public void execute(final ExecuteData data, final String[] args) {
        data.getChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Oops!")
                        .setDescription("**This command is not implemented yet!**")
        );
    }

    @Getter
    public static final class ExecuteData {
        private final MessageCreateEvent event;
        private final TextChannel channel;
        private final Message message;
        private final MessageAuthor author;
        private final DiscordApi api;

        public ExecuteData(final MessageCreateEvent event) {
            this.event = event;
            this.channel = this.event.getChannel();
            this.message = this.event.getMessage();
            this.author = this.event.getMessageAuthor();
            this.api = this.event.getApi();
        }
    }

}
