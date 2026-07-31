/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

public final class Combination {
    public enum Type {
        SINGLE("单张"), PAIR("对子"), TRIPLE("三张"),
        TRIPLE_SINGLE("三带一"), TRIPLE_PAIR("三带二"),
        STRAIGHT("顺子"), PAIR_STRAIGHT("连对"), AIRPLANE("飞机"),
        AIRPLANE_SINGLE("飞机带单"), AIRPLANE_PAIR("飞机带对"),
        FOUR_TWO_SINGLE("四带二"), FOUR_TWO_PAIR("四带两对"),
        BOMB("炸弹"), ROCKET("王炸");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Type type;
    private final int mainRank;
    private final int cardCount;

    Combination(Type type, int mainRank, int cardCount) {
        this.type = type;
        this.mainRank = mainRank;
        this.cardCount = cardCount;
    }

    public Type getType() {
        return type;
    }

    public int getMainRank() {
        return mainRank;
    }

    public int getCardCount() {
        return cardCount;
    }

    public boolean beats(Combination previous) {
        if (previous == null) {
            return true;
        }
        if (type == Type.ROCKET) {
            return previous.type != Type.ROCKET;
        }
        if (previous.type == Type.ROCKET) {
            return false;
        }
        if (type == Type.BOMB && previous.type != Type.BOMB) {
            return true;
        }
        if (type != previous.type || cardCount != previous.cardCount) {
            return false;
        }
        return mainRank > previous.mainRank;
    }

    @Override
    public String toString() {
        return type.getDisplayName();
    }
}
