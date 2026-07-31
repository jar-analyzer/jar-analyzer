/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

import java.util.*;

public final class LandlordRules {
    private LandlordRules() {
    }

    public static Combination evaluate(Collection<PlayingCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        TreeMap<Integer, Integer> counts = counts(cards);
        int size = cards.size();

        if (size == 2 && counts.containsKey(16) && counts.containsKey(17)) {
            return combo(Combination.Type.ROCKET, 17, size);
        }
        if (size == 4 && hasCount(counts, 4)) {
            return combo(Combination.Type.BOMB, rankWithCount(counts, 4), size);
        }
        if (size == 1) {
            return combo(Combination.Type.SINGLE, counts.firstKey(), size);
        }
        if (size == 2 && counts.size() == 1) {
            return combo(Combination.Type.PAIR, counts.firstKey(), size);
        }
        if (size == 3 && counts.size() == 1) {
            return combo(Combination.Type.TRIPLE, counts.firstKey(), size);
        }
        if (size == 4 && hasCount(counts, 3)) {
            return combo(Combination.Type.TRIPLE_SINGLE, rankWithCount(counts, 3), size);
        }
        if (size == 5 && hasCount(counts, 3) && hasCount(counts, 2)) {
            return combo(Combination.Type.TRIPLE_PAIR, rankWithCount(counts, 3), size);
        }
        if (size >= 5 && allCounts(counts, 1) && consecutive(counts.keySet(), size)) {
            return combo(Combination.Type.STRAIGHT, counts.lastKey(), size);
        }
        if (size >= 6 && size % 2 == 0 && allCounts(counts, 2)
                && consecutive(counts.keySet(), size / 2)) {
            return combo(Combination.Type.PAIR_STRAIGHT, counts.lastKey(), size);
        }
        Combination airplane = airplane(counts, size);
        if (airplane != null) {
            return airplane;
        }
        if (size == 6 && hasCount(counts, 4)) {
            return combo(Combination.Type.FOUR_TWO_SINGLE, rankWithCount(counts, 4), size);
        }
        if (size == 8 && hasCount(counts, 4)) {
            int four = rankWithCount(counts, 4);
            TreeMap<Integer, Integer> rest = new TreeMap<>(counts);
            rest.remove(four);
            if (rest.size() == 2 && allCounts(rest, 2)) {
                return combo(Combination.Type.FOUR_TWO_PAIR, four, size);
            }
        }
        return null;
    }

    static TreeMap<Integer, List<PlayingCard>> group(List<PlayingCard> hand) {
        TreeMap<Integer, List<PlayingCard>> groups = new TreeMap<>();
        for (PlayingCard card : hand) {
            groups.computeIfAbsent(card.getRank(), ignored -> new ArrayList<>()).add(card);
        }
        return groups;
    }

    private static Combination airplane(TreeMap<Integer, Integer> counts, int size) {
        int[] widths = {3, 4, 5};
        Combination.Type[] types = {
                Combination.Type.AIRPLANE,
                Combination.Type.AIRPLANE_SINGLE,
                Combination.Type.AIRPLANE_PAIR
        };
        for (int mode = 0; mode < widths.length; mode++) {
            int width = widths[mode];
            if (size % width != 0 || size / width < 2) {
                continue;
            }
            int chainLength = size / width;
            List<Integer> tripleRanks = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getKey() <= 14 && entry.getValue() >= 3) {
                    tripleRanks.add(entry.getKey());
                }
            }
            for (int start = 0; start + chainLength <= tripleRanks.size(); start++) {
                List<Integer> chain = tripleRanks.subList(start, start + chainLength);
                if (!consecutive(chain, chainLength)) {
                    continue;
                }
                TreeMap<Integer, Integer> rest = new TreeMap<>(counts);
                for (Integer rank : chain) {
                    int remaining = rest.get(rank) - 3;
                    if (remaining == 0) {
                        rest.remove(rank);
                    } else {
                        rest.put(rank, remaining);
                    }
                }
                int restCards = rest.values().stream().mapToInt(Integer::intValue).sum();
                boolean valid = mode == 0 && restCards == 0;
                valid |= mode == 1 && restCards == chainLength;
                valid |= mode == 2 && rest.size() == chainLength && allCounts(rest, 2);
                if (valid) {
                    return combo(types[mode], chain.get(chain.size() - 1), size);
                }
            }
        }
        return null;
    }

    private static TreeMap<Integer, Integer> counts(Collection<PlayingCard> cards) {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        for (PlayingCard card : cards) {
            result.merge(card.getRank(), 1, Integer::sum);
        }
        return result;
    }

    private static boolean consecutive(Collection<Integer> ranks, int expectedSize) {
        if (ranks.size() != expectedSize) {
            return false;
        }
        int previous = -1;
        for (Integer rank : ranks) {
            if (rank > 14 || previous != -1 && rank != previous + 1) {
                return false;
            }
            previous = rank;
        }
        return true;
    }

    private static boolean hasCount(Map<Integer, Integer> counts, int target) {
        return counts.containsValue(target);
    }

    private static int rankWithCount(Map<Integer, Integer> counts, int target) {
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == target) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private static boolean allCounts(Map<Integer, Integer> counts, int target) {
        for (Integer count : counts.values()) {
            if (count != target) {
                return false;
            }
        }
        return true;
    }

    private static Combination combo(Combination.Type type, int rank, int size) {
        return new Combination(type, rank, size);
    }
}
