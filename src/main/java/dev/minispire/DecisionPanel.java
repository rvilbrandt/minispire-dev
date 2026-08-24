package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public final class DecisionPanel extends JPanel {
    private static final Color BACKGROUND = new Color(31, 27, 38);
    private static final Color TEXT = new Color(240, 235, 223);
    private static final Color MUTED = new Color(176, 164, 185);
    private static final Color ACCENT = new Color(205, 112, 72);

    private final JLabel title = new JLabel("AKTION");
    private final JLabel description = new JLabel("Wähle deinen nächsten Kartenknoten.");
    private final JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final List<JButton> optionButtons = new ArrayList<>();

    public DecisionPanel() {
        super(new BorderLayout(0, 5));
        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        setPreferredSize(new Dimension(500, 115));

        JPanel heading = new JPanel(new BorderLayout(10, 0));
        heading.setOpaque(false);
        title.setForeground(ACCENT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        description.setForeground(MUTED);
        description.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        heading.add(title, BorderLayout.WEST);
        heading.add(description, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);

        options.setBackground(BACKGROUND);
        JScrollPane scrollPane = new JScrollPane(options,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(73, 63, 82)));
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void showDecision(ChoiceRequest request, IntConsumer submit) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showDecision(request, submit));
            return;
        }
        title.setText(request.title().toUpperCase());
        description.setText(request.description());
        optionButtons.clear();
        options.removeAll();
        for (ChoiceOption option : request.options()) {
            JButton button = new JButton(option.label());
            button.setBackground(ACCENT);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            button.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
            button.addActionListener(event -> {
                setButtonsEnabled(false);
                submit.accept(option.value());
            });
            optionButtons.add(button);
            options.add(button);
        }
        options.revalidate();
        options.repaint();
    }

    public void showSpecializedPrompt(ChoiceRequest request) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showSpecializedPrompt(request));
            return;
        }
        title.setText(request.title().toUpperCase());
        description.setText(request.description());
        optionButtons.clear();
        options.removeAll();
        JLabel hint = new JLabel(specializedHint(request.kind()));
        hint.setForeground(TEXT);
        hint.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        options.add(hint);
        options.revalidate();
        options.repaint();
    }

    private static String specializedHint(ChoiceKind kind) {
        return switch (kind) {
            case MAP_NODE -> "Goldenen Knoten auf der Karte anklicken";
            case COMBAT_ACTION -> "Handkarte wählen oder Zug beenden";
            case ENEMY_TARGET -> "Gold markiertes Monster anklicken";
            default -> "Option anklicken";
        };
    }

    private void setButtonsEnabled(boolean enabled) {
        optionButtons.forEach(button -> button.setEnabled(enabled));
    }

    List<JButton> optionButtons() {
        return List.copyOf(optionButtons);
    }
}
