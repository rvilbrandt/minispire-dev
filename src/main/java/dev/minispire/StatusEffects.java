package dev.minispire;

public final class StatusEffects {
    private int strength;
    private int weakness;
    private int vulnerability;
    private int poison;

    public int strength() {
        return strength;
    }

    public int weakness() {
        return weakness;
    }

    public int vulnerability() {
        return vulnerability;
    }

    public int poison() {
        return poison;
    }

    public void addStrength(int amount) {
        strength = Math.max(0, strength + amount);
    }

    public void addWeakness(int amount) {
        weakness = Math.max(weakness, amount);
    }

    public void addVulnerability(int amount) {
        vulnerability = Math.max(vulnerability, amount);
    }

    public void addPoison(int amount) {
        poison = Math.max(0, poison + amount);
    }

    int applyPoison() {
        int damage = poison;
        poison = Math.max(0, poison - 1);
        return damage;
    }

    void tickDurations() {
        weakness = Math.max(0, weakness - 1);
        vulnerability = Math.max(0, vulnerability - 1);
    }

    public void clear() {
        strength = 0;
        weakness = 0;
        vulnerability = 0;
        poison = 0;
    }

    public boolean isEmpty() {
        return strength == 0 && weakness == 0 && vulnerability == 0 && poison == 0;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        append(text, "Stärke", strength);
        append(text, "Schwäche", weakness);
        append(text, "Verwundbar", vulnerability);
        append(text, "Gift", poison);
        return text.isEmpty() ? "keine" : text.toString();
    }

    private static void append(StringBuilder text, String name, int value) {
        if (value <= 0) {
            return;
        }
        if (!text.isEmpty()) {
            text.append(", ");
        }
        text.append(name).append(' ').append(value);
    }
}
