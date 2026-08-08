/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.games.flappy;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlappyGameTest {
    @Test
    void flapStartsGameAndPauseFreezesPhysics() {
        FlappyGame game = new FlappyGame(new Random(1));
        assertEquals(FlappyGame.State.READY, game.snapshot().getState());
        game.flap();
        game.update(.1);
        int movingY = game.snapshot().getBirdY();
        assertEquals(FlappyGame.State.RUNNING, game.snapshot().getState());

        game.togglePause();
        game.update(1);
        assertEquals(movingY, game.snapshot().getBirdY());
        assertEquals(FlappyGame.State.PAUSED, game.snapshot().getState());
    }

    @Test
    void pipeCollisionEndsGameAndRestartClearsRound() {
        FlappyGame game = new FlappyGame(new Random(2));
        game.flap();
        game.addPipe(FlappyGame.BIRD_X, 20, 80);
        game.update(.016);
        assertEquals(FlappyGame.State.GAME_OVER, game.snapshot().getState());

        game.restart();
        assertEquals(FlappyGame.State.RUNNING, game.snapshot().getState());
        assertEquals(0, game.snapshot().getScore());
        assertTrue(game.snapshot().getPipes().isEmpty());
    }

    @Test
    void originalBirdSvgRenders() {
        FlatSVGIcon icon = new FlatSVGIcon("svg/game/flappy/bird.svg", 40, 30);
        BufferedImage image = new BufferedImage(40, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();

        int painted = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    painted++;
                }
            }
        }
        assertTrue(painted > 100);
    }
}
