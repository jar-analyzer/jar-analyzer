/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

import java.util.Objects;

public final class PlayingCard implements Comparable<PlayingCard> {
    public enum Suit {
        SPADE("♠", false), HEART("♥", true),
        CLUB("♣", false), DIAMOND("♦", true), JOKER("", false);

        private final String symbol;
        private final boolean red;

        Suit(String symbol, boolean red) {
            this.symbol = symbol;
            this.red = red;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean isRed() {
            return red;
        }
    }

    private final int id;
    private final Suit suit;
    private final int rank;

    public PlayingCard(int id, Suit suit, int rank) {
        this.id = id;
        this.suit = Objects.requireNonNull(suit);
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public String getRankText() {
        switch (rank) {
            case 11:
                return "J";
            case 12:
                return "Q";
            case 13:
                return "K";
            case 14:
                return "A";
            case 15:
                return "2";
            case 16:
                return "小王";
            case 17:
                return "大王";
            default:
                return String.valueOf(rank);
        }
    }

    @Override
    public int compareTo(PlayingCard other) {
        int byRank = Integer.compare(rank, other.rank);
        return byRank != 0 ? byRank : Integer.compare(suit.ordinal(), other.suit.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayingCard && ((PlayingCard) obj).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return suit.getSymbol() + getRankText();
    }
}
