package dev.minispire;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    @Test
    void runsThroughStructuredInteractionsWithoutConsoleIo() {
        List<String> messages = new ArrayList<>();
        List<ChoiceRequest> requests = new ArrayList<>();
        List<MapViewState> mapStates = new ArrayList<>();
        List<CombatViewState> combatStates = new ArrayList<>();
        List<List<CardView>> deckStates = new ArrayList<>();
        List<PlayerView> playerStates = new ArrayList<>();
        GameObserver observer = new GameObserver() {
            @Override
            public void mapChanged(MapViewState state) {
                mapStates.add(state);
            }

            @Override
            public void combatChanged(CombatViewState state) {
                combatStates.add(state);
            }

            @Override
            public void deckChanged(List<CardView> deck) {
                deckStates.add(deck);
            }

            @Override
            public void playerChanged(PlayerView player) {
                playerStates.add(player);
            }
        };
        GameInteraction interaction = new GameInteraction() {
            @Override
            public void message(String message) {
                messages.add(message);
            }

            @Override
            public int choose(ChoiceRequest request) {
                requests.add(request);
                return request.defaultValue();
            }
        };
        Game game = new Game(interaction, new Random(3), observer);

        game.run();

        String output = String.join("\n", messages);
        assertTrue(output.contains("Willkommen bei Minispire"));
        assertTrue(output.contains("Der Durchlauf endet hier"));
        assertEquals(ChoiceKind.MAP_NODE, requests.getFirst().kind());
        assertTrue(requests.stream().anyMatch(request -> request.kind() == ChoiceKind.COMBAT_ACTION));
        assertEquals(NodeType.COMBAT, mapStates.getFirst().choices().getFirst().type());
        assertEquals(NodeType.COMBAT, mapStates.get(1).visited().getFirst().type());
        assertEquals(10, deckStates.getFirst().size());
        assertEquals(Player.CARDS_PER_TURN, combatStates.getFirst().hand().size());
        assertTrue(combatStates.getFirst().acceptingCardSelection());
        assertTrue(combatStates.getFirst().enemies().getFirst().hp() > 0);
        assertFalse(combatStates.getFirst().enemies().getFirst().intentDescription().isBlank());
        assertEquals(70, playerStates.getFirst().hp());
        assertTrue(playerStates.stream().anyMatch(PlayerView::inCombat));
        assertFalse(output.contains("| HP "));
        assertFalse(output.contains("| Block "));
        assertFalse(output.contains("| Absicht:"));
    }
}
