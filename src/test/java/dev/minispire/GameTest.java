package dev.minispire;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    @Test
    void startsAndHandlesClosedInputWithoutLoopingForever() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Game game = new Game(new ByteArrayInputStream(new byte[0]),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), new Random(3));

        game.run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("M I N I S P I R E"));
        assertTrue(output.contains("Der Durchlauf endet hier"));
    }
}
