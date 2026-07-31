package me.n1ar4.games;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class GameFrameTest {
    @Test
    void disposingFrameStopsAndInterruptsManagedWorkers() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());

        TestGameFrame[] holder = new TestGameFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TestGameFrame());
        TestGameFrame frame = holder[0];

        frame.startWorker();
        assertTrue(frame.started.await(2, TimeUnit.SECONDS));
        assertTrue(frame.isGameRunning());

        SwingUtilities.invokeAndWait(frame::dispose);

        assertFalse(frame.isGameRunning());
        assertTrue(frame.stopped.await(2, TimeUnit.SECONDS));
    }

    private static final class TestGameFrame extends GameFrame {
        private static final long serialVersionUID = 1L;
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch stopped = new CountDownLatch(1);

        private void startWorker() {
            startGameWorker("test-game-worker", () -> {
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
    }
}
