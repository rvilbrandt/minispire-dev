package dev.minispire;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class Game {
    public static final int ACTS = 3;

    private final GameInteraction interaction;
    private final RandomGenerator random;
    private final Player player;
    private final GameMap map;
    private final GameObserver observer;
    public Game(GameInteraction interaction, RandomGenerator random, GameObserver observer) {
        this.interaction = interaction;
        this.random = random;
        this.player = new Player("Wanderer");
        this.map = new GameMap(random);
        this.observer = observer;
    }

    public Player player() {
        return player;
    }

    public void run() {
        printTitle();
        notifyPlayerChanged(false);
        notifyDeckChanged();
        for (int act = 1; act <= ACTS && player.isAlive(); act++) {
            List<MapNode> visitedNodes = new ArrayList<>();
            message("Akt %d beginnt.".formatted(act));
            for (int floor = 1; floor <= GameMap.FLOORS_PER_ACT && player.isAlive(); floor++) {
                printRunStatus(act, floor);
                List<MapNode> choices = map.choices(floor);
                observer.mapChanged(new MapViewState(act, floor, visitedNodes, choices));
                List<ChoiceOption> nodeOptions = indexedOptions(choices.stream().map(MapNode::toString).toList());
                int nodeChoice = choose(ChoiceKind.MAP_NODE, "Weg wählen",
                        "Wähle den nächsten Knoten auf der Karte.", nodeOptions, 1);
                MapNode selected = choices.get(nodeChoice - 1);
                visitedNodes.add(selected);
                observer.mapChanged(new MapViewState(act, floor, visitedNodes, List.of()));
                message("Du betrittst: " + selected.type().displayName());
                resolveNode(selected.type(), act);
                notifyPlayerChanged(false);
            }
            if (player.isAlive() && act < ACTS) {
                int healed = player.heal(player.maxHp() / 4);
                message("Akt %d geschafft! Auf dem Weg zum nächsten Akt heilst du %d HP.".formatted(act, healed));
                notifyPlayerChanged(false);
            }
        }

        if (player.isAlive()) {
            message("SIEG! Du hast den Turm bezwungen.");
        } else {
            message("Du bist gefallen. Der Durchlauf endet hier.");
        }
    }

    private void resolveNode(NodeType type, int act) {
        switch (type) {
            case COMBAT -> runCombat(EnemyFactory.normalEncounter(act, random), false, false);
            case ELITE -> runCombat(EnemyFactory.eliteEncounter(act, random), true, false);
            case BOSS -> runCombat(EnemyFactory.bossEncounter(act), true, true);
            case EVENT -> runEvent();
            case REST -> runRestSite();
            case TREASURE -> openTreasure();
        }
    }

    private void runCombat(List<Enemy> enemies, boolean elite, boolean boss) {
        message("Kampf gegen " + enemies.stream().map(Enemy::name)
                .reduce((left, right) -> left + " und " + right).orElse(""));
        Combat combat = new Combat(player, enemies, random);

        while (!combat.isWon() && !combat.isLost()) {
            message("Zug %d beginnt.".formatted(combat.turn() + 1));
            printEvents(combat.startPlayerTurn());
            if (combat.isWon() || combat.isLost()) {
                break;
            }

            while (!combat.isWon() && player.energy() >= 0) {
                notifyCombatChanged(combat, true, false);
                List<ChoiceOption> actions = new ArrayList<>();
                actions.add(new ChoiceOption(0, "Zug beenden"));
                for (int index = 0; index < combat.hand().size(); index++) {
                    actions.add(new ChoiceOption(index + 1, combat.hand().get(index).toString()));
                }
                int selection = choose(ChoiceKind.COMBAT_ACTION, "Spielerzug",
                        "Wähle eine Handkarte oder beende den Zug.", actions, 0);
                if (selection == 0) {
                    notifyCombatChanged(combat, false, false);
                    break;
                }
                if (selection < 1 || selection > combat.hand().size()) {
                    message("Diese Handkarte ist nicht mehr verfügbar.");
                    continue;
                }

                Card card = combat.hand().get(selection - 1);
                int target = 0;
                if (targetsEnemy(card) && combat.livingEnemies().size() > 1) {
                    notifyCombatChanged(combat, false, true);
                    target = chooseEnemy(combat.livingEnemies()) - 1;
                }
                PlayResult result = combat.playCard(selection - 1, target);
                message(result.message());
                notifyCombatChanged(combat, false, false);
                if (!result.successful()) {
                    continue;
                }
                if (combat.isWon()) {
                    break;
                }
            }

            if (!combat.isWon()) {
                notifyCombatChanged(combat, false, false);
                message("Die Gegner führen ihre Aktionen aus.");
                printEvents(combat.endPlayerTurn());
                notifyCombatChanged(combat, false, false);
            }
        }

        observer.combatEnded();

        if (combat.isWon()) {
            int healed = player.finishWonCombat();
            message("Kampf gewonnen!" + (healed > 0 ? " Brennendes Blut heilt " + healed + " HP." : ""));
            grantCombatRewards(elite, boss);
        }
        notifyPlayerChanged(false);
    }

    private void grantCombatRewards(boolean elite, boolean boss) {
        int baseGold = boss ? 90 : elite ? 45 + random.nextInt(16) : 20 + random.nextInt(16);
        int gold = player.hasRelic(Relic.GOLDEN_IDOL) ? baseGold * 5 / 4 : baseGold;
        player.addGold(gold);
        message("Belohnung: %d Gold.".formatted(gold));
        notifyPlayerChanged(false);

        if (elite || boss) {
            grantRandomRelic();
        }
        chooseCardReward();
    }

    private void chooseCardReward() {
        List<Card> rewards = CardLibrary.randomRewards(random, 3);
        List<ChoiceOption> options = new ArrayList<>();
        options.add(new ChoiceOption(0, "Keine Karte nehmen"));
        for (int i = 0; i < rewards.size(); i++) {
            options.add(new ChoiceOption(i + 1, rewards.get(i).toString()));
        }
        int choice = choose(ChoiceKind.CARD_REWARD, "Kartenbelohnung",
                "Füge eine Karte deinem Deck hinzu oder überspringe die Belohnung.", options, 0);
        if (choice > 0) {
            Card card = rewards.get(choice - 1);
            player.addCard(card);
            notifyDeckChanged();
            message(card.name() + " wurde dem Deck hinzugefügt.");
        }
    }

    private void runRestSite() {
        int choice = choose(ChoiceKind.REST_ACTION, "Rastplatz", "Wähle eine Aktion.", List.of(
                new ChoiceOption(1, "Rasten – 30 % der maximalen HP heilen"),
                new ChoiceOption(2, "Schmieden – eine Karte dauerhaft verbessern")
        ), 1);
        if (choice == 1) {
            int healed = player.heal((int) Math.ceil(player.maxHp() * 0.30));
            message("Du heilst %d HP.".formatted(healed));
        } else {
            upgradeCard();
        }
    }

    private void upgradeCard() {
        List<Card> upgradeable = player.deck().stream().filter(card -> !card.isUpgraded()).toList();
        if (upgradeable.isEmpty()) {
            message("Alle Karten sind bereits verbessert.");
            return;
        }
        List<ChoiceOption> options = new ArrayList<>();
        for (int i = 0; i < upgradeable.size(); i++) {
            options.add(new ChoiceOption(i + 1, upgradeable.get(i).toString()));
        }
        int choice = choose(ChoiceKind.CARD_UPGRADE, "Karte verbessern",
                "Wähle eine Karte für die dauerhafte Verbesserung.", options, 1);
        Card card = upgradeable.get(choice - 1);
        card.upgrade();
        notifyDeckChanged();
        message(card.name() + " wurde verbessert: " + card.description());
    }

    private void runEvent() {
        if (random.nextBoolean()) {
            int choice = choose(ChoiceKind.EVENT_ACTION, "Vergessener Altar",
                    "Der Altar verspricht Macht gegen Blut.", List.of(
                            new ChoiceOption(1, "8 HP opfern und ein Relikt erhalten"),
                            new ChoiceOption(2, "Weitergehen")
                    ), 2);
            if (choice == 1) {
                int damage = player.takeDamage(8);
                message("Du verlierst %d HP.".formatted(damage));
                grantRandomRelic();
            }
        } else {
            int choice = choose(ChoiceKind.EVENT_ACTION, "Friedliche Quelle",
                    "Eine friedliche Quelle glitzert zwischen den Felsen.", List.of(
                            new ChoiceOption(1, "Trinken und 15 HP heilen"),
                            new ChoiceOption(2, "Im Wasser eine Karte verbessern")
                    ), 1);
            if (choice == 1) {
                message("Du heilst %d HP.".formatted(player.heal(15)));
            } else {
                upgradeCard();
            }
        }
    }

    private void openTreasure() {
        if (random.nextBoolean()) {
            int gold = 55 + random.nextInt(31);
            player.addGold(gold);
            message("Die Truhe enthält %d Gold.".formatted(gold));
        } else {
            message("In der Truhe liegt ein Relikt.");
            grantRandomRelic();
        }
    }

    private void grantRandomRelic() {
        List<Relic> available = new ArrayList<>();
        for (Relic relic : Relic.values()) {
            if (!player.hasRelic(relic)) {
                available.add(relic);
            }
        }
        if (available.isEmpty()) {
            player.addGold(75);
            message("Du besitzt bereits alle Relikte und erhältst stattdessen 75 Gold.");
            return;
        }
        Relic relic = available.get(random.nextInt(available.size()));
        player.addRelic(relic);
        message("Relikt erhalten: " + relic);
    }

    private void printRunStatus(int act, int floor) {
        message("Akt %d · Ebene %d von %d".formatted(act, floor, GameMap.FLOORS_PER_ACT));
    }

    private int chooseEnemy(List<Enemy> enemies) {
        List<ChoiceOption> options = new ArrayList<>();
        for (int i = 0; i < enemies.size(); i++) {
            options.add(new ChoiceOption(i + 1, enemies.get(i).name()));
        }
        return choose(ChoiceKind.ENEMY_TARGET, "Ziel wählen",
                "Klicke das gewünschte Monster an.", options, 1);
    }

    private int choose(ChoiceKind kind, String title, String description,
                       List<ChoiceOption> options, int defaultValue) {
        return interaction.choose(new ChoiceRequest(kind, title, description, options, defaultValue));
    }

    private void printEvents(List<String> events) {
        events.forEach(this::message);
    }

    private static List<ChoiceOption> indexedOptions(List<String> labels) {
        List<ChoiceOption> options = new ArrayList<>();
        for (int index = 0; index < labels.size(); index++) {
            options.add(new ChoiceOption(index + 1, labels.get(index)));
        }
        return options;
    }

    private static boolean targetsEnemy(Card card) {
        return switch (card.effect()) {
            case DAMAGE, DAMAGE_AND_VULNERABLE, POISON, DAMAGE_AND_DRAW -> true;
            default -> false;
        };
    }

    private void printTitle() {
        message("Willkommen bei Minispire. Erklimme drei Akte und besiege die Bosse.");
    }

    private void message(String message) {
        interaction.message(message);
    }

    private void notifyDeckChanged() {
        observer.deckChanged(CombatViewState.toViews(player.deck()));
        notifyPlayerChanged(false);
    }

    private void notifyCombatChanged(Combat combat, boolean acceptingCards, boolean acceptingTarget) {
        observer.combatChanged(CombatViewState.from(combat, acceptingCards, acceptingTarget));
        notifyPlayerChanged(true);
    }

    private void notifyPlayerChanged(boolean inCombat) {
        observer.playerChanged(PlayerView.from(player, inCombat));
    }
}
