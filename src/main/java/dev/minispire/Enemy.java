package dev.minispire;

import java.util.List;
import java.util.Objects;

public final class Enemy extends Combatant {
    private final List<EnemyIntent> pattern;
    private int intentIndex;

    public Enemy(String name, int maxHp, List<EnemyIntent> pattern) {
        super(name, maxHp);
        this.pattern = List.copyOf(Objects.requireNonNull(pattern));
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Ein Gegner benötigt mindestens eine Aktion");
        }
    }

    public EnemyIntent intent() {
        return pattern.get(intentIndex % pattern.size());
    }

    public String performIntent(Player player) {
        EnemyIntent action = intent();
        String result = switch (action.type()) {
            case ATTACK -> {
                int announced = attackDamage(action.value());
                int hpDamage = player.takeAttackDamage(announced);
                yield "%s greift für %d an (%d HP-Schaden).".formatted(name(), announced, hpDamage);
            }
            case BLOCK -> {
                gainBlock(action.value());
                yield "%s erhält %d Block.".formatted(name(), action.value());
            }
            case BUFF_STRENGTH -> {
                statuses().addStrength(action.value());
                yield "%s erhält %d Stärke.".formatted(name(), action.value());
            }
            case INFLICT_WEAKNESS -> {
                player.statuses().addWeakness(action.value());
                yield "%s verursacht %d Schwäche.".formatted(name(), action.value());
            }
        };
        intentIndex++;
        return result;
    }
}
