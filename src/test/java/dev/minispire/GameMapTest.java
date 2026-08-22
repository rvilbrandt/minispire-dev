package dev.minispire;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameMapTest {
    @Test
    void runStartsWithCombatAndEndsWithBoss() {
        GameMap map = new GameMap(new Random(1));

        assertEquals(List.of(new MapNode(1, NodeType.COMBAT)), map.choices(1));
        assertEquals(List.of(new MapNode(GameMap.FLOORS_PER_ACT, NodeType.BOSS)),
                map.choices(GameMap.FLOORS_PER_ACT));
    }

    @Test
    void intermediateFloorOffersThreeDifferentPaths() {
        GameMap map = new GameMap(new Random(2));

        List<MapNode> choices = map.choices(3);

        assertEquals(3, choices.size());
        assertEquals(3, choices.stream().map(MapNode::type).distinct().count());
        assertTrue(choices.stream().noneMatch(node -> node.type() == NodeType.BOSS));
    }
}
