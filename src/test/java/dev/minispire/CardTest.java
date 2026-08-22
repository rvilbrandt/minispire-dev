package dev.minispire;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardTest {
    @Test
    void upgradePermanentlyImprovesCard() {
        Card card = new Card("Schlag", CardType.ATTACK, CardEffect.DAMAGE, 1, 6, 9);

        assertTrue(card.upgrade());
        assertEquals(9, card.value());
        assertEquals("Schlag+", card.name());
        assertFalse(card.upgrade());
    }
}
