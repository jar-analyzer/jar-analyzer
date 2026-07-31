/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

import me.n1ar4.games.GameFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Rewritten Fight the Landlord with vector cards and a testable rule model. */
public final class PokerGameFrame extends GameFrame {
    private static final long serialVersionUID = 1L;
    private final LandlordGame game = new LandlordGame();
    private final LandlordTableAnimator animator = new LandlordTableAnimator();
    private final PokerTable table = new PokerTable();
    private final Set<Integer> selectedIds = new LinkedHashSet<>();
    private final JButton bidButton = button("抢地主");
    private final JButton noBidButton = button("不抢");
    private final JButton playButton = button("出牌");
    private final JButton passButton = button("不出");
    private final JButton restartButton = button("重新开局");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private volatile double aiElapsed;
    private volatile int thinkingPlayer = -1;
    private volatile int actionPlayer = -1;
    private volatile String actionText = "";
    private volatile long actionStartedAt;
    private volatile long actionVisibleUntil;

    public PokerGameFrame() {
        setTitle("斗地主 / Open Landlord");
        JPanel root = new JPanel(new BorderLayout());
        root.add(table, BorderLayout.CENTER);
        root.add(createControls(), BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        installActions();
        refreshControls();
        setVisible(true);
        startGameLoop("landlord-ai-loop", 30, this::tick);
    }

    private JPanel createControls() {
        JPanel controls = new JPanel(new BorderLayout(8, 3));
        controls.setBorder(new EmptyBorder(5, 12, 7, 12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        actions.add(bidButton);
        actions.add(noBidButton);
        actions.add(playButton);
        actions.add(passButton);
        actions.add(restartButton);
        statusLabel.setPreferredSize(new Dimension(700, 25));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        controls.add(statusLabel, BorderLayout.NORTH);
        controls.add(actions, BorderLayout.CENTER);
        return controls;
    }

    private void installActions() {
        bidButton.addActionListener(e -> {
            afterHumanAction(game.humanBid(true));
        });
        noBidButton.addActionListener(e -> {
            afterHumanAction(game.humanBid(false));
        });
        playButton.addActionListener(e -> {
            boolean played = game.playHumanCards(new HashSet<>(selectedIds));
            if (played) {
                selectedIds.clear();
            }
            afterHumanAction(played);
        });
        passButton.addActionListener(e -> {
            boolean passed = game.humanPass();
            if (passed) {
                selectedIds.clear();
            }
            afterHumanAction(passed);
        });
        restartButton.addActionListener(e -> restartRound());
        bind("ENTER", "landlord-play", () -> playButton.doClick());
        bind("P", "landlord-pass", () -> passButton.doClick());
        bind("R", "landlord-restart", () -> restartButton.doClick());
    }

    private void bind(String key, String name, Runnable action) {
        table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), name);
        table.getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void tick(double deltaSeconds) {
        if (animator.isDealing()) {
            thinkingPlayer = -1;
            aiElapsed = 0;
            if (animator.update(deltaSeconds)) {
                SwingUtilities.invokeLater(this::refreshControls);
            }
            return;
        }

        LandlordGame.Snapshot snapshot = game.snapshot();
        if (snapshot.getState() == LandlordGame.State.GAME_OVER
                || snapshot.getCurrentPlayer() == LandlordGame.HUMAN) {
            thinkingPlayer = -1;
            aiElapsed = 0;
            return;
        }
        thinkingPlayer = snapshot.getCurrentPlayer();
        aiElapsed += deltaSeconds;
        double thinkTime = snapshot.getState() == LandlordGame.State.BIDDING ? 1.15 : 1.4;
        if (aiElapsed >= thinkTime) {
            aiElapsed = 0;
            if (game.performAiStep()) {
                showLatestAction();
                thinkingPlayer = -1;
                SwingUtilities.invokeLater(this::refreshControls);
            }
        }
    }

    private void afterHumanAction(boolean successful) {
        aiElapsed = 0;
        if (successful) {
            showLatestAction();
        }
        refreshControls();
        table.repaint();
    }

    private void restartRound() {
        game.restart();
        animator.restart();
        selectedIds.clear();
        aiElapsed = 0;
        thinkingPlayer = -1;
        actionPlayer = -1;
        actionText = "";
        actionStartedAt = 0;
        actionVisibleUntil = 0;
        refreshControls();
    }

    private void showLatestAction() {
        LandlordGame.Snapshot snapshot = game.snapshot();
        actionPlayer = snapshot.getLastActionPlayer();
        actionText = snapshot.getLastActionText();
        actionStartedAt = System.nanoTime();
        actionVisibleUntil = actionStartedAt + 1_450_000_000L;
    }

    private void refreshControls() {
        LandlordGame.Snapshot snapshot = game.snapshot();
        boolean dealFinished = !animator.isDealing();
        boolean humanBid = dealFinished && snapshot.getState() == LandlordGame.State.BIDDING
                && snapshot.getCurrentPlayer() == LandlordGame.HUMAN;
        boolean humanPlay = dealFinished && snapshot.getState() == LandlordGame.State.PLAYING
                && snapshot.getCurrentPlayer() == LandlordGame.HUMAN;
        bidButton.setVisible(humanBid);
        noBidButton.setVisible(humanBid);
        playButton.setVisible(dealFinished && snapshot.getState() == LandlordGame.State.PLAYING);
        passButton.setVisible(dealFinished && snapshot.getState() == LandlordGame.State.PLAYING);
        bidButton.setEnabled(humanBid);
        noBidButton.setEnabled(humanBid);
        playButton.setEnabled(humanPlay);
        passButton.setEnabled(humanPlay && snapshot.canHumanPass());
        restartButton.setVisible(true);
        String status = animator.isDealing() ? "正在发牌，请稍候…"
                : snapshot.getWinner() == null ? snapshot.getMessage()
                : snapshot.getWinner() + " · " + snapshot.getMessage();
        statusLabel.setText(status);

        Set<Integer> validIds = new HashSet<>();
        for (PlayingCard card : snapshot.getHumanHand()) {
            validIds.add(card.getId());
        }
        selectedIds.retainAll(validIds);
        table.repaint();
    }

    private static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(94, 32));
        return button;
    }

