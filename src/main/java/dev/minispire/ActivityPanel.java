package dev.minispire;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

public final class ActivityPanel extends JPanel {
    private static final int MAX_MESSAGES = 100;
    private static final Color BACKGROUND = new Color(39, 34, 48);
    private static final Color TEXT = new Color(238, 232, 218);
    private static final Color MUTED = new Color(176, 164, 185);

    private final DefaultListModel<String> messages = new DefaultListModel<>();
    private final JList<String> messageList = new JList<>(messages);

    public ActivityPanel() {
        super(new BorderLayout(0, 6));
        setOpaque(true);
        setBackground(BACKGROUND);

        JLabel title = new JLabel("EREIGNISSE");
        title.setForeground(MUTED);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        add(title, BorderLayout.NORTH);

        messageList.setBackground(BACKGROUND);
        messageList.setForeground(TEXT);
        messageList.setSelectionBackground(BACKGROUND);
        messageList.setSelectionForeground(TEXT);
        messageList.setFocusable(false);
        messageList.setCellRenderer(new WrappedMessageRenderer());

        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(74, 62, 82)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addMessage(String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> addMessage(message));
            return;
        }
        if (messages.size() == MAX_MESSAGES) {
            messages.remove(0);
        }
        messages.addElement(message);
        messageList.ensureIndexIsVisible(messages.size() - 1);
    }

    int messageCount() {
        return messages.size();
    }

    String lastMessage() {
        return messages.isEmpty() ? "" : messages.lastElement();
    }

    private static final class WrappedMessageRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean selected, boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
            label.setText("<html><body style='width:390px;padding:4px 6px'>• " + value + "</body></html>");
            label.setBackground(BACKGROUND);
            label.setForeground(TEXT);
            label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            return label;
        }
    }
}
