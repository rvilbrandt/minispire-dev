package dev.minispire;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.random.RandomGenerator;

public final class Game {
    public static final int ACTS = 3;

    private final Scanner input;
    private final PrintStream output;
    private final RandomGenerator random;
    private final Player player;
    private final GameMap map;
    private boolean inputClosed;

    public Game(InputStream input, PrintStream output, RandomGenerator random) {
        this.input = new Scanner(input, StandardCharsets.UTF_8);
        this.output = output;
        this.random = random;
        this.player = new Player("Wanderer");
        this.map = new GameMap(random);
    }

    public Player player() {
        return player;
    }

    public void run() {
        printTitle();
        for (int act = 1; act <= ACTS && player.isAlive(); act++) {
            output.println("%n========== AKT %d ==========".formatted(act));
            for (int floor = 1; floor <= GameMap.FLOORS_PER_ACT && player.isAlive(); floor++) {
                printRunStatus(act, floor);
                List<MapNode> choices = map.choices(floor);
                MapNode selected = choices.get(choose("Wähle deinen Weg", choices, 1) - 1);
                output.println("%nDu betrittst: " + selected.type().displayName());
                resolveNode(selected.type(), act);
            }
            if (player.isAlive() && act < ACTS) {
                int healed = player.heal(player.maxHp() / 4);
                output.println("%nAkt %d geschafft! Auf dem Weg zum nächsten Akt heilst du %d HP.".formatted(act, healed));
            }
        }

        if (player.isAlive()) {
            output.println("%n*** SIEG! Du hast den Turm bezwungen. ***");
            output.println("Finales Deck: %d Karten | Gold: %d | Relikte: %d"
                    .formatted(player.deck().size(), player.gold(), player.relics().size()));
        } else {
            output.println("%n*** Du bist gefallen. Der Durchlauf endet hier. ***");
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
        output.println("Gegner: " + enemies.stream().map(enemy -> enemy.name() + " (" + enemy.hp() + " HP)")
                .reduce((left, right) -> left + ", " + right).orElse(""));
        Combat combat = new Combat(player, enemies, random);

        while (!combat.isWon() && !combat.isLost()) {
            output.println("%n--- Zug %d ---".formatted(combat.turn() + 1));
            printEvents(combat.startPlayerTurn());
            if (combat.isWon() || combat.isLost()) {
                break;
            }

            while (!combat.isWon() && player.energy() >= 0) {
                printCombatState(combat);
                String command = ask("Karte spielen (Nummer), [D]eck ansehen oder [0] Zug beenden: ").trim();
                if (inputClosed) {
                    break;
                }
                if (command.equalsIgnoreCase("d")) {
                    printDeck();
                    continue;
                }
                int selection = parseInt(command, -1);
                if (selection == 0) {
                    break;
                }
                if (selection < 1 || selection > combat.hand().size()) {
                    output.println("Bitte wähle eine vorhandene Karte.");
                    continue;
                }

                Card card = combat.hand().get(selection - 1);
                int target = 0;
                if (targetsEnemy(card) && combat.livingEnemies().size() > 1) {
                    target = chooseEnemy(combat.livingEnemies()) - 1;
                }
                PlayResult result = combat.playCard(selection - 1, target);
                output.println(result.message());
                if (!result.successful()) {
                    continue;
                }
                if (combat.isWon()) {
                    break;
                }
            }

            if (!combat.isWon()) {
                output.println("%nGegnerzug:");
                printEvents(combat.endPlayerTurn());
            }
        }

        if (combat.isWon()) {
            int healed = player.finishWonCombat();
            output.println("%nKampf gewonnen!" + (healed > 0 ? " Brennendes Blut heilt " + healed + " HP." : ""));
            grantCombatRewards(elite, boss);
        }
    }

    private void grantCombatRewards(boolean elite, boolean boss) {
        int baseGold = boss ? 90 : elite ? 45 + random.nextInt(16) : 20 + random.nextInt(16);
        int gold = player.hasRelic(Relic.GOLDEN_IDOL) ? baseGold * 5 / 4 : baseGold;
        player.addGold(gold);
        output.println("Belohnung: %d Gold.".formatted(gold));

        if (elite || boss) {
            grantRandomRelic();
        }
        chooseCardReward();
    }

    private void chooseCardReward() {
        List<Card> rewards = CardLibrary.randomRewards(random, 3);
        output.println("%nKartenbelohnung:");
        for (int i = 0; i < rewards.size(); i++) {
            output.println("  %d) %s".formatted(i + 1, rewards.get(i)));
        }
        output.println("  0) Überspringen");
        int choice = chooseNumber("Wähle eine Karte", 0, rewards.size(), 0);
        if (choice > 0) {
            Card card = rewards.get(choice - 1);
            player.addCard(card);
            output.println(card.name() + " wurde dem Deck hinzugefügt.");
        }
    }

    private void runRestSite() {
        output.println("1) Rasten – 30 % der maximalen HP heilen");
        output.println("2) Schmieden – eine Karte dauerhaft verbessern");
        int choice = chooseNumber("Aktion", 1, 2, 1);
        if (choice == 1) {
            int healed = player.heal((int) Math.ceil(player.maxHp() * 0.30));
            output.println("Du heilst %d HP.".formatted(healed));
        } else {
            upgradeCard();
        }
    }

    private void upgradeCard() {
        List<Card> upgradeable = player.deck().stream().filter(card -> !card.isUpgraded()).toList();
        if (upgradeable.isEmpty()) {
            output.println("Alle Karten sind bereits verbessert.");
            return;
        }
        output.println("%nVerbesserbare Karten:");
        for (int i = 0; i < upgradeable.size(); i++) {
            output.println("  %d) %s".formatted(i + 1, upgradeable.get(i)));
        }
        int choice = chooseNumber("Karte verbessern", 1, upgradeable.size(), 1);
        Card card = upgradeable.get(choice - 1);
        card.upgrade();
        output.println(card.name() + " wurde verbessert: " + card.description());
    }

    private void runEvent() {
        if (random.nextBoolean()) {
            output.println("Ein vergessener Altar verspricht Macht gegen Blut.");
            output.println("1) 8 HP opfern und ein Relikt erhalten");
            output.println("2) Weitergehen");
            int choice = chooseNumber("Entscheidung", 1, 2, 2);
            if (choice == 1) {
                int damage = player.takeDamage(8);
                output.println("Du verlierst %d HP.".formatted(damage));
                grantRandomRelic();
            }
        } else {
            output.println("Eine friedliche Quelle glitzert zwischen den Felsen.");
            output.println("1) Trinken und 15 HP heilen");
            output.println("2) Im Wasser schmieden und eine Karte verbessern");
            int choice = chooseNumber("Entscheidung", 1, 2, 1);
            if (choice == 1) {
                output.println("Du heilst %d HP.".formatted(player.heal(15)));
            } else {
                upgradeCard();
            }
        }
    }

    private void openTreasure() {
        if (random.nextBoolean()) {
            int gold = 55 + random.nextInt(31);
            player.addGold(gold);
            output.println("Die Truhe enthält %d Gold.".formatted(gold));
        } else {
            output.println("In der Truhe liegt ein Relikt.");
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
            output.println("Du besitzt bereits alle Relikte und erhältst stattdessen 75 Gold.");
            return;
        }
        Relic relic = available.get(random.nextInt(available.size()));
        player.addRelic(relic);
        output.println("Relikt erhalten: " + relic);
    }

    private void printCombatState(Combat combat) {
        output.println("%n%s: %d/%d HP | Block %d | Energie %d | Status: %s"
                .formatted(player.name(), player.hp(), player.maxHp(), player.block(), player.energy(), player.statuses()));
        List<Enemy> living = combat.livingEnemies();
        for (int i = 0; i < living.size(); i++) {
            Enemy enemy = living.get(i);
            output.println("G%d %s: %d/%d HP | Block %d | Status: %s | Absicht: %s"
                    .formatted(i + 1, enemy.name(), enemy.hp(), enemy.maxHp(), enemy.block(), enemy.statuses(),
                            enemy.intent().description(enemy)));
        }
        output.println("Hand (Nachziehen %d / Ablage %d):".formatted(combat.drawPileSize(), combat.discardPileSize()));
        for (int i = 0; i < combat.hand().size(); i++) {
            output.println("  %d) %s".formatted(i + 1, combat.hand().get(i)));
        }
    }

    private void printRunStatus(int act, int floor) {
        output.println("%nAkt %d | Ebene %d/%d | HP %d/%d | Gold %d | Deck %d"
                .formatted(act, floor, GameMap.FLOORS_PER_ACT, player.hp(), player.maxHp(), player.gold(), player.deck().size()));
    }

    private void printDeck() {
        output.println("%nDein Deck:");
        for (int i = 0; i < player.deck().size(); i++) {
            output.println("  %d) %s".formatted(i + 1, player.deck().get(i)));
        }
        output.println("Relikte: " + player.relics());
    }

    private int chooseEnemy(List<Enemy> enemies) {
        output.println("Ziel wählen:");
        for (int i = 0; i < enemies.size(); i++) {
            output.println("  %d) %s (%d HP)".formatted(i + 1, enemies.get(i).name(), enemies.get(i).hp()));
        }
        return chooseNumber("Ziel", 1, enemies.size(), 1);
    }

    private int choose(String prompt, List<?> choices, int defaultValue) {
        for (int i = 0; i < choices.size(); i++) {
            output.println("  %d) %s".formatted(i + 1, choices.get(i)));
        }
        return chooseNumber(prompt, 1, choices.size(), defaultValue);
    }

    private int chooseNumber(String prompt, int minimum, int maximum, int defaultValue) {
        while (true) {
            String value = ask("%s [%d-%d]: ".formatted(prompt, minimum, maximum));
            if (inputClosed) {
                return defaultValue;
            }
            int choice = parseInt(value.trim(), Integer.MIN_VALUE);
            if (choice >= minimum && choice <= maximum) {
                return choice;
            }
            output.println("Bitte gib eine Zahl zwischen %d und %d ein.".formatted(minimum, maximum));
        }
    }

    private String ask(String prompt) {
        output.print(prompt);
        if (!input.hasNextLine()) {
            inputClosed = true;
            return "";
        }
        return input.nextLine();
    }

    private void printEvents(List<String> events) {
        events.forEach(output::println);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static boolean targetsEnemy(Card card) {
        return switch (card.effect()) {
            case DAMAGE, DAMAGE_AND_VULNERABLE, POISON, DAMAGE_AND_DRAW -> true;
            default -> false;
        };
    }

    private void printTitle() {
        output.println("=================================");
        output.println("          M I N I S P I R E");
        output.println("=================================");
        output.println("Erklimme drei Akte. Wähle Wege, verbessere dein Deck und besiege die Bosse.");
    }
}
