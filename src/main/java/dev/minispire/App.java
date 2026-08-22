package dev.minispire;

import java.util.random.RandomGenerator;

public final class App {
    private App() {
    }

    public static String greeting() {
        return "Hello, Java 21!";
    }

    public static void main(String[] args) {
        new Game(System.in, System.out, RandomGenerator.getDefault()).run();
    }
}
