package dev.minispire;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatTest {
    @Test
    void playingCardSpendsEnergyAndMovesCardToDiscardPile() {
        Player player = new Player("Test");
        Enemy enemy = new Enemy("Attrappe", 100,
                List.of(new EnemyIntent(IntentType.ATTACK, 1)));
        Combat combat = new Combat(player, List.of(enemy), new Random(42));
        combat.startPlayerTurn();
        int initialHandSize = combat.hand().size();
        int index = findAffordableCard(combat);
        Card selected = combat.hand().get(index);

        PlayResult result = combat.playCard(index, 0);

        assertTrue(result.successful());
        assertEquals(Player.BASE_ENERGY - selected.cost(), player.energy());
        assertEquals(initialHandSize - 1, combat.hand().size());
        assertEquals(1, combat.discardPileSize());
    }

    @Test
    void discardPileIsRecycledWhenDrawPileIsEmpty() {
        Player player = new Player("Test");
        Enemy enemy = new Enemy("Attrappe", 999,
                List.of(new EnemyIntent(IntentType.BLOCK, 0)));
        Combat combat = new Combat(player, List.of(enemy), new Random(7));

        combat.startPlayerTurn();
        combat.endPlayerTurn();
        combat.startPlayerTurn();
        combat.endPlayerTurn();
        combat.startPlayerTurn();

        assertEquals(Player.CARDS_PER_TURN, combat.hand().size());
        assertEquals(5, combat.drawPileSize());
        assertEquals(0, combat.discardPileSize());
    }

    @Test
    void unplayedBlockProtectsDuringEnemyTurnAndExpiresNextTurn() {
        Player player = new Player("Test");
        Enemy enemy = new Enemy("Attrappe", 20,
                List.of(new EnemyIntent(IntentType.ATTACK, 5)));
        Combat combat = new Combat(player, List.of(enemy), new Random(1));
        combat.startPlayerTurn();
        player.gainBlock(7);

        combat.endPlayerTurn();

        assertEquals(2, player.block());
        assertEquals(70, player.hp());

        combat.startPlayerTurn();
        assertEquals(0, player.block());
    }

    private static int findAffordableCard(Combat combat) {
        for (int i = 0; i < combat.hand().size(); i++) {
            if (combat.hand().get(i).cost() <= Player.BASE_ENERGY) {
                return i;
            }
        }
        throw new AssertionError("Keine spielbare Karte gezogen");
    }
}
