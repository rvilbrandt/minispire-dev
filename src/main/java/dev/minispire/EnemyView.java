package dev.minispire;

public record EnemyView(String name, int hp, int maxHp, int block, String statuses,
                        IntentType intentType, String intentDescription) {
    public static EnemyView from(Enemy enemy) {
        return new EnemyView(enemy.name(), enemy.hp(), enemy.maxHp(), enemy.block(), enemy.statuses().toString(),
                enemy.intent().type(), enemy.intent().description(enemy));
    }
}
