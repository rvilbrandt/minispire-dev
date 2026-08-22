package dev.minispire;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatantTest {
    @Test
    void blockAbsorbsDamageBeforeHitPoints() {
        Enemy enemy = enemyWithHp(30);
        enemy.gainBlock(6);

        int hpDamage = enemy.takeAttackDamage(10);

        assertEquals(4, hpDamage);
        assertEquals(26, enemy.hp());
        assertEquals(0, enemy.block());
    }

    @Test
    void vulnerabilityIncreasesIncomingAttackDamage() {
        Enemy enemy = enemyWithHp(30);
        enemy.statuses().addVulnerability(2);

        enemy.takeAttackDamage(7);

        assertEquals(19, enemy.hp());
    }

    @Test
    void poisonIgnoresBlockAndDecays() {
        Enemy enemy = enemyWithHp(30);
        enemy.gainBlock(20);
        enemy.statuses().addPoison(4);

        assertEquals(4, enemy.applyPoison());
        assertEquals(26, enemy.hp());
        assertEquals(20, enemy.block());
        assertEquals(3, enemy.statuses().poison());
    }

    private static Enemy enemyWithHp(int hp) {
        return new Enemy("Testgegner", hp, List.of(new EnemyIntent(IntentType.ATTACK, 1)));
    }
}
