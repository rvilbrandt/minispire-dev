package dev.minispire;

public record MapNode(int floor, NodeType type) {
    @Override
    public String toString() {
        return "%s – %s".formatted(type.displayName(), type.description());
    }
}
