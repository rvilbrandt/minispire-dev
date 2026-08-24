package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwingGameInteractionTest {
    @Test
    void genericDecisionIsCompletedByButtonClick() throws Exception {
        ActivityPanel activity = new ActivityPanel();
        DecisionPanel decisions = new DecisionPanel();
        SwingGameInteraction interaction = new SwingGameInteraction(activity, decisions);
        ChoiceRequest request = new ChoiceRequest(ChoiceKind.REST_ACTION, "Rastplatz", "Wähle eine Aktion",
                List.of(new ChoiceOption(1, "Heilen"), new ChoiceOption(2, "Verbessern")), 1);

        CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> interaction.choose(request));
        waitForButtons(decisions);
        SwingUtilities.invokeAndWait(() -> decisions.optionButtons().get(1).doClick());

        assertEquals(2, result.get(2, TimeUnit.SECONDS));
    }

    @Test
    void messagesAreForwardedToSwingActivityList() throws Exception {
        ActivityPanel activity = new ActivityPanel();
        SwingGameInteraction interaction = new SwingGameInteraction(activity, new DecisionPanel());

        interaction.message("Kampf gewonnen!");
        SwingUtilities.invokeAndWait(() -> {
        });

        assertEquals(1, activity.messageCount());
        assertEquals("Kampf gewonnen!", activity.lastMessage());
    }

    private static void waitForButtons(DecisionPanel decisions) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (decisions.optionButtons().isEmpty() && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        assertEquals(2, decisions.optionButtons().size());
    }
}
