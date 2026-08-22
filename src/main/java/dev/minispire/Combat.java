package dev.minispire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class Combat {
    private final Player player;
    private final List<Enemy> enemies;
    private final List<Card> drawPile;
    private final List<Card> discardPile = new ArrayList<>();
    private final List<Card> hand = new ArrayList<>();
    private final RandomGenerator random;
    private int turn;

    public Combat(Player player, List<Enemy> enemies, RandomGenerator random) {
        this.player = Objects.requireNonNull(player);
        this.enemies = new ArrayList<>(Objects.requireNonNull(enemies));
        this.random = Objects.requireNonNull(random);
        if (enemies.isEmpty()) {
            throw new IllegalArgumentException("Ein Kampf benötigt Gegner");
        }
        drawPile = new ArrayList<>(player.deck());
        shuffle(drawPile);
        player.prepareForCombat();
    }

    public Player player() {
        return player;
    }

    public List<Enemy> enemies() {
        return Collections.unmodifiableList(enemies);
    }

    public List<Enemy> livingEnemies() {
        return enemies.stream().filter(Enemy::isAlive).toList();
    }

    public List<Card> hand() {
        return Collections.unmodifiableList(hand);
    }

    public int drawPileSize() {
        return drawPile.size();
    }

    public int discardPileSize() {
        return discardPile.size();
    }

    public int turn() {
        return turn;
    }

    public boolean isWon() {
        return livingEnemies().isEmpty();
    }

    public boolean isLost() {
        return !player.isAlive();
    }

    public List<String> startPlayerTurn() {
        List<String> events = new ArrayList<>();
        if (turn > 0) {
            player.clearBlock();
        }
        turn++;

        int poisonDamage = player.applyPoison();
        if (poisonDamage > 0) {
            events.add("Gift verursacht dir %d Schaden.".formatted(poisonDamage));
        }
        for (Enemy enemy : livingEnemies()) {
            int damage = enemy.applyPoison();
            if (damage > 0) {
                events.add("%s erleidet %d Giftschaden.".formatted(enemy.name(), damage));
            }
        }

        player.resetEnergy(turn == 1);
        drawCards(Player.CARDS_PER_TURN);
        return events;
    }

    public PlayResult playCard(int handIndex, int targetIndex) {
        if (handIndex < 0 || handIndex >= hand.size()) {
            return PlayResult.failure("Diese Karte gibt es nicht.");
        }
        Card card = hand.get(handIndex);
        Enemy target = null;
        if (requiresTarget(card.effect())) {
            List<Enemy> living = livingEnemies();
            if (targetIndex < 0 || targetIndex >= living.size()) {
                return PlayResult.failure("Dieses Ziel gibt es nicht.");
            }
            target = living.get(targetIndex);
        }
        if (!player.spendEnergy(card.cost())) {
            return PlayResult.failure("Nicht genügend Energie.");
        }

        String message = applyCard(card, target);
        hand.remove(handIndex);
        discardPile.add(card);
        return PlayResult.success(message);
    }

    private String applyCard(Card card, Enemy target) {
        return switch (card.effect()) {
            case DAMAGE -> dealCardDamage(card, target);
            case BLOCK -> {
                player.gainBlock(card.value());
                yield "%s gibt dir %d Block.".formatted(card.name(), card.value());
            }
            case DAMAGE_AND_VULNERABLE -> {
                String damage = dealCardDamage(card, target);
                target.statuses().addVulnerability(2);
                yield damage + " Das Ziel erhält 2 Verwundbar.";
            }
            case STRENGTH -> {
                player.statuses().addStrength(card.value());
                yield "%s gibt dir %d Stärke.".formatted(card.name(), card.value());
            }
            case POISON -> {
                target.statuses().addPoison(card.value());
                yield "%s erhält %d Gift.".formatted(target.name(), card.value());
            }
            case DAMAGE_AND_DRAW -> {
                String damage = dealCardDamage(card, target);
                drawCards(1);
                yield damage + " Du ziehst 1 Karte.";
            }
            case BLOCK_AND_DRAW -> {
                player.gainBlock(card.value());
                drawCards(1);
                yield "%s gibt dir %d Block und zieht 1 Karte.".formatted(card.name(), card.value());
            }
        };
    }

    private String dealCardDamage(Card card, Enemy target) {
        int attack = player.attackDamage(card.value());
        int hpDamage = target.takeAttackDamage(attack);
        return "%s trifft %s für %d (%d HP-Schaden).".formatted(card.name(), target.name(), attack, hpDamage);
    }

    public List<String> endPlayerTurn() {
        discardPile.addAll(hand);
        hand.clear();
        player.statuses().tickDurations();

        List<String> events = new ArrayList<>();
        for (Enemy enemy : livingEnemies()) {
            enemy.clearBlock();
            if (!player.isAlive()) {
                break;
            }
            events.add(enemy.performIntent(player));
            enemy.statuses().tickDurations();
        }
        return events;
    }

    private void drawCards(int amount) {
        for (int i = 0; i < amount; i++) {
            if (drawPile.isEmpty()) {
                if (discardPile.isEmpty()) {
                    return;
                }
                drawPile.addAll(discardPile);
                discardPile.clear();
                shuffle(drawPile);
            }
            hand.add(drawPile.removeLast());
        }
    }

    private void shuffle(List<Card> cards) {
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card temporary = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temporary);
        }
    }

    private static boolean requiresTarget(CardEffect effect) {
        return switch (effect) {
            case DAMAGE, DAMAGE_AND_VULNERABLE, POISON, DAMAGE_AND_DRAW -> true;
            default -> false;
        };
    }
}
