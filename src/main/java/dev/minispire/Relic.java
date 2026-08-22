package dev.minispire;

public enum Relic {
    BURNING_BLOOD("Brennendes Blut", "Heilt nach jedem gewonnenen Kampf 6 HP"),
    LANTERN("Laterne", "+1 Energie im ersten Zug jedes Kampfes"),
    ANCHOR("Anker", "+10 Block im ersten Zug jedes Kampfes"),
    VAJRA("Vajra", "+1 Stärke in jedem Kampf"),
    GOLDEN_IDOL("Goldenes Idol", "+25 % Gold aus Kämpfen");

    private final String displayName;
    private final String description;

    Relic(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return displayName + " – " + description;
    }
}
