package dev.minispire;

public interface GameObserver {
    GameObserver NONE = new GameObserver() {
    };

    default void mapChanged(MapViewState state) {
    }

    default void combatChanged(CombatViewState state) {
    }

    default void combatEnded() {
    }

    default void deckChanged(java.util.List<CardView> deck) {
    }
}
