package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnemiesPanelTest {
    private static final EnemyView CULTIST = new EnemyView(
            "Kultist", 38, 38, 0, "Stärke 2", IntentType.ATTACK, "Angriff für 9");
    private static final EnemyView SLIME = new EnemyView(
            "Säureschleim", 24, 30, 5, "Schwäche 1", IntentType.INFLICT_WEAKNESS, "2 Schwäche");

    @Test
    void clickSubmitsEnemyNumberDuringTargetSelection() throws Exception {
        AtomicInteger selectedTarget = new AtomicInteger();
        EnemiesPanel panel = new EnemiesPanel(selectedTarget::set);
        CombatViewState state = new CombatViewState(List.of(), List.of(), List.of(CULTIST, SLIME),
                2, false, true);

        SwingUtilities.invokeAndWait(() -> {
            panel.showCombat(state);
            panel.enemyButtons().get(1).doClick();
        });

        assertEquals(2, selectedTarget.get());
    }

    @Test
    void clickDoesNothingOutsideTargetSelection() throws Exception {
        AtomicInteger selectedTarget = new AtomicInteger();
        EnemiesPanel panel = new EnemiesPanel(selectedTarget::set);
        CombatViewState state = new CombatViewState(List.of(), List.of(), List.of(CULTIST),
                2, true, false);

        SwingUtilities.invokeAndWait(() -> {
            panel.showCombat(state);
            panel.enemyButtons().getFirst().doClick();
        });

        assertEquals(0, selectedTarget.get());
    }
}
