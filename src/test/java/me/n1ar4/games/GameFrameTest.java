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

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class GameFrameTest {
    @Test
    void disposingFrameStopsAndInterruptsManagedWorkers() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());

        TestGameFrame[] holder = new TestGameFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TestGameFrame());
        TestGameFrame frame = holder[0];

        Thread worker = frame.startWorker();
        assertTrue(frame.started.await(2, TimeUnit.SECONDS));
        assertTrue(frame.isGameRunning());
        assertTrue(worker.isDaemon());
        assertEquals(Thread.MIN_PRIORITY, worker.getPriority());

        SwingUtilities.invokeAndWait(frame::dispose);

        assertFalse(frame.isGameRunning());
        assertTrue(frame.stopped.await(2, TimeUnit.SECONDS));
    }

    @Test
    void managedLoopIdlesWhileHiddenAndTerminatesOnDispose() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());

        TestGameFrame[] holder = new TestGameFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TestGameFrame());
        TestGameFrame frame = holder[0];
        AtomicInteger ticks = new AtomicInteger();
        Thread loop = frame.startLoop(ticks);

        Thread.sleep(250);
        assertEquals(0, ticks.get());
        SwingUtilities.invokeAndWait(frame::dispose);
        loop.join(2000);
        assertFalse(loop.isAlive());
    }

    private static final class TestGameFrame extends GameFrame {
        private static final long serialVersionUID = 1L;
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch stopped = new CountDownLatch(1);

        private Thread startWorker() {
            return startGameWorker("test-game-worker", () -> {
                started.countDown();
                try {
                    while (isGameRunning()) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    stopped.countDown();
                }
            });
        }

        private Thread startLoop(AtomicInteger ticks) {
            return startGameLoop("test-game-loop", 60,
                    ignored -> ticks.incrementAndGet());
        }
    }
}
