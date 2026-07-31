/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class LandlordAi {
    private LandlordAi() {
    }

    static List<PlayingCard> choose(List<PlayingCard> hand, Combination previous) {
        List<List<PlayingCard>> candidates = candidates(hand);
        candidates.removeIf(cards -> {
            Combination value = LandlordRules.evaluate(cards);
            return value == null || !value.beats(previous);
        });
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        candidates.sort((left, right) -> compareCandidate(left, right, previous));
        return candidates.get(0);
    }

    private static int compareCandidate(List<PlayingCard> left, List<PlayingCard> right,
                                        Combination previous) {
        Combination a = LandlordRules.evaluate(left);
        Combination b = LandlordRules.evaluate(right);
        if (previous == null) {
            boolean aPower = isPower(a);
            boolean bPower = isPower(b);
            if (aPower != bPower) {
                return aPower ? 1 : -1;
            }
            int bySize = Integer.compare(right.size(), left.size());
            if (bySize != 0) {
                return bySize;
            }
        } else {
            boolean aSame = a.getType() == previous.getType();
            boolean bSame = b.getType() == previous.getType();
            if (aSame != bSame) {
                return aSame ? -1 : 1;
            }
        }
        int byPower = Boolean.compare(isPower(a), isPower(b));
        return byPower != 0 ? byPower : Integer.compare(a.getMainRank(), b.getMainRank());
    }

    private static boolean isPower(Combination combination) {
        return combination.getType() == Combination.Type.BOMB
                || combination.getType() == Combination.Type.ROCKET;
    }

    private static List<List<PlayingCard>> candidates(List<PlayingCard> hand) {
        TreeMap<Integer, List<PlayingCard>> groups = LandlordRules.group(hand);
        List<List<PlayingCard>> result = new ArrayList<>();
        for (List<PlayingCard> group : groups.values()) {
            add(result, group, 1);
            if (group.size() >= 2) {
                add(result, group, 2);
            }
            if (group.size() >= 3) {
                add(result, group, 3);
            }
            if (group.size() == 4) {
                add(result, group, 4);
            }
        }
        if (groups.containsKey(16) && groups.containsKey(17)) {
            List<PlayingCard> rocket = new ArrayList<>();
            rocket.add(groups.get(16).get(0));
            rocket.add(groups.get(17).get(0));
            result.add(rocket);
        }

        for (Map.Entry<Integer, List<PlayingCard>> triple : groups.entrySet()) {
            if (triple.getValue().size() < 3) {
                continue;
            }
            for (Map.Entry<Integer, List<PlayingCard>> kicker : groups.entrySet()) {
                if (kicker.getKey().equals(triple.getKey())) {
                    continue;
                }
                List<PlayingCard> withSingle = take(triple.getValue(), 3);
                withSingle.add(kicker.getValue().get(0));
                result.add(withSingle);
                if (kicker.getValue().size() >= 2) {
                    List<PlayingCard> withPair = take(triple.getValue(), 3);
                    withPair.addAll(take(kicker.getValue(), 2));
                    result.add(withPair);
                }
            }
        }
        addSequences(result, groups, 1, 5);
        addSequences(result, groups, 2, 3);
        addSequences(result, groups, 3, 2);
        return result;
    }

    private static void addSequences(List<List<PlayingCard>> result,
                                     TreeMap<Integer, List<PlayingCard>> groups,
                                     int copies, int minimumRanks) {
        List<Integer> available = new ArrayList<>();
        for (Map.Entry<Integer, List<PlayingCard>> entry : groups.entrySet()) {
            if (entry.getKey() <= 14 && entry.getValue().size() >= copies) {
                available.add(entry.getKey());
            }
        }
        for (int start = 0; start < available.size(); start++) {
            for (int end = start + minimumRanks; end <= available.size(); end++) {
                List<Integer> ranks = available.subList(start, end);
                if (!isConsecutive(ranks)) {
                    break;
                }
                List<PlayingCard> cards = new ArrayList<>();
                for (Integer rank : ranks) {
                    cards.addAll(take(groups.get(rank), copies));
                }
                result.add(cards);
            }
        }
    }

    private static boolean isConsecutive(List<Integer> ranks) {
        for (int i = 1; i < ranks.size(); i++) {
            if (ranks.get(i) != ranks.get(i - 1) + 1) {
                return false;
            }
        }
        return true;
    }

    private static void add(List<List<PlayingCard>> result, List<PlayingCard> cards, int count) {
        result.add(take(cards, count));
    }

    private static List<PlayingCard> take(List<PlayingCard> cards, int count) {
        List<PlayingCard> result = new ArrayList<>(cards.subList(0, count));
        result.sort(Comparator.naturalOrder());
        return result;
    }
}
