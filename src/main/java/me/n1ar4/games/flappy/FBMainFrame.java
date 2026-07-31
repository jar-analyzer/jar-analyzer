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
import me.n1ar4.games.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

/**
 * Rewritten Flappy Bird: pure model, vector UI and managed game loop.
 */
public final class FBMainFrame extends GameFrame {
    private static final long serialVersionUID = 1L;
    private final FlappyGame game = new FlappyGame();
    private final FlappyPanel panel = new FlappyPanel();

    public FBMainFrame() {
        setTitle("Flappy Bird · Open Edition");
        setContentPane(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        installControls();
        setVisible(true);
        startGameLoop("flappy-loop", 60, game::update);
    }

    private void installControls() {
        bind("SPACE", "flap-space", game::flap);
        bind("ENTER", "flap-enter", game::flap);
        bind("P", "flap-pause", game::togglePause);
        bind("R", "flap-restart", game::restart);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                game.flap();
                panel.repaint();
            }
        });
    }

    private void bind(String key, String name, Runnable action) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), name);
        panel.getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
                panel.repaint();
            }
        });
    }

    private final class FlappyPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final FlatSVGIcon bird = new FlatSVGIcon(
                "svg/game/flappy/bird.svg", FlappyGame.BIRD_WIDTH, FlappyGame.BIRD_HEIGHT);

        private FlappyPanel() {
            setPreferredSize(new Dimension(FlappyGame.WIDTH, FlappyGame.HEIGHT));
            setDoubleBuffered(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FlappyGame.Snapshot snapshot = game.snapshot();
            paintSky(g);
            paintPipes(g, snapshot);
            paintGround(g, snapshot);
            paintBird(g, snapshot);
            paintScore(g, snapshot);
            paintOverlay(g, snapshot);
            g.dispose();
        }

        private void paintSky(Graphics2D g) {
            GradientPaint sky = new GradientPaint(0, 0, new Color(0x78C9F4),
                    0, FlappyGame.GROUND_Y, new Color(0xDDF5F4));
            g.setPaint(sky);
            g.fillRect(0, 0, getWidth(), FlappyGame.GROUND_Y);
            g.setColor(new Color(255, 255, 255, 155));
            paintCloud(g, 35, 92, 1.0);
            paintCloud(g, 235, 150, .8);
            paintCloud(g, 120, 255, .65);
        }

        private void paintCloud(Graphics2D g, int x, int y, double scale) {
            int w = (int) (74 * scale);
            int h = (int) (25 * scale);
            g.fillOval(x, y, w, h);
            g.fillOval(x + w / 5, y - h / 2, w / 2, h);
        }

        private void paintPipes(Graphics2D g, FlappyGame.Snapshot snapshot) {
            for (FlappyGame.PipeView pipe : snapshot.getPipes()) {
                paintPipe(g, pipe.getX(), 0, pipe.getGapTop(), false);
                int lowerY = pipe.getGapTop() + pipe.getGapHeight();
                paintPipe(g, pipe.getX(), lowerY,
                        FlappyGame.GROUND_Y - lowerY, true);
            }
        }

        private void paintPipe(Graphics2D g, int x, int y, int height, boolean lower) {
            if (height <= 0) {
                return;
            }
            GradientPaint pipe = new GradientPaint(x, 0, new Color(0x8EDC4B),
                    x + FlappyGame.PIPE_WIDTH, 0, new Color(0x3A9C35));
            g.setPaint(pipe);
            g.fillRoundRect(x + 6, y, FlappyGame.PIPE_WIDTH - 12, height, 8, 8);
            int capY = lower ? y : Math.max(0, y + height - 24);
            g.fillRoundRect(x, capY, FlappyGame.PIPE_WIDTH, 24, 7, 7);
            g.setColor(new Color(0x26752C));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(x + 6, y, FlappyGame.PIPE_WIDTH - 12, height, 8, 8);
            g.drawRoundRect(x, capY, FlappyGame.PIPE_WIDTH, 24, 7, 7);
        }

        private void paintGround(Graphics2D g, FlappyGame.Snapshot snapshot) {
            g.setColor(new Color(0x78B84A));
            g.fillRect(0, FlappyGame.GROUND_Y, getWidth(), 14);
            g.setColor(new Color(0xE8D58B));
            g.fillRect(0, FlappyGame.GROUND_Y + 14, getWidth(),
                    getHeight() - FlappyGame.GROUND_Y - 14);
            g.setColor(new Color(0xC6A95C));
            for (int x = snapshot.getGroundOffset() - 32; x < getWidth(); x += 32) {
                g.fillRect(x, FlappyGame.GROUND_Y + 21, 17, 5);
            }
        }

        private void paintBird(Graphics2D g, FlappyGame.Snapshot snapshot) {
            double angle = Math.max(-0.35, Math.min(0.65,
                    snapshot.getBirdVelocity() / 620.0));
            AffineTransform old = g.getTransform();
            g.rotate(angle, FlappyGame.BIRD_X + FlappyGame.BIRD_WIDTH / 2.0,
                    snapshot.getBirdY() + FlappyGame.BIRD_HEIGHT / 2.0);
            bird.paintIcon(this, g, FlappyGame.BIRD_X, snapshot.getBirdY());
            g.setTransform(old);
        }

        private void paintScore(Graphics2D g, FlappyGame.Snapshot snapshot) {
            g.setFont(getFont().deriveFont(Font.BOLD, 38f));
            String value = String.valueOf(snapshot.getScore());
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(value)) / 2;
            g.setColor(new Color(0, 0, 0, 75));
            g.drawString(value, x + 2, 65 + 2);
            g.setColor(Color.WHITE);
            g.drawString(value, x, 65);
        }

        private void paintOverlay(Graphics2D g, FlappyGame.Snapshot snapshot) {
            String title = null;
            String hint = null;
            if (snapshot.getState() == FlappyGame.State.READY) {
                title = "FLAPPY BIRD";
                hint = "点击 / SPACE 开始飞行";
            } else if (snapshot.getState() == FlappyGame.State.PAUSED) {
                title = "已暂停";
                hint = "P 或 SPACE 继续";
            } else if (snapshot.getState() == FlappyGame.State.GAME_OVER) {
                title = "撞到了";
                hint = "得分 " + snapshot.getScore() + "  ·  最佳 "
                        + snapshot.getBestScore() + "  ·  R 重开";
            }
            if (title == null) {
                return;
            }
            g.setColor(new Color(15, 25, 35, 205));
            g.fillRoundRect(30, 205, 300, 130, 22, 22);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 26f));
            FontMetrics titleMetrics = g.getFontMetrics();
            g.drawString(title, (getWidth() - titleMetrics.stringWidth(title)) / 2, 258);
            g.setColor(new Color(0xC9D9E8));
            g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            FontMetrics hintMetrics = g.getFontMetrics();
            g.drawString(hint, (getWidth() - hintMetrics.stringWidth(hint)) / 2, 298);
        }
    }
}
