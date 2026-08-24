package dev.minispire;

import java.util.List;

public record CombatViewState(List<CardView> hand, List<CardView> deck, List<EnemyView> enemies, int energy,
                              boolean acceptingCardSelection, boolean acceptingTargetSelection) {
    public CombatViewState {
        hand = List.copyOf(hand);
        deck = List.copyOf(deck);
        enemies = List.copyOf(enemies);
    }

    public CombatViewState(List<CardView> hand, List<CardView> deck, int energy,
                           boolean acceptingCardSelection) {
        this(hand, deck, List.of(), energy, acceptingCardSelection, false);
    }

    public static CombatViewState from(Combat combat, boolean acceptingCardSelection) {
        return from(combat, acceptingCardSelection, false);
    }

    public static CombatViewState from(Combat combat, boolean acceptingCardSelection,
                                       boolean acceptingTargetSelection) {
        List<EnemyView> enemies = combat.livingEnemies().stream().map(EnemyView::from).toList();
        return new CombatViewState(toViews(combat.hand()), toViews(combat.player().deck()), enemies,
                combat.player().energy(), acceptingCardSelection, acceptingTargetSelection);
    }

    static List<CardView> toViews(List<Card> cards) {
        return cards.stream().map(CardView::from).toList();
    }
}
