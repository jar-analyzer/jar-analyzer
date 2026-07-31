/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.games.racing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.n1ar4.games.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Original, asset-light lane racing game bundled with Jar Analyzer. */
public final class RacingGameFrame extends GameFrame {
    private static final long serialVersionUID = 1L;

    private final RacingGame game = new RacingGame();
    private final RacingPanel gamePanel = new RacingPanel();

    public RacingGameFrame() {
        setTitle("极速公路 / Open Racing");
        setContentPane(gamePanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        installControls();
        setVisible(true);
        startGameLoop("racing-game-loop", 60,
                seconds -> game.update(Math.max(1L, (long) (seconds * 1000))));
    }

    private void installControls() {
        bind("LEFT", "move-left", game::moveLeft);
        bind("A", "move-left-a", game::moveLeft);
        bind("RIGHT", "move-right", game::moveRight);
        bind("D", "move-right-d", game::moveRight);
        bind("ENTER", "start-race", game::startOrResume);
        bind("SPACE", "pause-race", game::togglePause);
        bind("R", "restart-race", game::restart);
    }

    private void bind(String keyStroke, String actionName, Runnable action) {
        InputMap inputMap = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionName);
        gamePanel.getActionMap().put(actionName, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
                gamePanel.repaint();
            }
        });
    }

    private final class RacingPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final FlatSVGIcon playerCar = new FlatSVGIcon(
                "svg/game/racing/playerCar.svg", RacingGame.CAR_WIDTH, RacingGame.CAR_HEIGHT);
        private final FlatSVGIcon enemyRed = new FlatSVGIcon(
                "svg/game/racing/enemyRed.svg", RacingGame.CAR_WIDTH, RacingGame.CAR_HEIGHT);
        private final FlatSVGIcon enemyYellow = new FlatSVGIcon(
                "svg/game/racing/enemyYellow.svg", RacingGame.CAR_WIDTH, RacingGame.CAR_HEIGHT);

        private RacingPanel() {
            setPreferredSize(new Dimension(RacingGame.WORLD_WIDTH, RacingGame.WORLD_HEIGHT));
            setDoubleBuffered(true);
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RacingGame.Snapshot snapshot = game.snapshot();

            paintRoad(g, snapshot);
            paintCars(g, snapshot);
            paintHud(g, snapshot);
            paintOverlay(g, snapshot);
            g.dispose();
        }

        private void paintRoad(Graphics2D g, RacingGame.Snapshot snapshot) {
            g.setColor(new Color(0x17442D));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(0xD7DCE2));
            g.fillRect(RacingGame.ROAD_LEFT - 8, 0, RacingGame.ROAD_WIDTH + 16, getHeight());
            g.setColor(new Color(0x30343B));
            g.fillRect(RacingGame.ROAD_LEFT, 0, RacingGame.ROAD_WIDTH, getHeight());

            g.setColor(new Color(0xF5F7FA));
            int laneWidth = RacingGame.ROAD_WIDTH / RacingGame.LANE_COUNT;
            for (int lane = 1; lane < RacingGame.LANE_COUNT; lane++) {
                int x = RacingGame.ROAD_LEFT + lane * laneWidth - 2;
                for (int y = snapshot.getRoadOffset() - 80; y < getHeight(); y += 80) {
                    g.fillRoundRect(x, y, 4, 42, 4, 4);
                }
            }

            g.setColor(new Color(0xE25555));
            for (int y = snapshot.getRoadOffset() - 36; y < getHeight(); y += 48) {
                g.fillRect(RacingGame.ROAD_LEFT - 8, y, 8, 24);
                g.fillRect(RacingGame.ROAD_LEFT + RacingGame.ROAD_WIDTH, y, 8, 24);
            }
        }

        private void paintCars(Graphics2D g, RacingGame.Snapshot snapshot) {
            for (RacingGame.Obstacle obstacle : snapshot.getObstacles()) {
                int x = RacingGame.laneX(obstacle.getLane());
                paintShadow(g, x, obstacle.getY());
                FlatSVGIcon icon = obstacle.getStyle() == 0 ? enemyRed : enemyYellow;
                icon.paintIcon(this, g, x, obstacle.getY());
            }
            int playerX = RacingGame.laneX(snapshot.getPlayerLane());
            paintShadow(g, playerX, RacingGame.PLAYER_Y);
            playerCar.paintIcon(this, g, playerX, RacingGame.PLAYER_Y);
        }

        private void paintShadow(Graphics2D g, int x, int y) {
            g.setColor(new Color(0, 0, 0, 55));
            g.fillRoundRect(x + 5, y + 8, RacingGame.CAR_WIDTH, RacingGame.CAR_HEIGHT, 14, 14);
        }

        private void paintHud(Graphics2D g, RacingGame.Snapshot snapshot) {
            g.setColor(new Color(12, 16, 22, 205));
            g.fillRoundRect(12, 12, 160, 62, 14, 14);
            g.fillRoundRect(282, 12, 126, 62, 14, 14);

            g.setFont(getFont().deriveFont(Font.BOLD, 18f));
            g.setColor(Color.WHITE);
            g.drawString(String.format("%06d", snapshot.getScore()), 27, 40);
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g.setColor(new Color(0xAFC2D8));
            g.drawString("SCORE   BEST " + snapshot.getBestScore(), 27, 60);

            g.setFont(getFont().deriveFont(Font.BOLD, 17f));
            g.setColor(new Color(0x80C6FF));
            g.drawString(snapshot.getSpeed() + " km/h", 299, 40);
            g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            g.setColor(new Color(0xAFC2D8));
            g.drawString("极速公路", 320, 59);

            if (snapshot.getState() == RacingGame.State.RUNNING) {
                String controls = "← → / A D 换道    SPACE 暂停    R 重开";
                g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
                FontMetrics metrics = g.getFontMetrics();
                int width = metrics.stringWidth(controls) + 24;
                int x = (getWidth() - width) / 2;
                g.setColor(new Color(12, 16, 22, 180));
                g.fillRoundRect(x, getHeight() - 34, width, 24, 12, 12);
                g.setColor(new Color(0xDDE8F3));
                g.drawString(controls, x + 12, getHeight() - 17);
            }
        }

        private void paintOverlay(Graphics2D g, RacingGame.Snapshot snapshot) {
            String title = null;
            String hint = null;
            if (snapshot.getState() == RacingGame.State.READY) {
                title = "极速公路";
                hint = "ENTER 开始  ·  ← → / A D 换道";
            } else if (snapshot.getState() == RacingGame.State.PAUSED) {
                title = "已暂停";
                hint = "SPACE 或 ENTER 继续";
            } else if (snapshot.getState() == RacingGame.State.GAME_OVER) {
                title = "发生碰撞";
                hint = "本局 " + snapshot.getScore() + " 分  ·  R 重新开始";
            }
            if (title == null) {
                return;
            }

            g.setColor(new Color(8, 11, 16, 205));
            g.fillRoundRect(54, 246, 312, 130, 22, 22);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 29f));
            FontMetrics titleMetrics = g.getFontMetrics();
            g.drawString(title, (getWidth() - titleMetrics.stringWidth(title)) / 2, 300);
            g.setColor(new Color(0xBFD0E2));
            g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            FontMetrics hintMetrics = g.getFontMetrics();
            g.drawString(hint, (getWidth() - hintMetrics.stringWidth(hint)) / 2, 339);
        }
    }
}
