package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public final class CardsPanel extends JPanel {
    private static final Color BACKGROUND = new Color(31, 27, 38);
    private static final Color CARD_BACKGROUND = new Color(54, 47, 63);
    private static final Color TEXT = new Color(240, 235, 223);
    private static final Color MUTED = new Color(176, 164, 185);
    private static final Color ACCENT = new Color(225, 157, 74);

    private final JPanel handCards = cardRow();
    private final JPanel deckCards = cardRow();
    private final JLabel handTitle = sectionTitle("HAND · 0 KARTEN");
    private final JLabel deckTitle = sectionTitle("DECK · 0 KARTEN");
    private final JButton confirmButton = new JButton("Karte bestätigen");
    private final List<JToggleButton> handButtons = new ArrayList<>();
    private final IntConsumer playCard;
    private List<CardView> displayedDeck = List.of();
    private int selectedIndex = -1;

    public CardsPanel(IntConsumer playCard) {
        super(new BorderLayout(0, 8));
        this.playCard = playCard;
        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        setPreferredSize(new Dimension(800, 245));

        JPanel galleries = new JPanel(new GridLayout(1, 2, 12, 0));
        galleries.setOpaque(false);
        galleries.add(createSection(handTitle, handCards));
        galleries.add(createSection(deckTitle, deckCards));
        add(galleries, BorderLayout.CENTER);

        confirmButton.setEnabled(false);
        confirmButton.setBackground(ACCENT);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(190, 38));
        confirmButton.addActionListener(event -> confirmSelection());

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(confirmButton, BorderLayout.EAST);
        add(actions, BorderLayout.SOUTH);
    }

    public void showCombat(CombatViewState state) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showCombat(state));
            return;
        }
        showDeckNow(state.deck());
        rebuildHand(state);
    }

    public void showDeck(List<CardView> cards) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showDeck(cards));
            return;
        }
        showDeckNow(List.copyOf(cards));
    }

    public void clearHand() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::clearHand);
            return;
        }
        selectedIndex = -1;
        handButtons.clear();
        handCards.removeAll();
        handCards.add(emptyMessage("Kein Kampf aktiv"));
        handTitle.setText("HAND · 0 KARTEN");
        confirmButton.setText("Karte bestätigen");
        confirmButton.setEnabled(false);
        refresh(handCards);
    }

    private void rebuildHand(CombatViewState state) {
        selectedIndex = -1;
        handButtons.clear();
        handCards.removeAll();
        ButtonGroup selectionGroup = new ButtonGroup();

        for (int index = 0; index < state.hand().size(); index++) {
            CardView card = state.hand().get(index);
            JToggleButton button = createHandCard(card, index, state);
            selectionGroup.add(button);
            handButtons.add(button);
            handCards.add(button);
            handCards.add(Box.createHorizontalStrut(8));
        }
        if (state.hand().isEmpty()) {
            handCards.add(emptyMessage("Keine Karten auf der Hand"));
        }

        handTitle.setText("HAND · %d KARTEN · %d ENERGIE".formatted(state.hand().size(), state.energy()));
        confirmButton.setText("Karte bestätigen");
        confirmButton.setEnabled(false);
        refresh(handCards);
    }

    private JToggleButton createHandCard(CardView card, int index, CombatViewState state) {
        JToggleButton button = new JToggleButton(cardHtml(card, index + 1));
        styleCardButton(button, card.type());
        boolean affordable = card.cost() <= state.energy();
        button.setEnabled(state.acceptingCardSelection() && affordable);
        button.setToolTipText(affordable ? card.description() : "Nicht genügend Energie");
        button.addItemListener(event -> updateSelectionStyle(button, card.type()));
        button.addActionListener(event -> {
            selectedIndex = index;
            confirmButton.setText("%s spielen".formatted(card.name()));
            confirmButton.setEnabled(true);
        });
        return button;
    }

    private void showDeckNow(List<CardView> cards) {
        if (displayedDeck.equals(cards)) {
            return;
        }
        displayedDeck = List.copyOf(cards);
        deckCards.removeAll();
        for (CardView card : cards) {
            deckCards.add(createDeckCard(card));
            deckCards.add(Box.createHorizontalStrut(8));
        }
        if (cards.isEmpty()) {
            deckCards.add(emptyMessage("Deck noch nicht verfügbar"));
        }
        deckTitle.setText("DECK · %d KARTEN".formatted(cards.size()));
        refresh(deckCards);
    }

    private JPanel createDeckCard(CardView card) {
        JPanel panel = new JPanel(new BorderLayout(4, 5));
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorFor(card.type()), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.setPreferredSize(new Dimension(128, 128));
        panel.setMaximumSize(new Dimension(128, 128));

        JLabel name = new JLabel(card.name());
        name.setForeground(TEXT);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        JLabel cost = new JLabel("⚡ " + card.cost(), SwingConstants.RIGHT);
        cost.setForeground(ACCENT);
        cost.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(name, BorderLayout.CENTER);
        top.add(cost, BorderLayout.EAST);

        JLabel description = new JLabel("<html><body style='width:100px'>" + card.description() + "</body></html>");
        description.setForeground(TEXT);
        description.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        JLabel type = new JLabel(card.type().displayName());
        type.setForeground(MUTED);
        type.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

        panel.add(top, BorderLayout.NORTH);
        panel.add(description, BorderLayout.CENTER);
        panel.add(type, BorderLayout.SOUTH);
        return panel;
    }

    private void confirmSelection() {
        if (selectedIndex < 0) {
            return;
        }
        int cardNumber = selectedIndex + 1;
        confirmButton.setEnabled(false);
        handButtons.forEach(button -> button.setEnabled(false));
        playCard.accept(cardNumber);
    }

    private JPanel createSection(JLabel title, JPanel cards) {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(false);
        section.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(cards,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(73, 63, 82)));
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }

    private static JPanel cardRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(BACKGROUND);
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return row;
    }

    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        return label;
    }

    private static JLabel emptyMessage(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
        return label;
    }

    private static void styleCardButton(JToggleButton button, CardType type) {
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.TOP);
        button.setBackground(CARD_BACKGROUND);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorFor(type), 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        button.setPreferredSize(new Dimension(142, 140));
        button.setMaximumSize(new Dimension(142, 140));
    }

    private static void updateSelectionStyle(JToggleButton button, CardType type) {
        Color border = button.isSelected() ? ACCENT : colorFor(type);
        button.setBackground(button.isSelected() ? new Color(82, 67, 65) : CARD_BACKGROUND);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, button.isSelected() ? 4 : 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private static String cardHtml(CardView card, int number) {
        return "<html><body style='width:112px;text-align:center'>"
                + "<b>" + number + " · " + card.name() + "</b><br>"
                + "<font color='#b0a4b9'>" + card.type().displayName() + "</font><br><br>"
                + card.description() + "<br><br><b>⚡ " + card.cost() + "</b>"
                + "</body></html>";
    }

    private static Color colorFor(CardType type) {
        return switch (type) {
            case ATTACK -> new Color(178, 77, 68);
            case DEFENSE -> new Color(65, 112, 178);
            case SPECIAL -> new Color(143, 91, 169);
        };
    }

    private static void refresh(JPanel panel) {
        panel.revalidate();
        panel.repaint();
    }

    List<JToggleButton> handButtons() {
        return List.copyOf(handButtons);
    }

    JButton confirmButton() {
        return confirmButton;
    }
}
