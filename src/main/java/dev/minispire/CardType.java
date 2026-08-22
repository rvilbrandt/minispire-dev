package dev.minispire;

public enum CardType {
    ATTACK("Angriff"),
    DEFENSE("Verteidigung"),
    SPECIAL("Spezial");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
