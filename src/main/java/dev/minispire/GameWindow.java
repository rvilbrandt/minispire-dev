package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.random.RandomGenerator;

public final class GameWindow extends JFrame {
    private static final Color BACKGROUND = new Color(25, 22, 31);
    private static final Color PANEL = new Color(39, 34, 48);
    private static final Color TEXT = new Color(238, 232, 218);
    private static final Color MUTED_TEXT = new Color(176, 164, 185);
    private static final Color ACCENT = new Color(205, 112, 72);

    private final JTextArea gameOutput = new JTextArea();
    private final JTextField commandInput = new JTextField();
    private final JButton submitButton = new JButton("Eingeben");
    private final PipedInputStream gameInput;
    private final PipedOutputStream userInput;
    private boolean started;

    public GameWindow() {
        super("Minispire");
        try {
            gameInput = new PipedInputStream();
            userInput = new PipedOutputStream(gameInput);
        } catch (IOException exception) {
            throw new IllegalStateException("Spieleingabe konnte nicht initialisiert werden", exception);
        }

        configureWindow();
        setContentPane(createContent());
        bindActions();
    }

    public void startGame() {
        if (started) {
            return;
        }
        started = true;
        PrintStream gamePrintStream = new PrintStream(
                new TextAreaOutputStream(gameOutput), true, StandardCharsets.UTF_8);
        Thread.ofVirtual()
                .name("minispire-game-loop")
                .start(() -> new Game(gameInput, gamePrintStream, RandomGenerator.getDefault()).run());
        commandInput.requestFocusInWindow();
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setSize(960, 700);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                closeInput();
            }
        });
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createGameView(), BorderLayout.CENTER);
        root.add(createInputArea(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("MINISPIRE");
        title.setForeground(ACCENT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));

        JLabel subtitle = new JLabel("Deckbuilding · Taktik · Risiko", SwingConstants.RIGHT);
        subtitle.setForeground(MUTED_TEXT);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createGameView() {
        gameOutput.setEditable(false);
        gameOutput.setLineWrap(true);
        gameOutput.setWrapStyleWord(true);
        gameOutput.setBackground(PANEL);
        gameOutput.setForeground(TEXT);
        gameOutput.setCaretColor(ACCENT);
        gameOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        gameOutput.setMargin(new java.awt.Insets(16, 18, 16, 18));

        JScrollPane scrollPane = new JScrollPane(gameOutput);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(74, 62, 82)));
        scrollPane.getViewport().setBackground(PANEL);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel createInputArea() {
        JPanel container = new JPanel(new BorderLayout(10, 7));
        container.setOpaque(false);

        JLabel hint = new JLabel("Zahl eingeben · 0 beendet den Zug · D zeigt das Deck");
        hint.setForeground(MUTED_TEXT);
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        commandInput.setBackground(PANEL);
        commandInput.setForeground(TEXT);
        commandInput.setCaretColor(ACCENT);
        commandInput.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        commandInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(94, 78, 103)),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));

        submitButton.setBackground(ACCENT);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        submitButton.setPreferredSize(new Dimension(120, 42));

        controls.add(commandInput, BorderLayout.CENTER);
        controls.add(submitButton, BorderLayout.EAST);
        container.add(hint, BorderLayout.NORTH);
        container.add(controls, BorderLayout.CENTER);
        return container;
    }

    private void bindActions() {
        commandInput.addActionListener(event -> submitCommand());
        submitButton.addActionListener(event -> submitCommand());
    }

    private void submitCommand() {
        String command = commandInput.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        try {
            userInput.write((command + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            userInput.flush();
            gameOutput.append("\n> " + command + "\n");
            gameOutput.setCaretPosition(gameOutput.getDocument().getLength());
            commandInput.setText("");
            commandInput.requestFocusInWindow();
        } catch (IOException exception) {
            commandInput.setEnabled(false);
            submitButton.setEnabled(false);
            gameOutput.append("\nDie Spieleingabe wurde geschlossen.\n");
        }
    }

    private void closeInput() {
        try {
            userInput.close();
        } catch (IOException ignored) {
            // Das Fenster wird bereits geschlossen; weitere Eingaben sind nicht relevant.
        }
    }
}
