import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationManager {
    // Color constants
    private static final Color WARNING_COLOR = new Color(255, 87, 87);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color URGENT_COLOR = new Color(255, 152, 0);
    private static final Color INFO_COLOR = new Color(59, 130, 246);

    private static Main mainApp = null;

    public static void setMainApp(Main mainApplication) {
        mainApp = mainApplication;
    }

    /**
     * Show deadline notification for a single task
     */
    public static boolean showDeadlineNotification(Task task) {
        boolean[] taskMarkedAsDone = {false};

        long timeLeft = task.getDeadline().getTime() - System.currentTimeMillis();
        long hours = timeLeft / (1000 * 60 * 60);
        long minutes = (timeLeft % (1000 * 60 * 60)) / (1000 * 60);
        String timeText = formatTimeText(hours, minutes);

        JDialog dialog = createNotificationDialog("⏰ TASK DEADLINE ALERT!");

        JPanel contentPanel = createImprovedNotificationContent(task, hours, minutes, timeText);
        JPanel buttonPanel = createNotificationButtons(task, dialog, taskMarkedAsDone);

        dialog.add(createHeaderPanel(), BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        showDialogWithEffects(dialog);

        return taskMarkedAsDone[0];
    }

    /**
     * UI for the buttons (FIXED: Added snooze button to the panel)
     */
    private static JPanel createNotificationButtons(Task task, JDialog dialog, boolean[] taskMarkedAsDone) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        JButton markDoneButton = createStyledButton("✓ Mark Done", SUCCESS_COLOR);
        JButton snoozeButton = createStyledButton("⏰ Snooze 10m", URGENT_COLOR);
        JButton closeButton = createStyledButton("✕ Close", Color.GRAY);

        // Mark as Done Logic
        markDoneButton.addActionListener(e -> {
            try {
                TaskDAO.updateTaskCompletion(task.getId(), true);
                task.setCompleted(true);
                if (mainApp != null) {
                    SwingUtilities.invokeLater(() -> mainApp.updateTaskInTable(task));
                }
                taskMarkedAsDone[0] = true;
                dialog.dispose();
                showSuccessNotification("Task completed!");
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        // Snooze Logic
        snoozeButton.addActionListener(e -> {
            try {
                Date snoozedUntil = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
                TaskDAO.updateTaskSnooze(task.getId(), snoozedUntil);
                task.setSnoozedUntil(snoozedUntil);
                dialog.dispose();
                showInfoNotification("Snoozed for 10 minutes.");
                scheduleReminder(task, 10);
            } catch (Exception ex) {
                showError("Snooze failed: " + ex.getMessage());
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        // ADDING BUTTONS TO PANEL
        buttonPanel.add(markDoneButton);
        buttonPanel.add(snoozeButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private static void scheduleReminder(Task task, int minutes) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    TaskDAO.updateTaskSnooze(task.getId(), null);
                    task.setSnoozedUntil(null);
                    task.setNotified(false);
                    SwingUtilities.invokeLater(() -> showDeadlineNotification(task));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, (long) minutes * 60 * 1000);
    }

    /**
     * Methods required by Main.java
     */
    public static void showBulkNotifications(List<Task> tasks) {
        for (Task t : tasks) showDeadlineNotification(t);
    }

    public static void showExpiredTasksNotification(List<Task> expiredTasks) {
        if (expiredTasks.isEmpty()) return;
        StringBuilder sb = new StringBuilder("Expired tasks:\n");
        for (Task t : expiredTasks) sb.append("- ").append(t.getText()).append("\n");
        JOptionPane.showMessageDialog(null, sb.toString(), "⚠️ Expired", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Helper UI Methods
     */
    private static JDialog createNotificationDialog(String title) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 320);
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);
        return dialog;
    }

    private static JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout());
        header.setBackground(WARNING_COLOR);
        JLabel label = new JLabel("TASK DEADLINE ALERT!");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(label);
        return header;
    }

    private static JPanel createImprovedNotificationContent(Task task, long h, long m, String time) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel text = new JLabel("<html><center>📝 " + task.getText() + "<br><br>Time: " + time + "</center></html>", SwingConstants.CENTER);
        text.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private static void showDialogWithEffects(JDialog dialog) {
        Toolkit.getDefaultToolkit().beep();
        dialog.toFront();
        dialog.requestFocus();
        dialog.setVisible(true);
    }

    private static JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        return b;
    }

    private static String formatTimeText(long h, long m) { return h + "h " + m + "m"; }
    public static void showSuccessNotification(String m) { JOptionPane.showMessageDialog(null, m); }
    public static void showError(String m) { JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE); }
    public static void showInfoNotification(String m) { JOptionPane.showMessageDialog(null, m); }
}