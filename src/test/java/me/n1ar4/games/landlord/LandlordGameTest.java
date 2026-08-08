/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.games.landlord;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LandlordGameTest {
    @Test
    void dealsCompleteDeckAndFinishesOneRoundOfBidding() {
        LandlordGame game = new LandlordGame(new Random(7));
        LandlordGame.Snapshot initial = game.snapshot();
        assertEquals(LandlordGame.State.BIDDING, initial.getState());
        assertEquals(17, initial.getHumanHand().size());
        assertEquals(3, initial.getBottomCards().size());

        assertTrue(game.humanBid(true));
        assertTrue(game.performAiStep());
        assertTrue(game.performAiStep());

        LandlordGame.Snapshot playing = game.snapshot();
        assertEquals(LandlordGame.State.PLAYING, playing.getState());
        assertTrue(playing.getLandlord() >= 0 && playing.getLandlord() <= 2);
        assertEquals(20, playing.getCardCount(playing.getLandlord()));
        assertEquals(54, playing.getCardCount(0) + playing.getCardCount(1)
                + playing.getCardCount(2));
        assertFalse(game.humanBid(false));
    }

    @Test
    void restartReturnsToFreshBiddingRound() {
        LandlordGame game = new LandlordGame(new Random(11));
        game.humanBid(false);
        game.performAiStep();
        game.performAiStep();
        game.restart();

        LandlordGame.Snapshot snapshot = game.snapshot();
        assertEquals(LandlordGame.State.BIDDING, snapshot.getState());
        assertEquals(LandlordGame.HUMAN, snapshot.getCurrentPlayer());
        assertEquals(-1, snapshot.getLandlord());
        assertEquals(17, snapshot.getCardCount(LandlordGame.HUMAN));
    }

    @Test
    void exposesEveryPlayersLastActionForTableFeedback() {
        LandlordGame game = new LandlordGame(new Random(17));
        assertTrue(game.humanBid(false));
        assertEquals(LandlordGame.HUMAN, game.snapshot().getLastActionPlayer());
        assertEquals("不抢", game.snapshot().getLastActionText());

        assertTrue(game.performAiStep());
        LandlordGame.Snapshot firstAi = game.snapshot();
        assertEquals(LandlordGame.LEFT_AI, firstAi.getLastActionPlayer());
        assertFalse(firstAi.getLastActionText().isEmpty());

        assertTrue(game.performAiStep());
        LandlordGame.Snapshot finalBid = game.snapshot();
        assertEquals(LandlordGame.RIGHT_AI, finalBid.getLastActionPlayer());
        assertFalse(finalBid.getLastActionText().isEmpty());
        assertEquals(LandlordGame.State.PLAYING, finalBid.getState());
    }
}