    private final class PokerTable extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int CARD_W = 72;
        private static final int CARD_H = 104;
        private final List<CardHit> handHits = new ArrayList<>();

        private PokerTable() {
            setPreferredSize(new Dimension(980, 600));
            setDoubleBuffered(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    toggleCardAt(e.getPoint());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            LandlordGame.Snapshot snapshot = game.snapshot();
            paintTable(g);
            paintOpponents(g, snapshot);
            paintBottomCards(g, snapshot);
            paintLastPlay(g, snapshot);
            paintHumanHand(g, snapshot);
            paintDealAnimation(g);
            paintPlayerFeedback(g, snapshot);
            paintTurnBanner(g, snapshot);
            g.dispose();
        }

        private void paintTable(Graphics2D g) {
            g.setPaint(new GradientPaint(0, 0, new Color(0x176A49),
                    0, getHeight(), new Color(0x0B3E31)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255, 18));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(180, 75, 620, 390);
            g.drawOval(245, 115, 490, 310);

            g.setColor(new Color(255, 255, 255, 105));
            g.setFont(getFont().deriveFont(Font.BOLD, 13f));
            g.drawString("三人经典场", 22, 30);
            g.setColor(new Color(255, 255, 255, 55));
            g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            g.drawString("基础牌 · 智能陪练", 22, 48);
        }

        private void paintOpponents(Graphics2D g, LandlordGame.Snapshot snapshot) {
            paintOpponent(g, 105, 145, LandlordGame.LEFT_AI, "电脑甲", snapshot);
            paintOpponent(g, 875, 145, LandlordGame.RIGHT_AI, "电脑乙", snapshot);
            paintPlayerBadge(g, 490, 438, LandlordGame.HUMAN, "你", snapshot);
        }

        private void paintOpponent(Graphics2D g, int centerX, int centerY, int player,
                                   String name, LandlordGame.Snapshot snapshot) {
            paintPlayerBadge(g, centerX, centerY, player, name, snapshot);
            int count = displayedCardCount(player, snapshot);
            int backs = Math.min(count, 12);
            int step = 5;
            int startX = centerX - CARD_W / 2 - (backs - 1) * step / 2;
            for (int i = 0; i < backs; i++) {
                drawCardBack(g, startX + i * step, centerY + 42, 54, 78);
            }
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 13f));
            g.drawString(count + " 张", centerX - 16, centerY + 137);
        }

        private void paintPlayerBadge(Graphics2D g, int x, int y, int player,
                                      String name, LandlordGame.Snapshot snapshot) {
            boolean active = !animator.isDealing() && snapshot.getCurrentPlayer() == player
                    && snapshot.getState() != LandlordGame.State.GAME_OVER;
            if (active) {
                g.setColor(new Color(255, 202, 85, 70));
                g.fillOval(x - 37, y - 37, 74, 74);
            }
            g.setColor(active ? new Color(0xFFCA55) : new Color(0xD7E5DF));
            g.fillOval(x - 30, y - 30, 60, 60);
            g.setColor(new Color(0x173B31));
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(name, x - metrics.stringWidth(name) / 2, y + 5);
            if (snapshot.getLandlord() == player) {
                g.setColor(new Color(0xC7463A));
                g.fillRoundRect(x - 23, y - 48, 46, 20, 10, 10);
                g.setColor(Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, 11f));
                g.drawString("地主", x - 11, y - 34);
            }
        }

        private void paintBottomCards(Graphics2D g, LandlordGame.Snapshot snapshot) {
            int startX = getWidth() / 2 - 70;
            int visible = animator.isDealing() ? animator.visibleBottomCards() : 3;
            for (int i = 0; i < visible; i++) {
                int x = startX + i * 50;
                if (animator.isDealing() || snapshot.getState() == LandlordGame.State.BIDDING) {
                    drawCardBack(g, x, 18, 44, 62);
                } else {
                    drawCard(g, snapshot.getBottomCards().get(i), x, 18, 44, 62, false);
                }
            }
        }

        private void paintLastPlay(Graphics2D g, LandlordGame.Snapshot snapshot) {
            if (animator.isDealing()) {
                return;
            }
            List<PlayingCard> cards = snapshot.getLastCards();
            if (cards.isEmpty()) {
                return;
            }
            int step = 36;
            int width = CARD_W + (cards.size() - 1) * step;
            int startX = (getWidth() - width) / 2;
            double flight = actionPlayer == snapshot.getLastPlayer()
                    ? Math.min(1, Math.max(0,
                    (System.nanoTime() - actionStartedAt) / 350_000_000.0)) : 1;
            double eased = 1 - Math.pow(1 - flight, 3);
            int sourceX = snapshot.getLastPlayer() == LandlordGame.LEFT_AI ? 80
                    : snapshot.getLastPlayer() == LandlordGame.RIGHT_AI ? getWidth() - 134
                    : getWidth() / 2 - CARD_W / 2;
            int sourceY = snapshot.getLastPlayer() == LandlordGame.HUMAN
                    ? getHeight() - 80 : 190;
            for (int i = 0; i < cards.size(); i++) {
                int finalX = startX + i * step;
                int x = (int) Math.round(sourceX + (finalX - sourceX) * eased);
                int y = (int) Math.round(sourceY + (245 - sourceY) * eased);
                drawCard(g, cards.get(i), x, y,
                        CARD_W, CARD_H, false);
            }
            String label = (snapshot.getLastPlayer() == 0 ? "你"
                    : snapshot.getLastPlayer() == 1 ? "电脑甲" : "电脑乙")
                    + " · " + snapshot.getLastCombination();
            g.setColor(new Color(255, 255, 255, 210));
            g.setFont(getFont().deriveFont(Font.BOLD, 13f));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(label, (getWidth() - metrics.stringWidth(label)) / 2, 235);
        }

        private void paintHumanHand(Graphics2D g, LandlordGame.Snapshot snapshot) {
            handHits.clear();
            List<PlayingCard> hand = snapshot.getHumanHand();
            if (animator.isDealing()) {
                int visible = Math.min(animator.visibleHandCards(LandlordGame.HUMAN), hand.size());
                hand = hand.subList(0, visible);
            }
            if (hand.isEmpty()) {
                return;
            }
            int step = hand.size() == 1 ? 0
                    : Math.min(48, (getWidth() - 90 - CARD_W) / (hand.size() - 1));
            int width = CARD_W + (hand.size() - 1) * step;
            int startX = (getWidth() - width) / 2;
            int baseY = getHeight() - CARD_H - 18;
            for (int i = 0; i < hand.size(); i++) {
                PlayingCard card = hand.get(i);
                int x = startX + i * step;
                int y = selectedIds.contains(card.getId()) ? baseY - 20 : baseY;
                drawCard(g, card, x, y, CARD_W, CARD_H, selectedIds.contains(card.getId()));
                handHits.add(new CardHit(card.getId(), new Rectangle(x, y, CARD_W, CARD_H)));
            }
        }

        private void paintTurnBanner(Graphics2D g, LandlordGame.Snapshot snapshot) {
            if (animator.isDealing()) {
                g.setColor(new Color(8, 13, 18, 175));
                g.fillRoundRect(385, 330, 210, 56, 18, 18);
                g.setColor(Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, 18f));
                String text = "正在发牌…";
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2, 365);
                return;
            }
            if (snapshot.getState() != LandlordGame.State.GAME_OVER) {
                return;
            }
            g.setColor(new Color(8, 13, 18, 215));
            g.fillRoundRect(315, 190, 350, 150, 24, 24);
            g.setColor(new Color(0xFFCF67));
            g.setFont(getFont().deriveFont(Font.BOLD, 31f));
            FontMetrics metrics = g.getFontMetrics();
            String winner = snapshot.getWinner();
            g.drawString(winner, (getWidth() - metrics.stringWidth(winner)) / 2, 255);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            String hint = "按 R 或点击重新开局";
            metrics = g.getFontMetrics();
            g.drawString(hint, (getWidth() - metrics.stringWidth(hint)) / 2, 294);
        }

        private int displayedCardCount(int player, LandlordGame.Snapshot snapshot) {
            return animator.isDealing() ? animator.visibleHandCards(player)
                    : snapshot.getCardCount(player);
        }

        private void paintDealAnimation(Graphics2D g) {
            if (!animator.isDealing()) {
                return;
            }
            int deckX = getWidth() / 2 - 27;
            int deckY = 156;
            for (int layer = 3; layer >= 0; layer--) {
                drawCardBack(g, deckX + layer * 2, deckY - layer * 2, 54, 78);
            }

            LandlordTableAnimator.DealFrame frame = animator.frame();
            int targetX;
            int targetY;
            switch (frame.getTarget()) {
                case LandlordGame.HUMAN:
                    targetX = getWidth() / 2 - 27;
                    targetY = getHeight() - 122;
                    break;
                case LandlordGame.LEFT_AI:
                    targetX = 80;
                    targetY = 190;
                    break;
                case LandlordGame.RIGHT_AI:
                    targetX = getWidth() - 134;
                    targetY = 190;
                    break;
                default:
                    targetX = getWidth() / 2 - 22;
                    targetY = 18;
                    break;
            }
            double progress = frame.getProgress();
            double eased = 1 - Math.pow(1 - progress, 3);
            int x = (int) Math.round(deckX + (targetX - deckX) * eased);
            int y = (int) Math.round(deckY + (targetY - deckY) * eased);
            drawCardBack(g, x, y, 54, 78);
        }

        private void paintPlayerFeedback(Graphics2D g, LandlordGame.Snapshot snapshot) {
            long now = System.nanoTime();
            if (actionPlayer >= 0 && now < actionVisibleUntil && !actionText.isEmpty()) {
                paintSpeechBubble(g, actionPlayer, actionText, new Color(0xFFF4CB));
            }
            if (!animator.isDealing() && thinkingPlayer >= 0
                    && snapshot.getState() != LandlordGame.State.GAME_OVER) {
                int dots = (int) ((System.currentTimeMillis() / 320) % 3) + 1;
                StringBuilder text = new StringBuilder("思考中");
                for (int i = 0; i < dots; i++) {
                    text.append('·');
                }
                paintSpeechBubble(g, thinkingPlayer, text.toString(), Color.WHITE);
            }
        }

        private void paintSpeechBubble(Graphics2D g, int player, String text, Color fill) {
            g.setFont(getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics metrics = g.getFontMetrics();
            int width = Math.max(76, metrics.stringWidth(text) + 24);
            int x;
            int y;
            if (player == LandlordGame.LEFT_AI) {
                x = 145;
                y = 100;
            } else if (player == LandlordGame.RIGHT_AI) {
                x = getWidth() - 145 - width;
                y = 100;
            } else {
                x = (getWidth() - width) / 2;
                y = 382;
            }
            g.setColor(new Color(0, 0, 0, 45));
            g.fillRoundRect(x + 3, y + 4, width, 36, 15, 15);
            g.setColor(fill);
            g.fillRoundRect(x, y, width, 36, 15, 15);
            g.setColor(new Color(0x24352F));
            g.drawString(text, x + (width - metrics.stringWidth(text)) / 2, y + 23);
        }

        private void drawCard(Graphics2D g, PlayingCard card, int x, int y,
                              int width, int height, boolean selected) {
            Shape shape = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
            g.setColor(selected ? new Color(0xFFF4C7) : new Color(0xFAFBFD));
            g.fill(shape);
            g.setColor(selected ? new Color(0xFFB020) : new Color(0xB8C0C8));
            g.setStroke(new BasicStroke(selected ? 2.3f : 1.2f));
            g.draw(shape);

            boolean joker = card.getSuit() == PlayingCard.Suit.JOKER;
            Color ink = joker && card.getRank() == 17 || card.getSuit().isRed()
                    ? new Color(0xD63F4B) : new Color(0x20242B);
            g.setColor(ink);
            int fontSize = Math.max(12, width / 4);
            g.setFont(getFont().deriveFont(Font.BOLD, (float) fontSize));
            String rank = card.getRankText();
            g.drawString(rank, x + 7, y + fontSize + 4);
            if (!joker) {
                g.setFont(getFont().deriveFont(Font.PLAIN, (float) Math.max(14, width / 3)));
                g.drawString(card.getSuit().getSymbol(), x + 7, y + fontSize + width / 3 + 5);
                g.setFont(getFont().deriveFont(Font.PLAIN, (float) Math.max(25, width / 2)));
                FontMetrics metrics = g.getFontMetrics();
                String symbol = card.getSuit().getSymbol();
                g.drawString(symbol, x + (width - metrics.stringWidth(symbol)) / 2,
                        y + height - 18);
            } else {
                g.setFont(getFont().deriveFont(Font.BOLD, (float) Math.max(14, width / 4)));
                String jokerText = card.getRank() == 17 ? "JOKER" : "joker";
                g.drawString(jokerText, x + 7, y + height - 20);
            }
        }

        private void drawCardBack(Graphics2D g, int x, int y, int width, int height) {
            Shape previousClip = g.getClip();
            Shape outer = new RoundRectangle2D.Double(x, y, width, height, 9, 9);
            Shape inner = new RoundRectangle2D.Double(
                    x + 4, y + 4, width - 8, height - 8, 7, 7);
            g.setColor(new Color(0xE8EDF3));
            g.fill(outer);
            g.setColor(new Color(0x285BA8));
            g.fill(inner);
            g.clip(inner);
            g.setColor(new Color(255, 255, 255, 85));
            for (int offset = -height; offset < width; offset += 12) {
                g.drawLine(x + offset, y + height - 5, x + offset + height, y + 5);
            }
            g.setClip(previousClip);
            g.setColor(new Color(0xDCE5EF));
            g.setStroke(new BasicStroke(1.1f));
            g.draw(outer);
        }

        private void toggleCardAt(Point point) {
            LandlordGame.Snapshot snapshot = game.snapshot();
            if (animator.isDealing() || snapshot.getState() != LandlordGame.State.PLAYING
                    || snapshot.getCurrentPlayer() != LandlordGame.HUMAN) {
                return;
            }
            for (int i = handHits.size() - 1; i >= 0; i--) {
                CardHit hit = handHits.get(i);
                if (hit.bounds.contains(point)) {
                    if (!selectedIds.add(hit.cardId)) {
                        selectedIds.remove(hit.cardId);
                    }
                    repaint();
                    return;
                }
            }
        }
    }

    private static final class CardHit {
        private final int cardId;
        private final Rectangle bounds;

        private CardHit(int cardId, Rectangle bounds) {
            this.cardId = cardId;
            this.bounds = bounds;
        }
    }
}
