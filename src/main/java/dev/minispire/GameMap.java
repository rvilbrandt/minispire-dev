package dev.minispire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

public final class GameMap {
    public static final int FLOORS_PER_ACT = 5;

    private final RandomGenerator random;

    public GameMap(RandomGenerator random) {
        this.random = random;
    }

    public List<MapNode> choices(int floor) {
        if (floor == FLOORS_PER_ACT) {
            return List.of(new MapNode(floor, NodeType.BOSS));
        }
        if (floor == 1) {
            return List.of(new MapNode(floor, NodeType.COMBAT));
        }

        List<NodeType> candidates = new ArrayList<>(List.of(
                NodeType.COMBAT, NodeType.COMBAT, NodeType.ELITE,
                NodeType.EVENT, NodeType.REST, NodeType.TREASURE));
        shuffle(candidates);
        List<MapNode> nodes = new ArrayList<>();
        for (NodeType type : candidates) {
            if (nodes.stream().noneMatch(node -> node.type() == type)) {
                nodes.add(new MapNode(floor, type));
            }
            if (nodes.size() == 3) {
                break;
            }
        }
        return List.copyOf(nodes);
    }

    private void shuffle(List<NodeType> values) {
        for (int i = values.size() - 1; i > 0; i--) {
            Collections.swap(values, i, random.nextInt(i + 1));
        }
    }
}
