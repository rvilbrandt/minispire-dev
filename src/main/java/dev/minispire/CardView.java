package dev.minispire;

public record CardView(String name, CardType type, int cost, String description, boolean upgraded) {
    public static CardView from(Card card) {
        return new CardView(card.name(), card.type(), card.cost(), card.description(), card.isUpgraded());
    }
}
