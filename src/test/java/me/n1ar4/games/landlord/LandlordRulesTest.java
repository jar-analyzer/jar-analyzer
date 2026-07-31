package me.n1ar4.games.landlord;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandlordRulesTest {
    private int nextId;

    @Test
    void recognizesBasicAndSequenceCombinations() {
        assertType(Combination.Type.SINGLE, cards(3));
        assertType(Combination.Type.PAIR, cards(7, 7));
        assertType(Combination.Type.TRIPLE, cards(9, 9, 9));
        assertType(Combination.Type.TRIPLE_SINGLE, cards(6, 6, 6, 12));
        assertType(Combination.Type.TRIPLE_PAIR, cards(8, 8, 8, 10, 10));
        assertType(Combination.Type.STRAIGHT, cards(3, 4, 5, 6, 7));
        assertType(Combination.Type.PAIR_STRAIGHT, cards(6, 6, 7, 7, 8, 8));
        assertType(Combination.Type.AIRPLANE, cards(9, 9, 9, 10, 10, 10));
        assertType(Combination.Type.AIRPLANE_SINGLE,
                cards(4, 4, 4, 5, 5, 5, 11, 12));
        assertType(Combination.Type.FOUR_TWO_PAIR,
                cards(7, 7, 7, 7, 9, 9, 10, 10));
    }

    @Test
    void rejectsInvalidSequencesAndComparesPowerCards() {
        assertNull(LandlordRules.evaluate(cards(10, 11, 12, 13, 14, 15)));

        Combination straight = LandlordRules.evaluate(cards(3, 4, 5, 6, 7));
        Combination longerStraight = LandlordRules.evaluate(cards(3, 4, 5, 6, 7, 8));
        Combination bomb = LandlordRules.evaluate(cards(4, 4, 4, 4));
        Combination rocket = LandlordRules.evaluate(jokers());
        assertNotNull(straight);
        assertNotNull(longerStraight);
        assertNotNull(bomb);
        assertNotNull(rocket);
        assertFalse(longerStraight.beats(straight));
        assertTrue(bomb.beats(straight));
        assertTrue(rocket.beats(bomb));
        assertFalse(bomb.beats(rocket));
    }

    @Test
    void aiUsesSmallestLegalResponseBeforeBombs() {
        List<PlayingCard> hand = cards(3, 5, 5, 7, 7, 7, 7);
        Combination previous = LandlordRules.evaluate(cards(4, 4));
        List<PlayingCard> choice = LandlordAi.choose(hand, previous);

        assertEquals(Arrays.asList(5, 5), ranks(choice));
        assertEquals(Combination.Type.PAIR, LandlordRules.evaluate(choice).getType());
    }

    private void assertType(Combination.Type expected, List<PlayingCard> cards) {
        Combination combination = LandlordRules.evaluate(cards);
        assertNotNull(combination);
        assertEquals(expected, combination.getType());
    }

    private List<PlayingCard> cards(int... ranks) {
        List<PlayingCard> cards = new ArrayList<>();
        PlayingCard.Suit[] suits = PlayingCard.Suit.values();
        for (int rank : ranks) {
            PlayingCard.Suit suit = rank >= 16
                    ? PlayingCard.Suit.JOKER : suits[nextId % 4];
            cards.add(new PlayingCard(nextId++, suit, rank));
        }
        return cards;
    }

    private List<PlayingCard> jokers() {
        return cards(16, 17);
    }

    private static List<Integer> ranks(List<PlayingCard> cards) {
        List<Integer> ranks = new ArrayList<>();
        for (PlayingCard card : cards) {
            ranks.add(card.getRank());
        }
        return ranks;
    }
}
