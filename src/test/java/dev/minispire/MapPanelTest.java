package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapPanelTest {
    @Test
    void rendersVisitedPathAndCurrentChoices() throws Exception {
        MapViewState state = new MapViewState(1, 2,
                List.of(new MapNode(1, NodeType.COMBAT)),
                List.of(new MapNode(2, NodeType.ELITE),
                        new MapNode(2, NodeType.REST),
                        new MapNode(2, NodeType.TREASURE)));
        MapPanel panel = new MapPanel();

        SwingUtilities.invokeAndWait(() -> {
            panel.setSize(340, 560);
            panel.showMap(state);
            BufferedImage image = new BufferedImage(340, 560, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            panel.paint(graphics);
            graphics.dispose();
        });

        assertEquals(state, panel.state());
    }
}
