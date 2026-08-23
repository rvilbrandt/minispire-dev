package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextAreaOutputStreamTest {
    @Test
    void forwardsUtf8TextAndPromptsToTextArea() throws Exception {
        JTextArea textArea = new JTextArea();
        TextAreaOutputStream output = new TextAreaOutputStream(textArea);

        output.write("Wähle deinen Weg\nEingabe: ".getBytes(StandardCharsets.UTF_8));
        output.flush();
        SwingUtilities.invokeAndWait(() -> {
            // Wartet, bis alle zuvor eingeplanten Swing-Aktualisierungen verarbeitet wurden.
        });

        assertEquals("Wähle deinen Weg\nEingabe: ", textArea.getText());
    }
}
