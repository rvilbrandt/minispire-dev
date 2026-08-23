package dev.minispire;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    @Test
    void startsAndHandlesClosedInputWithoutLoopingForever() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        List<MapViewState> mapStates = new ArrayList<>();
        List<CombatViewState> combatStates = new ArrayList<>();
        List<List<CardView>> deckStates = new ArrayList<>();
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
        };
        Game game = new Game(new ByteArrayInputStream(new byte[0]),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), new Random(3), observer);

        game.run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("M I N I S P I R E"));
        assertTrue(output.contains("Der Durchlauf endet hier"));
        assertEquals(NodeType.COMBAT, mapStates.getFirst().choices().getFirst().type());
        assertEquals(NodeType.COMBAT, mapStates.get(1).visited().getFirst().type());
        assertEquals(10, deckStates.getFirst().size());
        assertEquals(Player.CARDS_PER_TURN, combatStates.getFirst().hand().size());
        assertTrue(combatStates.getFirst().acceptingCardSelection());
    }
}
