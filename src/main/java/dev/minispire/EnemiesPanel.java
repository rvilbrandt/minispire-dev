package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public final class EnemiesPanel extends JPanel {
    private static final Color BACKGROUND = new Color(31, 27, 38);
    private static final Color TEXT = new Color(240, 235, 223);
    private static final Color MUTED = new Color(176, 164, 185);
    private static final Color ACCENT = new Color(225, 157, 74);

    private final JLabel title = new JLabel("GEGNER · KEIN KAMPF AKTIV");
    private final JPanel enemyRow = new JPanel();
    private final List<EnemyCardButton> enemyButtons = new ArrayList<>();
    private final IntConsumer selectTarget;
    private boolean acceptingTarget;

    public EnemiesPanel(IntConsumer selectTarget) {
        super(new java.awt.BorderLayout(0, 5));
        this.selectTarget = selectTarget;
        setOpaque(true);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        setPreferredSize(new Dimension(600, 205));

        title.setForeground(MUTED);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        add(title, java.awt.BorderLayout.NORTH);

        enemyRow.setLayout(new BoxLayout(enemyRow, BoxLayout.X_AXIS));
        enemyRow.setBackground(BACKGROUND);
        enemyRow.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        enemyRow.add(emptyMessage());

        JScrollPane scrollPane = new JScrollPane(enemyRow,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(73, 63, 82)));
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    public void showCombat(CombatViewState state) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showCombat(state));
            return;
        }
        acceptingTarget = state.acceptingTargetSelection();
        enemyButtons.clear();
        enemyRow.removeAll();

        for (int index = 0; index < state.enemies().size(); index++) {
            EnemyCardButton button = new EnemyCardButton(state.enemies().get(index), index + 1, acceptingTarget);
            int targetNumber = index + 1;
            button.addActionListener(event -> chooseTarget(targetNumber, button));
            enemyButtons.add(button);
            enemyRow.add(button);
            enemyRow.add(Box.createHorizontalStrut(10));
        }
        if (state.enemies().isEmpty()) {
            enemyRow.add(emptyMessage());
        }

        title.setText(acceptingTarget
                ? "GEGNER · ZIEL JETZT PER KLICK AUSWÄHLEN"
                : "GEGNER · %d LEBEND".formatted(state.enemies().size()));
        enemyRow.revalidate();
        enemyRow.repaint();
    }

    public void clearEnemies() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::clearEnemies);
            return;
        }
        acceptingTarget = false;
        enemyButtons.clear();
        enemyRow.removeAll();
        enemyRow.add(emptyMessage());
        title.setText("GEGNER · KEIN KAMPF AKTIV");
        enemyRow.revalidate();
        enemyRow.repaint();
    }

    private void chooseTarget(int targetNumber, EnemyCardButton selected) {
        if (!acceptingTarget) {
            return;
        }
        acceptingTarget = false;
        for (EnemyCardButton button : enemyButtons) {
            button.setTargetMode(false);
            button.setChosen(button == selected);
        }
        title.setText("GEGNER · ZIEL %d AUSGEWÄHLT".formatted(targetNumber));
        selectTarget.accept(targetNumber);
    }

    private static JLabel emptyMessage() {
        JLabel label = new JLabel("Gegner erscheinen beim Kampfstart.");
        label.setForeground(MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(25, 12, 25, 12));
        return label;
    }

    List<JButton> enemyButtons() {
        return List.copyOf(enemyButtons);
    }

    private static final class EnemyCardButton extends JButton {
        private static final Color CARD = new Color(59, 45, 52);
        private final EnemyView enemy;
        private final int number;
        private boolean targetMode;
        private boolean chosen;

        private EnemyCardButton(EnemyView enemy, int number, boolean targetMode) {
            this.enemy = enemy;
            this.number = number;
            this.targetMode = targetMode;
            setPreferredSize(new Dimension(210, 155));
            setMaximumSize(new Dimension(210, 155));
            setMinimumSize(new Dimension(210, 155));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(targetMode ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            setToolTipText(targetMode ? enemy.name() + " als Ziel auswählen" : enemy.intentDescription());
        }

        private void setTargetMode(boolean targetMode) {
            this.targetMode = targetMode;
            setCursor(targetMode ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        private void setChosen(boolean chosen) {
            this.chosen = chosen;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D canvas = (Graphics2D) graphics.create();
            try {
                canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isRollover() && targetMode ? new Color(79, 58, 62) : CARD;
                RoundRectangle2D card = new RoundRectangle2D.Double(2, 2, getWidth() - 5, getHeight() - 5, 18, 18);
                canvas.setColor(fill);
                canvas.fill(card);
                canvas.setStroke(new BasicStroke(targetMode || chosen ? 4f : 2f));
                canvas.setColor(chosen ? new Color(118, 205, 135) : targetMode ? ACCENT : intentColor(enemy.intentType()));
                canvas.draw(card);

                paintMonsterFace(canvas);
                paintName(canvas);
                paintHealth(canvas);
                paintStats(canvas);
                paintIntent(canvas);
            } finally {
                canvas.dispose();
            }
        }

        private void paintMonsterFace(Graphics2D canvas) {
            canvas.setColor(intentColor(enemy.intentType()).darker());
            canvas.fillOval(12, 11, 31, 27);
            canvas.setColor(TEXT);
            canvas.fillOval(20, 20, 4, 4);
            canvas.fillOval(31, 20, 4, 4);
            canvas.drawArc(21, 23, 13, 8, 190, 160);
        }

        private void paintName(Graphics2D canvas) {
            canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            canvas.setColor(TEXT);
            canvas.drawString(number + " · " + fit(canvas, enemy.name(), 135), 50, 29);
        }

        private void paintHealth(Graphics2D canvas) {
            int x = 12;
            int y = 45;
            int width = getWidth() - 24;
            int filled = enemy.maxHp() == 0 ? 0 : width * enemy.hp() / enemy.maxHp();
            canvas.setColor(new Color(38, 32, 42));
            canvas.fillRoundRect(x, y, width, 16, 8, 8);
            canvas.setColor(new Color(164, 61, 65));
            canvas.fillRoundRect(x, y, filled, 16, 8, 8);
            canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            canvas.setColor(Color.WHITE);
            String hp = "HP %d / %d".formatted(enemy.hp(), enemy.maxHp());
            canvas.drawString(hp, centerTextX(canvas, hp), y + 12);
        }

        private void paintStats(Graphics2D canvas) {
            canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            canvas.setColor(MUTED);
            canvas.drawString("Block: " + enemy.block(), 13, 78);
            canvas.drawString("Status: " + fit(canvas, enemy.statuses(), 128), 75, 78);
        }

        private void paintIntent(Graphics2D canvas) {
            int x = 11;
            int y = 89;
            int width = getWidth() - 22;
            canvas.setColor(new Color(40, 34, 45));
            canvas.fillRoundRect(x, y, width, 51, 12, 12);
            canvas.setColor(intentColor(enemy.intentType()));
            canvas.setStroke(new BasicStroke(2f));
            canvas.drawRoundRect(x, y, width, 51, 12, 12);
            canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            canvas.drawString("ANGESAGTES VERHALTEN", x + 9, y + 15);
            canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            canvas.setColor(TEXT);
            canvas.drawString(fit(canvas, enemy.intentDescription(), width - 18), x + 9, y + 36);
        }

        private int centerTextX(Graphics2D canvas, String text) {
            return (getWidth() - canvas.getFontMetrics().stringWidth(text)) / 2;
        }

        private static String fit(Graphics2D canvas, String text, int maximumWidth) {
            FontMetrics metrics = canvas.getFontMetrics();
            if (metrics.stringWidth(text) <= maximumWidth) {
                return text;
            }
            String shortened = text;
            while (!shortened.isEmpty() && metrics.stringWidth(shortened + "…") > maximumWidth) {
                shortened = shortened.substring(0, shortened.length() - 1);
            }
            return shortened + "…";
        }

        private static Color intentColor(IntentType type) {
            return switch (type) {
                case ATTACK -> new Color(224, 91, 83);
                case BLOCK -> new Color(90, 143, 211);
                case BUFF_STRENGTH -> new Color(184, 105, 207);
                case INFLICT_WEAKNESS -> new Color(214, 177, 78);
            };
        }
    }
}
