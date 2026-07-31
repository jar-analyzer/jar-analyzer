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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Set<Thread> workers = ConcurrentHashMap.newKeySet();

    protected GameFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void launch(Supplier<? extends GameFrame> factory) {
        Runnable createWindow = factory::get;
        if (SwingUtilities.isEventDispatchThread()) {
            createWindow.run();
        } else {
            SwingUtilities.invokeLater(createWindow);
        }
    }

    public final boolean isGameRunning() {
        return running.get();
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
        workers.add(worker);
        if (running.get()) {
            worker.start();
        } else {
            workers.remove(worker);
        }
        return worker;
    }

    @Override
    public final void dispose() {
        stopGame();
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
}
