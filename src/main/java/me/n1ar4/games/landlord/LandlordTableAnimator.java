/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

/**
 * Small, thread-safe presentation clock for the initial deal animation.
 */
final class LandlordTableAnimator {
    static final int HAND_CARDS = 51;
    static final int TOTAL_CARDS = 54;
    private static final double CARD_INTERVAL_SECONDS = .065;

    private int completedCards;
    private double cardElapsed;
    private boolean dealing = true;

    synchronized void restart() {
        completedCards = 0;
        cardElapsed = 0;
        dealing = true;
    }

    /**
     * @return true only on the update that completes the deal.
     */
    synchronized boolean update(double deltaSeconds) {
        if (!dealing) {
            return false;
        }
        cardElapsed += Math.max(0, deltaSeconds);
        while (cardElapsed >= CARD_INTERVAL_SECONDS && completedCards < TOTAL_CARDS) {
            cardElapsed -= CARD_INTERVAL_SECONDS;
            completedCards++;
        }
        if (completedCards >= TOTAL_CARDS) {
            completedCards = TOTAL_CARDS;
            cardElapsed = 0;
            dealing = false;
            return true;
        }
        return false;
    }

    synchronized boolean isDealing() {
        return dealing;
    }

    synchronized int visibleHandCards(int player) {
        int handCards = Math.min(completedCards, HAND_CARDS);
        if (handCards <= player) {
            return 0;
        }
        return (handCards - 1 - player) / 3 + 1;
    }

    synchronized int visibleBottomCards() {
        return Math.max(0, completedCards - HAND_CARDS);
    }

    synchronized DealFrame frame() {
        if (!dealing) {
            return new DealFrame(-1, 1);
        }
        int target = completedCards < HAND_CARDS ? completedCards % 3 : 3;
        return new DealFrame(target, cardElapsed / CARD_INTERVAL_SECONDS);
    }

    static final class DealFrame {
        private final int target;
        private final double progress;

        private DealFrame(int target, double progress) {
            this.target = target;
            this.progress = progress;
        }

        int getTarget() {
            return target;
        }

        double getProgress() {
            return progress;
        }
    }
}
