package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatsPanelTest {
    @Test
    void permanentlyDisplaysPlayerValuesAndCombatEnergy() throws Exception {
        PlayerStatsPanel panel = new PlayerStatsPanel();
        PlayerView player = new PlayerView("Wanderer", 54, 70, 8, 3, 125,
                "Stärke 2", 12, List.of("Brennendes Blut", "Anker"), true);

        SwingUtilities.invokeAndWait(() -> panel.showPlayer(player));

        assertEquals("HP 54 / 70", panel.healthText());
        assertTrue(panel.energyText().contains("3"));
    }

    @Test
    void hidesTurnEnergyOutsideCombat() throws Exception {
        PlayerStatsPanel panel = new PlayerStatsPanel();
        PlayerView player = new PlayerView("Wanderer", 70, 70, 0, 2, 0,
                "keine", 10, List.of("Brennendes Blut"), false);

        SwingUtilities.invokeAndWait(() -> panel.showPlayer(player));

        assertTrue(panel.energyText().contains("–"));
    }
}
