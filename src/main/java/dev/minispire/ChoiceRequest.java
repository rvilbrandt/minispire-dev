package dev.minispire;

import java.util.List;

public record ChoiceRequest(ChoiceKind kind, String title, String description,
                            List<ChoiceOption> options, int defaultValue) {
    public ChoiceRequest {
        options = List.copyOf(options);
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Eine Auswahl benötigt mindestens eine Option");
        }
        if (options.stream().noneMatch(option -> option.value() == defaultValue)) {
            throw new IllegalArgumentException("Standardwert muss in den Optionen enthalten sein");
        }
    }
}
