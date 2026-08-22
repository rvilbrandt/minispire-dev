package dev.minispire;

import java.util.Objects;

public final class Card {
    private final String name;
    private final CardType type;
    private final CardEffect effect;
    private final int cost;
    private final int baseValue;
    private final int upgradedValue;
    private boolean upgraded;

    public Card(String name, CardType type, CardEffect effect, int cost, int baseValue, int upgradedValue) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.effect = Objects.requireNonNull(effect);
        if (cost < 0 || baseValue < 0 || upgradedValue < baseValue) {
            throw new IllegalArgumentException("Ungültige Kartenwerte");
        }
        this.cost = cost;
        this.baseValue = baseValue;
        this.upgradedValue = upgradedValue;
    }

    public String name() {
        return upgraded ? name + "+" : name;
    }

    public CardType type() {
        return type;
    }

    public CardEffect effect() {
        return effect;
    }

    public int cost() {
        return cost;
    }

    public int value() {
        return upgraded ? upgradedValue : baseValue;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public boolean upgrade() {
        if (upgraded) {
            return false;
        }
        upgraded = true;
        return true;
    }

    public String description() {
        return switch (effect) {
            case DAMAGE -> value() + " Schaden";
            case BLOCK -> value() + " Block";
            case DAMAGE_AND_VULNERABLE -> value() + " Schaden, 2 Verwundbar";
            case STRENGTH -> value() + " Stärke";
            case POISON -> value() + " Gift";
            case DAMAGE_AND_DRAW -> value() + " Schaden, 1 Karte ziehen";
            case BLOCK_AND_DRAW -> value() + " Block, 1 Karte ziehen";
        };
    }

    public Card freshCopy() {
        Card copy = new Card(name, type, effect, cost, baseValue, upgradedValue);
        if (upgraded) {
            copy.upgrade();
        }
        return copy;
    }

    @Override
    public String toString() {
        return "%s [%s, %d Energie] – %s".formatted(name(), type.displayName(), cost, description());
    }
}
