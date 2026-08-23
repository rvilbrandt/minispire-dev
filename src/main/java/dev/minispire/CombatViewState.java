package dev.minispire;

import java.util.List;

public record CombatViewState(List<CardView> hand, List<CardView> deck, int energy,
                              boolean acceptingCardSelection) {
    public CombatViewState {
        hand = List.copyOf(hand);
        deck = List.copyOf(deck);
    }

    public static CombatViewState from(Combat combat, boolean acceptingCardSelection) {
        return new CombatViewState(toViews(combat.hand()), toViews(combat.player().deck()),
                combat.player().energy(), acceptingCardSelection);
    }

    static List<CardView> toViews(List<Card> cards) {
        return cards.stream().map(CardView::from).toList();
    }
}
