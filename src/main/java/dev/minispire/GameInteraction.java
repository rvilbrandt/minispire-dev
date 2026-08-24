package dev.minispire;

public interface GameInteraction {
    void message(String message);

    int choose(ChoiceRequest request);
}
