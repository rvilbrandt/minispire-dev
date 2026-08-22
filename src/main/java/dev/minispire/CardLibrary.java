package dev.minispire;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class CardLibrary {
    private static final List<Card> REWARDS = List.of(
            new Card("Schwerer Hieb", CardType.ATTACK, CardEffect.DAMAGE, 2, 14, 19),
            new Card("Schneller Schnitt", CardType.ATTACK, CardEffect.DAMAGE_AND_DRAW, 1, 5, 8),
            new Card("Eisenhaut", CardType.DEFENSE, CardEffect.BLOCK, 2, 13, 17),
            new Card("Vorbereitung", CardType.DEFENSE, CardEffect.BLOCK_AND_DRAW, 1, 6, 9),
            new Card("Kraftschub", CardType.SPECIAL, CardEffect.STRENGTH, 1, 2, 3),
            new Card("Giftwolke", CardType.SPECIAL, CardEffect.POISON, 1, 4, 7),
            new Card("Zerschmettern", CardType.ATTACK, CardEffect.DAMAGE_AND_VULNERABLE, 2, 9, 13)
    );

    private CardLibrary() {
    }

    public static List<Card> starterDeck() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(new Card("Schlag", CardType.ATTACK, CardEffect.DAMAGE, 1, 6, 9));
        }
        for (int i = 0; i < 4; i++) {
            cards.add(new Card("Verteidigen", CardType.DEFENSE, CardEffect.BLOCK, 1, 5, 8));
        }
        cards.add(new Card("Wuchtschlag", CardType.ATTACK, CardEffect.DAMAGE_AND_VULNERABLE, 2, 8, 11));
        return cards;
    }

    public static List<Card> randomRewards(RandomGenerator random, int amount) {
        List<Card> pool = new ArrayList<>(REWARDS);
        List<Card> result = new ArrayList<>();
        while (result.size() < amount && !pool.isEmpty()) {
            result.add(pool.remove(random.nextInt(pool.size())).freshCopy());
        }
        return result;
    }
}
