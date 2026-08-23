package dev.minispire;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class TextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

    TextAreaOutputStream(JTextArea textArea) {
        this.textArea = Objects.requireNonNull(textArea);
    }

    @Override
    public synchronized void write(int value) {
        if (value == '\n') {
            publishBufferedLine(true);
        } else if (value != '\r') {
            lineBuffer.write(value);
        }
    }

    @Override
    public synchronized void write(byte[] bytes, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        for (int index = offset; index < offset + length; index++) {
            write(bytes[index] & 0xff);
        }
    }

    @Override
    public synchronized void flush() {
        publishBufferedLine(false);
    }

    @Override
    public synchronized void close() throws IOException {
        flush();
        super.close();
    }

    private void publishBufferedLine(boolean newline) {
        if (lineBuffer.size() == 0 && !newline) {
            return;
        }
        String text = lineBuffer.toString(StandardCharsets.UTF_8) + (newline ? "\n" : "");
        lineBuffer.reset();
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
