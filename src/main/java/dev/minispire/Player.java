package dev.minispire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Player extends Combatant {
    public static final int BASE_ENERGY = 3;
    public static final int CARDS_PER_TURN = 5;

    private int energy;
    private int gold;
    private final List<Card> deck;
    private final List<Relic> relics = new ArrayList<>();

    public Player(String name) {
        super(name, 70);
        deck = new ArrayList<>(CardLibrary.starterDeck());
        relics.add(Relic.BURNING_BLOOD);
    }

    public int energy() {
        return energy;
    }

    public int gold() {
        return gold;
    }

    public List<Card> deck() {
        return Collections.unmodifiableList(deck);
    }

    public List<Relic> relics() {
        return Collections.unmodifiableList(relics);
    }

    public void resetEnergy(boolean firstTurn) {
        energy = BASE_ENERGY + (firstTurn && hasRelic(Relic.LANTERN) ? 1 : 0);
    }

    public boolean spendEnergy(int amount) {
        if (amount < 0 || energy < amount) {
            return false;
        }
        energy -= amount;
        return true;
    }

    public void addGold(int amount) {
        gold += Math.max(0, amount);
    }

    public boolean spendGold(int amount) {
        if (amount < 0 || gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void addCard(Card card) {
        deck.add(card);
    }

    public Card removeCard(int index) {
        return deck.remove(index);
    }

    public boolean addRelic(Relic relic) {
        if (hasRelic(relic)) {
            return false;
        }
        relics.add(relic);
        return true;
    }

    public boolean hasRelic(Relic relic) {
        return relics.contains(relic);
    }

    public void prepareForCombat() {
        clearBlock();
        statuses().clear();
        if (hasRelic(Relic.VAJRA)) {
            statuses().addStrength(1);
        }
        if (hasRelic(Relic.ANCHOR)) {
            gainBlock(10);
        }
    }

    public int finishWonCombat() {
        clearBlock();
        statuses().clear();
        return hasRelic(Relic.BURNING_BLOOD) ? heal(6) : 0;
    }
}
