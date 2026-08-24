package dev.minispire;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class SwingGameInteraction implements GameInteraction {
    private final ActivityPanel activityPanel;
    private final DecisionPanel decisionPanel;
    private final AtomicReference<CompletableFuture<Integer>> pendingChoice = new AtomicReference<>();

    public SwingGameInteraction(ActivityPanel activityPanel, DecisionPanel decisionPanel) {
        this.activityPanel = activityPanel;
        this.decisionPanel = decisionPanel;
    }

    @Override
    public void message(String message) {
        activityPanel.addMessage(message);
    }

    @Override
    public int choose(ChoiceRequest request) {
        CompletableFuture<Integer> answer = new CompletableFuture<>();
        if (!pendingChoice.compareAndSet(null, answer)) {
            throw new IllegalStateException("Es ist bereits eine Auswahl offen");
        }
        showRequest(request);
        return answer.join();
    }

    public void submit(int value) {
        CompletableFuture<Integer> answer = pendingChoice.getAndSet(null);
        if (answer != null) {
            answer.complete(value);
        }
    }

    private static boolean isSpecialized(ChoiceKind kind) {
        return kind == ChoiceKind.MAP_NODE
                || kind == ChoiceKind.COMBAT_ACTION
                || kind == ChoiceKind.ENEMY_TARGET;
    }

    private void showRequest(ChoiceRequest request) {
        Runnable display = () -> {
            if (isSpecialized(request.kind())) {
                decisionPanel.showSpecializedPrompt(request);
            } else {
                decisionPanel.showDecision(request, this::submit);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            display.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(display);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Anzeige der Spielauswahl wurde unterbrochen", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Spielauswahl konnte nicht angezeigt werden", exception.getCause());
        }
    }
}
