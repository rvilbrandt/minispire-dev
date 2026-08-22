package dev.minispire;

import java.util.Objects;

public abstract class Combatant {
    private final String name;
    private int maxHp;
    private int hp;
    private int block;
    private final StatusEffects statuses = new StatusEffects();

    protected Combatant(String name, int maxHp) {
        this.name = Objects.requireNonNull(name);
        if (maxHp <= 0) {
            throw new IllegalArgumentException("Maximale HP müssen positiv sein");
        }
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public String name() {
        return name;
    }

    public int maxHp() {
        return maxHp;
    }

    public int hp() {
        return hp;
    }

    public int block() {
        return block;
    }

    public StatusEffects statuses() {
        return statuses;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void gainBlock(int amount) {
        block += Math.max(0, amount);
    }

    public void clearBlock() {
        block = 0;
    }

    public int attackDamage(int baseDamage) {
        int result = Math.max(0, baseDamage + statuses.strength());
        if (statuses.weakness() > 0) {
            result = result * 3 / 4;
        }
        return result;
    }

    public int takeAttackDamage(int amount) {
        int incoming = Math.max(0, amount);
        if (statuses.vulnerability() > 0) {
            incoming = (incoming * 3 + 1) / 2;
        }
        return takeDamage(incoming);
    }

    public int takeDamage(int amount) {
        int incoming = Math.max(0, amount);
        int absorbed = Math.min(block, incoming);
        block -= absorbed;
        int hpDamage = incoming - absorbed;
        hp = Math.max(0, hp - hpDamage);
        return hpDamage;
    }

    public int applyPoison() {
        int poisonDamage = statuses.applyPoison();
        hp = Math.max(0, hp - poisonDamage);
        return poisonDamage;
    }

    public int heal(int amount) {
        int oldHp = hp;
        hp = Math.min(maxHp, hp + Math.max(0, amount));
        return hp - oldHp;
    }

    public void increaseMaxHp(int amount) {
        int increase = Math.max(0, amount);
        maxHp += increase;
        hp += increase;
    }

    protected void setHpForTesting(int hp) {
        this.hp = Math.clamp(hp, 0, maxHp);
    }
}
