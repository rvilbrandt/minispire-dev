package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.random.RandomGenerator;

public final class GameWindow extends JFrame {
    private static final Color BACKGROUND = new Color(25, 22, 31);
    private static final Color MUTED_TEXT = new Color(176, 164, 185);
    private static final Color ACCENT = new Color(205, 112, 72);

    private final ActivityPanel activityPanel = new ActivityPanel();
    private final DecisionPanel decisionPanel = new DecisionPanel();
    private final SwingGameInteraction interaction = new SwingGameInteraction(activityPanel, decisionPanel);
    private final MapPanel mapPanel = new MapPanel(interaction::submit);
    private final CardsPanel cardsPanel = new CardsPanel(interaction::submit, () -> interaction.submit(0));
    private final EnemiesPanel enemiesPanel = new EnemiesPanel(interaction::submit);
    private final PlayerStatsPanel playerStatsPanel = new PlayerStatsPanel();
    private boolean started;

    public GameWindow() {
        super("Minispire");
        configureWindow();
        setContentPane(createContent());
    }

    public void startGame() {
        if (started) {
            return;
        }
        started = true;
        GameObserver uiObserver = new GameObserver() {
            @Override
            public void mapChanged(MapViewState state) {
                mapPanel.showMap(state);
            }

            @Override
            public void combatChanged(CombatViewState state) {
                cardsPanel.showCombat(state);
                enemiesPanel.showCombat(state);
            }

            @Override
            public void combatEnded() {
                cardsPanel.clearHand();
                enemiesPanel.clearEnemies();
            }

            @Override
            public void deckChanged(java.util.List<CardView> deck) {
                cardsPanel.showDeck(deck);
            }

            @Override
            public void playerChanged(PlayerView player) {
                playerStatsPanel.showPlayer(player);
            }
        };
        Thread.ofVirtual()
                .name("minispire-game-loop")
                .start(() -> new Game(interaction, RandomGenerator.getDefault(), uiObserver).run());
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 680));
        setSize(1180, 850);
        setLocationRelativeTo(null);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainArea(), BorderLayout.CENTER);
        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        JPanel branding = new JPanel(new BorderLayout());
        branding.setOpaque(false);

        JLabel title = new JLabel("MINISPIRE");
        title.setForeground(ACCENT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));

        JLabel subtitle = new JLabel("Deckbuilding · Taktik · Risiko", SwingConstants.RIGHT);
        subtitle.setForeground(MUTED_TEXT);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        branding.add(title, BorderLayout.WEST);
        branding.add(subtitle, BorderLayout.EAST);
        header.add(branding, BorderLayout.NORTH);
        header.add(playerStatsPanel, BorderLayout.CENTER);
        return header;
    }

    private JSplitPane createCenterArea() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, createEncounterArea());
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.34);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(BACKGROUND);
        return splitPane;
    }

    private JSplitPane createEncounterArea() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, enemiesPanel, createInteractionArea());
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.38);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(BACKGROUND);
        return splitPane;
    }

    private JPanel createInteractionArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(activityPanel, BorderLayout.CENTER);
        panel.add(decisionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JSplitPane createMainArea() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createCenterArea(), cardsPanel);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.68);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(BACKGROUND);
        return splitPane;
    }

}
