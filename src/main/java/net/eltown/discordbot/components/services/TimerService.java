package net.eltown.discordbot.components.services;

import lombok.RequiredArgsConstructor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class TimerService {

    private final ExecutorService executor;
    private final Timer timer = new Timer("Timer-1");

    public void run(final Runnable runnable, final long time) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executor.submit(runnable);
            }
        }, time);
    }

}
