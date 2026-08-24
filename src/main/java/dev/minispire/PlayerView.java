package dev.minispire;

import java.util.List;

public record PlayerView(String name, int hp, int maxHp, int block, int energy, int gold,
                         String statuses, int deckSize, List<String> relics, boolean inCombat) {
    public PlayerView {
        relics = List.copyOf(relics);
    }

    public static PlayerView from(Player player, boolean inCombat) {
        List<String> relics = player.relics().stream().map(Relic::displayName).toList();
        return new PlayerView(player.name(), player.hp(), player.maxHp(), player.block(), player.energy(),
                player.gold(), player.statuses().toString(), player.deck().size(), relics, inCombat);
    }
}
