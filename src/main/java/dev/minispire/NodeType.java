package dev.minispire;

public enum NodeType {
    COMBAT("Kampf", "Normaler Gegner"),
    ELITE("Elite", "Starker Gegner mit Reliktbelohnung"),
    EVENT("Ereignis", "Eine Entscheidung mit ungewissem Ausgang"),
    REST("Rastplatz", "Heilen oder eine Karte verbessern"),
    TREASURE("Schatz", "Gold oder ein Relikt"),
    BOSS("Boss", "Endgegner des Aktes");

    private final String displayName;
    private final String description;

    NodeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }
}
