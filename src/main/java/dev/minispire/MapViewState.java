package dev.minispire;

import java.util.List;

public record MapViewState(int act, int floor, List<MapNode> visited, List<MapNode> choices) {
    public MapViewState {
        if (act < 1 || floor < 1) {
            throw new IllegalArgumentException("Akt und Ebene müssen positiv sein");
        }
        visited = List.copyOf(visited);
        choices = List.copyOf(choices);
    }
}
