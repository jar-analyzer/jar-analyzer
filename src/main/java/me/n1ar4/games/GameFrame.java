/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.games;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * Common lifecycle for the small games bundled with Jar Analyzer.
 *
 * <p>The Swing window is created on the EDT while game loops run as managed
 * daemon workers. Disposing the frame interrupts every worker so a closed
 * game cannot continue consuming CPU or keep the application alive.</p>
 */
public abstract class GameFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final ConcurrentHashMap<String, GameFrame> OPEN_GAMES =
            new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Set<Thread> workers = ConcurrentHashMap.newKeySet();
    private volatile String gameId;

    protected GameFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void launch(String gameId, Supplier<? extends GameFrame> factory) {
        Runnable createWindow = () -> {
            GameFrame existing = OPEN_GAMES.get(gameId);
            if (existing != null && existing.isDisplayable()) {
                existing.setState(Frame.NORMAL);
                existing.toFront();
                existing.requestFocus();
                return;
            }
            GameFrame frame = factory.get();
            frame.gameId = gameId;
            OPEN_GAMES.put(gameId, frame);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            createWindow.run();
        } else {
            SwingUtilities.invokeLater(createWindow);
        }
    }

    /**
     * Backward-compatible launcher for callers that do not need de-duplication.
     */
    public static void launch(Supplier<? extends GameFrame> factory) {
        launch(factory.getClass().getName(), factory);
    }

    public final boolean isGameRunning() {
        return running.get();
    }

    /**
     * Games automatically idle while minimized, hidden or out of focus. This
     * keeps them cheap enough to leave open while Jar Analyzer builds its DB.
     */
    public final boolean isGameActive() {
        return running.get() && isShowing() && isActive()
                && (getExtendedState() & Frame.ICONIFIED) == 0;
    }

    protected final Thread startGameWorker(String name, Runnable task) {
        Thread worker = new Thread(() -> {
            try {
                if (running.get()) {
                    task.run();
                }
            } finally {
                workers.remove(Thread.currentThread());
            }
        }, name);
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        workers.add(worker);
        if (running.get()) {
            worker.start();
        } else {
            workers.remove(worker);
        }
        return worker;
    }

    /**
     * Starts a fixed-rate, low-priority game loop. The loop automatically
     * sleeps while the game window is not active and clamps large frame gaps.
     */
    protected final Thread startGameLoop(String name, int framesPerSecond,
                                         DoubleConsumer tick) {
        if (framesPerSecond < 1 || framesPerSecond > 120) {
            throw new IllegalArgumentException("framesPerSecond must be between 1 and 120");
        }
        final long frameNanos = 1_000_000_000L / framesPerSecond;
        return startGameWorker(name, () -> {
            long previous = System.nanoTime();
            while (running.get()) {
                if (!isGameActive()) {
                    if (!sleepMillis(100)) {
                        break;
                    }
                    previous = System.nanoTime();
                    continue;
                }

                long frameStart = System.nanoTime();
                double deltaSeconds = Math.min(0.05,
                        (frameStart - previous) / 1_000_000_000.0);
                previous = frameStart;
                tick.accept(deltaSeconds);
                repaint();

                long remaining = frameNanos - (System.nanoTime() - frameStart);
                if (remaining > 0 && !sleepNanos(remaining)) {
                    break;
                }
            }
        });
    }

    @Override
    public final void dispose() {
        stopGame();
        if (gameId != null) {
            OPEN_GAMES.remove(gameId, this);
        }
        super.dispose();
    }

    private void stopGame() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        onGameStop();
        for (Thread worker : workers) {
            worker.interrupt();
        }
        workers.clear();
    }

    /**
     * Optional hook for game-specific flags that should be released before
     * managed workers are interrupted.
     */
    protected void onGameStop() {
    }

    private static boolean sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static boolean sleepNanos(long nanos) {
        long millis = nanos / 1_000_000L;
        int extraNanos = (int) (nanos % 1_000_000L);
        try {
            Thread.sleep(millis, extraNanos);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
