package dev.minispire;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void clickSubmitsCurrentNodeAndPreventsDuplicateSelection() throws Exception {
        AtomicInteger selectedNode = new AtomicInteger();
        MapPanel panel = new MapPanel(selectedNode::set);
        MapViewState state = new MapViewState(1, 2,
                List.of(new MapNode(1, NodeType.COMBAT)),
                List.of(new MapNode(2, NodeType.ELITE),
                        new MapNode(2, NodeType.REST),
                        new MapNode(2, NodeType.TREASURE)));

        SwingUtilities.invokeAndWait(() -> {
            panel.setSize(340, 560);
            panel.showMap(state);
            Point center = panel.choiceCenter(1);
            panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                    0, center.x, center.y, 1, false, MouseEvent.BUTTON1));

            Point otherCenter = panel.choiceCenter(0);
            panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                    0, otherCenter.x, otherCenter.y, 1, false, MouseEvent.BUTTON1));
        });

        assertEquals(2, selectedNode.get());
    }
}
