package dev.minispire;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapPanel extends JPanel {
    private static final Color BACKGROUND = new Color(31, 27, 38);
    private static final Color PATH = new Color(103, 88, 112);
    private static final Color VISITED = new Color(117, 190, 128);
    private static final Color CURRENT = new Color(240, 192, 91);
    private static final Color UNKNOWN = new Color(79, 70, 88);
    private static final Color TEXT = new Color(240, 235, 223);
    private static final int NODE_WIDTH = 78;
    private static final int NODE_HEIGHT = 38;

    private MapViewState state;

    public MapPanel() {
        setOpaque(true);
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(340, 560));
        setMinimumSize(new Dimension(280, 420));
    }

    public void showMap(MapViewState newState) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showMap(newState));
            return;
        }
        state = newState;
        repaint();
    }

    MapViewState state() {
        return state;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D canvas = (Graphics2D) graphics.create();
        try {
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintHeader(canvas);
            if (state == null) {
                paintCentered(canvas, "Karte wird erstellt …", getHeight() / 2, new Color(171, 158, 179));
                return;
            }

            Map<Integer, List<VisualNode>> floors = buildVisualNodes();
            paintPaths(canvas, floors);
            for (int floor = 1; floor <= GameMap.FLOORS_PER_ACT; floor++) {
                paintFloorLabel(canvas, floor, floorY(floor));
                for (VisualNode node : floors.get(floor)) {
                    paintNode(canvas, node);
                }
            }
            paintLegend(canvas);
        } finally {
            canvas.dispose();
        }
    }

    private void paintHeader(Graphics2D canvas) {
        canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        canvas.setColor(TEXT);
        String title = state == null ? "KARTE" : "AKT %d · KARTE".formatted(state.act());
        canvas.drawString(title, 18, 27);
        canvas.setColor(new Color(77, 65, 86));
        canvas.drawLine(16, 39, getWidth() - 16, 39);
    }

    private Map<Integer, List<VisualNode>> buildVisualNodes() {
        Map<Integer, List<VisualNode>> floors = new HashMap<>();
        for (int floor = 1; floor <= GameMap.FLOORS_PER_ACT; floor++) {
            MapNode visited = findVisited(floor);
            if (visited != null) {
                floors.put(floor, List.of(new VisualNode(centerX(), floorY(floor), visited.type(), "✓", true, false)));
            } else if (floor == state.floor() && !state.choices().isEmpty()) {
                floors.put(floor, choiceNodes(floor));
            } else if (floor >= state.floor()) {
                floors.put(floor, unknownNodes(floor));
            } else {
                floors.put(floor, List.of());
            }
        }
        return floors;
    }

    private List<VisualNode> choiceNodes(int floor) {
        List<VisualNode> nodes = new ArrayList<>();
        int count = state.choices().size();
        for (int index = 0; index < count; index++) {
            nodes.add(new VisualNode(nodeX(index, count), floorY(floor), state.choices().get(index).type(),
                    Integer.toString(index + 1), false, true));
        }
        return nodes;
    }

    private List<VisualNode> unknownNodes(int floor) {
        int count = floor == GameMap.FLOORS_PER_ACT ? 1 : 3;
        List<VisualNode> nodes = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            NodeType knownType = floor == GameMap.FLOORS_PER_ACT ? NodeType.BOSS : null;
            nodes.add(new VisualNode(nodeX(index, count), floorY(floor), knownType, "?", false, false));
        }
        return nodes;
    }

    private void paintPaths(Graphics2D canvas, Map<Integer, List<VisualNode>> floors) {
        canvas.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int floor = 1; floor < GameMap.FLOORS_PER_ACT; floor++) {
            List<VisualNode> from = floors.get(floor);
            List<VisualNode> to = floors.get(floor + 1);
            for (VisualNode start : from) {
                for (VisualNode end : to) {
                    boolean active = start.visited() || start.current();
                    canvas.setColor(active ? new Color(139, 111, 102) : PATH);
                    canvas.draw(new Line2D.Double(start.x(), start.y(), end.x(), end.y()));
                }
            }
        }
    }

    private void paintNode(Graphics2D canvas, VisualNode node) {
        int x = node.x() - NODE_WIDTH / 2;
        int y = node.y() - NODE_HEIGHT / 2;
        RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, NODE_WIDTH, NODE_HEIGHT, 16, 16);

        canvas.setColor(node.type() == null ? UNKNOWN : colorFor(node.type()));
        canvas.fill(shape);
        canvas.setStroke(new BasicStroke(node.current() ? 3f : 2f));
        canvas.setColor(node.visited() ? VISITED : node.current() ? CURRENT : new Color(125, 109, 134));
        canvas.draw(shape);

        String name = node.type() == null ? "Unbekannt" : node.type().displayName();
        canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        canvas.setColor(TEXT);
        FontMetrics metrics = canvas.getFontMetrics();
        canvas.drawString(name, node.x() - metrics.stringWidth(name) / 2, node.y() + 4);

        canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        canvas.setColor(node.visited() ? VISITED : CURRENT);
        canvas.drawString(node.marker(), x + 6, y + 14);
    }

    private void paintFloorLabel(Graphics2D canvas, int floor, int y) {
        canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        canvas.setColor(new Color(143, 130, 151));
        canvas.drawString("E" + floor, 8, y + 4);
    }

    private void paintLegend(Graphics2D canvas) {
        canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        canvas.setColor(new Color(171, 158, 179));
        String text = state.choices().isEmpty()
                ? "Aktueller Weg"
                : "Goldener Rand = jetzt wählbar";
        canvas.drawString(text, 18, getHeight() - 13);
    }

    private void paintCentered(Graphics2D canvas, String text, int y, Color color) {
        canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        canvas.setColor(color);
        int width = canvas.getFontMetrics().stringWidth(text);
        canvas.drawString(text, (getWidth() - width) / 2, y);
    }

    private MapNode findVisited(int floor) {
        return state.visited().stream().filter(node -> node.floor() == floor).findFirst().orElse(null);
    }

    private int floorY(int floor) {
        int top = 72;
        int bottom = Math.max(top + 80, getHeight() - 58);
        double step = (bottom - top) / (double) (GameMap.FLOORS_PER_ACT - 1);
        return (int) Math.round(bottom - (floor - 1) * step);
    }

    private int nodeX(int index, int count) {
        if (count == 1) {
            return centerX();
        }
        int margin = NODE_WIDTH / 2 + 13;
        return margin + index * (getWidth() - 2 * margin) / (count - 1);
    }

    private int centerX() {
        return getWidth() / 2;
    }

    private static Color colorFor(NodeType type) {
        return switch (type) {
            case COMBAT -> new Color(126, 62, 62);
            case ELITE -> new Color(104, 62, 130);
            case EVENT -> new Color(49, 105, 112);
            case REST -> new Color(56, 107, 76);
            case TREASURE -> new Color(133, 103, 45);
            case BOSS -> new Color(151, 66, 47);
        };
    }

    private record VisualNode(int x, int y, NodeType type, String marker, boolean visited, boolean current) {
    }
}
