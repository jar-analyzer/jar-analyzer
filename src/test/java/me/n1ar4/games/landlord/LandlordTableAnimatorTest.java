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

import static org.junit.jupiter.api.Assertions.*;

class LandlordTableAnimatorTest {
    @Test
    void dealsRoundRobinThenPlacesThreeBottomCards() {
        LandlordTableAnimator animator = new LandlordTableAnimator();
        assertTrue(animator.isDealing());
        assertEquals(LandlordGame.HUMAN, animator.frame().getTarget());

        animator.update(.066);
        assertEquals(1, animator.visibleHandCards(LandlordGame.HUMAN));
        assertEquals(0, animator.visibleHandCards(LandlordGame.LEFT_AI));

        animator.update(.066);
        assertEquals(1, animator.visibleHandCards(LandlordGame.LEFT_AI));
        assertEquals(0, animator.visibleHandCards(LandlordGame.RIGHT_AI));

        assertTrue(animator.update(4));
        assertFalse(animator.isDealing());
        assertEquals(17, animator.visibleHandCards(LandlordGame.HUMAN));
        assertEquals(17, animator.visibleHandCards(LandlordGame.LEFT_AI));
        assertEquals(17, animator.visibleHandCards(LandlordGame.RIGHT_AI));
        assertEquals(3, animator.visibleBottomCards());
    }

    @Test
    void restartRestoresAnEmptyTable() {
        LandlordTableAnimator animator = new LandlordTableAnimator();
        animator.update(4);
        animator.restart();

        assertTrue(animator.isDealing());
        assertEquals(0, animator.visibleHandCards(LandlordGame.HUMAN));
        assertEquals(0, animator.visibleBottomCards());
    }
}
