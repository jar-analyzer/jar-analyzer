package me.n1ar4.games.racing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RacingGameTest {
    @Test
    void gameStartsMovesBetweenThreeLanesAndPauses() {
        RacingGame game = new RacingGame(new Random(1));
        game.update(1000);
        assertEquals(0, game.snapshot().getScore());

        game.startOrResume();
        game.moveLeft();
        game.moveLeft();
        assertEquals(0, game.snapshot().getPlayerLane());
        game.moveRight();
        game.moveRight();
        game.moveRight();
        assertEquals(2, game.snapshot().getPlayerLane());

        game.togglePause();
        int pausedScore = game.snapshot().getScore();
        game.update(1000);
        assertEquals(pausedScore, game.snapshot().getScore());
        assertEquals(RacingGame.State.PAUSED, game.snapshot().getState());
    }

    @Test
    void collisionEndsRaceAndRestartResetsCurrentScore() {
        RacingGame game = new RacingGame(new Random(2));
        game.startOrResume();
        game.update(500);
        int scoreBeforeCrash = game.snapshot().getScore();
        game.addObstacle(1, RacingGame.PLAYER_Y);
        game.update(16);

        assertEquals(RacingGame.State.GAME_OVER, game.snapshot().getState());
        assertTrue(game.snapshot().getBestScore() >= scoreBeforeCrash);

        int best = game.snapshot().getBestScore();
        game.restart();
        assertEquals(RacingGame.State.RUNNING, game.snapshot().getState());
        assertEquals(0, game.snapshot().getScore());
        assertEquals(best, game.snapshot().getBestScore());
        assertTrue(game.snapshot().getObstacles().isEmpty());
    }

    @Test
    void originalSvgCarsLoadAndRender() {
        String[] paths = {
                "svg/game/racing/playerCar.svg",
                "svg/game/racing/enemyRed.svg",
                "svg/game/racing/enemyYellow.svg",
                "svg/menu/racing.svg"
        };
        for (String path : paths) {
            int width = path.contains("menu") ? 16 : RacingGame.CAR_WIDTH;
            int height = path.contains("menu") ? 16 : RacingGame.CAR_HEIGHT;
            FlatSVGIcon icon = new FlatSVGIcon(path, width, height);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            icon.paintIcon(null, graphics, 0, 0);
            graphics.dispose();

            int painted = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((image.getRGB(x, y) >>> 24) != 0) {
                        painted++;
                    }
                }
            }
            assertTrue(painted > 20, path + " rendered no visible content");
        }
    }
}
