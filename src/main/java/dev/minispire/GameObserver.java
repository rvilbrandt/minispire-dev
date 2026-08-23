package dev.minispire;

@FunctionalInterface
public interface GameObserver {
    GameObserver NONE = state -> {
    };

    void mapChanged(MapViewState state);
}
