package dev.minispire;

public record EnemyIntent(IntentType type, int value) {
    public EnemyIntent {
        if (value < 0) {
            throw new IllegalArgumentException("Intent-Wert darf nicht negativ sein");
        }
    }

    public String description(Enemy enemy) {
        return switch (type) {
            case ATTACK -> "Angriff für " + enemy.attackDamage(value);
            case BLOCK -> value + " Block";
            case BUFF_STRENGTH -> value + " Stärke";
            case INFLICT_WEAKNESS -> value + " Schwäche";
        };
    }
}
