package dev.minispire;

public final class App {
    private App() {
    }

    public static String greeting() {
        return "Hello, Java 21!";
    }

    public static void main(String[] args) {
        SwingGameLauncher.launch();
    }
}
