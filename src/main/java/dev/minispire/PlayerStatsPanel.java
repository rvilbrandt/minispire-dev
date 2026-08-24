package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public final class PlayerStatsPanel extends JPanel {
    private static final Color BACKGROUND = new Color(39, 34, 48);
    private static final Color TEXT = new Color(240, 235, 223);
    private static final Color MUTED = new Color(176, 164, 185);
    private static final Color ACCENT = new Color(225, 157, 74);

    private final JLabel playerName = new JLabel("SPIELER");
    private final JProgressBar health = new JProgressBar();
    private final JLabel energy = statLabel("ENERGIE", "–");
    private final JLabel block = statLabel("BLOCK", "0");
    private final JLabel gold = statLabel("GOLD", "0");
    private final JLabel status = statLabel("STATUS", "keine");
    private final JLabel deck = statLabel("DECK", "0");
    private final JLabel relics = statLabel("RELIKTE", "0");

    public PlayerStatsPanel() {
        super(new BorderLayout(14, 0));
        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 64, 85)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        setPreferredSize(new Dimension(900, 68));

        playerName.setForeground(ACCENT);
        playerName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        playerName.setPreferredSize(new Dimension(105, 42));
        add(playerName, BorderLayout.WEST);

        health.setMinimum(0);
        health.setMaximum(1);
        health.setValue(1);
        health.setString("HP –");
        health.setStringPainted(true);
        health.setForeground(new Color(174, 65, 69));
        health.setBackground(new Color(25, 22, 31));
        health.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        health.setPreferredSize(new Dimension(210, 26));
        add(health, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 6, 7, 0));
        stats.setOpaque(false);
        stats.add(energy);
        stats.add(block);
        stats.add(gold);
        stats.add(status);
        stats.add(deck);
        stats.add(relics);
        stats.setPreferredSize(new Dimension(620, 44));
        add(stats, BorderLayout.EAST);
    }

    public void showPlayer(PlayerView player) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showPlayer(player));
            return;
        }
        playerName.setText(player.name().toUpperCase());
        health.setMaximum(player.maxHp());
        health.setValue(player.hp());
        health.setString("HP %d / %d".formatted(player.hp(), player.maxHp()));
        setValue(energy, "ENERGIE", player.inCombat() ? Integer.toString(player.energy()) : "–");
        setValue(block, "BLOCK", Integer.toString(player.block()));
        setValue(gold, "GOLD", Integer.toString(player.gold()));
        setValue(status, "STATUS", shortStatus(player.statuses()));
        status.setToolTipText(player.statuses());
        setValue(deck, "DECK", Integer.toString(player.deckSize()));
        setValue(relics, "RELIKTE", Integer.toString(player.relics().size()));
        relics.setToolTipText(player.relics().isEmpty() ? "Keine Relikte" : String.join(", ", player.relics()));
    }

    private static JLabel statLabel(String label, String value) {
        JLabel component = new JLabel(html(label, value), SwingConstants.CENTER);
        component.setForeground(TEXT);
        component.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        return component;
    }

    private static void setValue(JLabel component, String label, String value) {
        component.setText(html(label, value));
    }

    private static String html(String label, String value) {
        return "<html><div style='text-align:center'><font color='#b0a4b9'>" + label
                + "</font><br><b>" + value + "</b></div></html>";
    }

    private static String shortStatus(String statuses) {
        if (statuses.length() <= 13) {
            return statuses;
        }
        return statuses.substring(0, 11) + "…";
    }

    String healthText() {
        return health.getString();
    }

    String energyText() {
        return energy.getText();
    }
}
