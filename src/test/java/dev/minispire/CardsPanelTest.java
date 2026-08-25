package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardsPanelTest {
    @Test
    void primaryCardActionIsNextToTheHandAndEndTurnIsOnTheRight() throws Exception {
        CardsPanel panel = new CardsPanel(ignored -> {
        });

        SwingUtilities.invokeAndWait(() -> {
            BorderLayout layout = (BorderLayout) panel.actionPanel().getLayout();

            assertSame(panel.confirmButton(), layout.getLayoutComponent(BorderLayout.WEST));
            assertSame(panel.endTurnButton(), layout.getLayoutComponent(BorderLayout.EAST));
        });
    }

    @Test
    void mouseSelectionRequiresConfirmationBeforeSubmittingCard() throws Exception {
        AtomicInteger submittedCard = new AtomicInteger();
        CardsPanel panel = new CardsPanel(submittedCard::set);
        CardView strike = new CardView("Schlag", CardType.ATTACK, 1, "6 Schaden", false);
        CardView defend = new CardView("Verteidigen", CardType.DEFENSE, 1, "5 Block", false);
        CombatViewState state = new CombatViewState(List.of(strike, defend), List.of(strike, defend), 1, true);

        SwingUtilities.invokeAndWait(() -> {
            panel.showCombat(state);
            panel.handButtons().get(1).doClick();

            assertEquals(0, submittedCard.get());
            assertTrue(panel.confirmButton().isEnabled());
            assertEquals("Ausgewählt: Verteidigen", panel.selectionHint().getText());

            panel.confirmButton().doClick();
        });

        assertEquals(2, submittedCard.get());
        assertFalse(panel.confirmButton().isEnabled());
    }

    @Test
    void unaffordableCardsCannotBeSelected() throws Exception {
        CardsPanel panel = new CardsPanel(ignored -> {
        });
        CardView expensive = new CardView("Schwerer Hieb", CardType.ATTACK, 2, "14 Schaden", false);

        SwingUtilities.invokeAndWait(() -> panel.showCombat(
                new CombatViewState(List.of(expensive), List.of(expensive), 1, true)));

        assertFalse(panel.handButtons().getFirst().isEnabled());
        assertFalse(panel.confirmButton().isEnabled());
    }

    @Test
    void endTurnButtonSubmitsWithoutTextInput() throws Exception {
        AtomicInteger endedTurns = new AtomicInteger();
        CardsPanel panel = new CardsPanel(ignored -> {
        }, endedTurns::incrementAndGet);
        CardView strike = new CardView("Schlag", CardType.ATTACK, 1, "6 Schaden", false);

        SwingUtilities.invokeAndWait(() -> {
            panel.showCombat(new CombatViewState(List.of(strike), List.of(strike), 3, true));
            panel.endTurnButton().doClick();
        });

        assertEquals(1, endedTurns.get());
        assertFalse(panel.endTurnButton().isEnabled());
    }
}
