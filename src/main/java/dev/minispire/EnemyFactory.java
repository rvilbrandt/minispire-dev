package dev.minispire;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class EnemyFactory {
    private EnemyFactory() {
    }

    public static List<Enemy> normalEncounter(int act, RandomGenerator random) {
        int scale = (act - 1) * 7;
        if (random.nextBoolean()) {
            return List.of(new Enemy("Kultist", 38 + scale,
                    List.of(new EnemyIntent(IntentType.BUFF_STRENGTH, 2),
                            new EnemyIntent(IntentType.ATTACK, 7 + act))));
        }
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy("Säureschleim", 24 + scale,
                List.of(new EnemyIntent(IntentType.ATTACK, 6 + act),
                        new EnemyIntent(IntentType.INFLICT_WEAKNESS, 2))));
        if (act > 1 || random.nextBoolean()) {
            enemies.add(new Enemy("Stachelschleim", 20 + scale,
                    List.of(new EnemyIntent(IntentType.ATTACK, 5 + act),
                            new EnemyIntent(IntentType.BLOCK, 6 + act))));
        }
        return enemies;
    }

    public static List<Enemy> eliteEncounter(int act, RandomGenerator random) {
        int scale = (act - 1) * 12;
        if (random.nextBoolean()) {
            return List.of(new Enemy("Gremlin-Nob", 82 + scale,
                    List.of(new EnemyIntent(IntentType.ATTACK, 13 + act),
                            new EnemyIntent(IntentType.BUFF_STRENGTH, 3),
                            new EnemyIntent(IntentType.ATTACK, 17 + act))));
        }
        return List.of(new Enemy("Lagavulin", 90 + scale,
                List.of(new EnemyIntent(IntentType.BLOCK, 12),
                        new EnemyIntent(IntentType.ATTACK, 18 + act),
                        new EnemyIntent(IntentType.INFLICT_WEAKNESS, 2))));
    }

    public static List<Enemy> bossEncounter(int act) {
        return switch (act) {
            case 1 -> List.of(new Enemy("Hexaghost", 125,
                    List.of(new EnemyIntent(IntentType.ATTACK, 14),
                            new EnemyIntent(IntentType.BLOCK, 12),
                            new EnemyIntent(IntentType.ATTACK, 20))));
            case 2 -> List.of(new Enemy("Der Champion", 175,
                    List.of(new EnemyIntent(IntentType.ATTACK, 18),
                            new EnemyIntent(IntentType.BUFF_STRENGTH, 3),
                            new EnemyIntent(IntentType.BLOCK, 15),
                            new EnemyIntent(IntentType.ATTACK, 25))));
            default -> List.of(new Enemy("Erwachter", 225,
                    List.of(new EnemyIntent(IntentType.ATTACK, 21),
                            new EnemyIntent(IntentType.BUFF_STRENGTH, 4),
                            new EnemyIntent(IntentType.ATTACK, 30))));
        };
    }
}
