package net.eltown.discordbot.components.messaging;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.discordbot.Bot;

@RequiredArgsConstructor
public class AuthListener {

    private final Bot bot;

    @SneakyThrows
    public void startListening() {
        this.bot.getListener().callback((request -> {
            switch (AuthCalls.valueOf(request.getKey().toUpperCase())) {
                case REQUEST_SOLVE_AUTH:
                    final String token = request.getData()[1];
                    final String ingameName = request.getData()[2];
                    if (this.bot.getAuthAPI().isTokenValid(token)) {
                        this.bot.getAuthAPI().isIngameNameAuthorized(ingameName, is1 -> {
                            if (!is1) {
                                this.bot.getAuthAPI().solveAuthentification(token, ingameName);
                                request.answer(AuthCalls.CALLBACK_NULL.name(), "null");
                            } else request.answer(AuthCalls.CALLBACK_ALREADY_AUTH.name(), "null");
                        });
                    } else request.answer(AuthCalls.CALLBACK_TOKEN_INVALID.name(), "null");
                    break;
            }
        }), "DiscordBot/AuthAPI/Listener", "discord.bot.auth");
    }

    public enum AuthCalls {

        REQUEST_SOLVE_AUTH,
        CALLBACK_NULL,
        CALLBACK_TOKEN_INVALID,
        CALLBACK_ALREADY_AUTH

    }

}
