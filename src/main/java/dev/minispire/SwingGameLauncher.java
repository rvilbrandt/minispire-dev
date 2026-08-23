package dev.minispire;

import javax.swing.SwingUtilities;

public final class SwingGameLauncher {
    private SwingGameLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
            window.startGame();
        });
    }
}
