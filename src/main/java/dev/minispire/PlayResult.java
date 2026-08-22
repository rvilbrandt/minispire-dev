package dev.minispire;

public record PlayResult(boolean successful, String message) {
    public static PlayResult success(String message) {
        return new PlayResult(true, message);
    }

    public static PlayResult failure(String message) {
        return new PlayResult(false, message);
    }
}
