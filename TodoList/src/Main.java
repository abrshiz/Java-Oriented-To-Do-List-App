import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public class Main extends JFrame {

    // ==================== GUI COMPONENTS ====================
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private JTextField taskField;
    private JComboBox<String> categoryCombo;
    private JSpinner dateSpinner;
    private JComboBox<String> filterCombo;
    private JComboBox<String> statusCombo; // ADDED: Status combo as class variable
    private TableRowSorter<DefaultTableModel> rowSorter; // Added for Search

    // ==================== DATA ====================
    private List<Task> tasks;
    private javax.swing.Timer notificationTimer;

    // ==================== CONSTRUCTOR ====================
    public Main() {
        try {
            TaskDAO.initializeDatabase();
            tasks = TaskDAO.getAllTasks();
            System.out.println("✅ Loaded " + tasks.size() + " tasks from database");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage() +
                            "\nStarting with empty task list.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            tasks = new ArrayList<>();
        }

        initializeUI();
        NotificationManager.setMainApp(this);
        startNotificationChecker();
        checkInitialNotifications();
        addTableContextMenu(); // Initialize Context Menu
    }

    // ==================== GUI INITIALIZATION ====================
    private void initializeUI() {
        setTitle("TO DO LIST ");
        // Note: Ensure this path is correct for your project structure
        ImageIcon image = new ImageIcon("TodoList/assets/todoIcon.jpg");
        setIconImage(image.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(255, 255, 255));

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Task Manager");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 0, 0));

        JLabel subtitleLabel = new JLabel("<html>Capture your tasks instantly and stay organized. <br>" +
                "Manage deadlines, priorities, and daily checklists.<br>" +
                "Achieve more with the simple, powerful to-do list.</html>");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(0, 0, 0));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(new Color(255, 255, 255));
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setBackground(Color.WHITE);
        JTextField searchBar = new JTextField(15);
        JLabel searchLabel = new JLabel("Search tasks:");

        // SEARCH LOGIC: Add DocumentListener for live filtering
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchBar.getText();
                if (rowSorter == null) return;
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    try {
                        // Filter Column 1 (Task Name) using Regex Case-Insensitive
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                    } catch (java.util.regex.PatternSyntaxException pse) {
                        // Ignore invalid regex while typing
                    }
                }
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchBar);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.SOUTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filterPanel.setBackground(new Color(255, 255, 255));

        String[] categories = {"All", "Work", "Personal", "Other"};
        filterCombo = new JComboBox<>(categories);
        filterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        filterCombo.setPreferredSize(new Dimension(100, 35));
        filterCombo.setBackground(Color.ORANGE);
        filterCombo.addActionListener((ActionEvent e) -> filterTasks());

        String[] statuses = {"All Status", "Active", "Due Soon", "Expired", "Completed"};
        statusCombo = new JComboBox<>(statuses); // USING CLASS VARIABLE NOW
        statusCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        statusCombo.setPreferredSize(new Dimension(100, 35));
        statusCombo.setBackground(Color.ORANGE);
        statusCombo.addActionListener((ActionEvent e) -> filterTasks()); // CALLING filterTasks()

        filterPanel.add(filterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(statusCombo);
        panel.add(filterPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 255, 255), 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Added "ID" as a 7th column (index 6) to keep data synced
        String[] columnNames = {"Done", "Task", "Category", "Deadline", "Status", "Action", "ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 5;
            }
        };

        taskTable = new JTable(tableModel);

        // Initialize Sorter
        rowSorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(rowSorter);

        taskTable.setGridColor(new Color(0, 0, 0));
        taskTable.setRowHeight(40);
        taskTable.setFont(new Font("Arial", Font.PLAIN, 14));
        taskTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        taskTable.getTableHeader().setBackground(new Color(0, 0, 0));
        taskTable.getTableHeader().setForeground(new Color(255, 255, 255));
        taskTable.setBackground(Color.ORANGE);

        // Styling Columns
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        // HIDE THE ID COLUMN (Index 6)
        taskTable.removeColumn(taskTable.getColumnModel().getColumn(6));

        // Checkbox Listener - Use ID lookup for safety
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 0 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int modelRow = e.getFirstRow();
                if (modelRow >= 0) {
                    int taskId = (int) tableModel.getValueAt(modelRow, 6);
                    boolean completed = (Boolean) tableModel.getValueAt(modelRow, 0);

                    // Update local list
                    for(Task t : tasks) {
                        if(t.getId() == taskId) {
                            t.setCompleted(completed);
                            try {
                                TaskDAO.updateTaskCompletion(taskId, completed);
                            } catch (SQLException ex) {
                                NotificationManager.showError("DB Update Failed: " + ex.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        });

        taskTable.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("Delete");
            btn.setFont(new Font("Arial", Font.BOLD, 11));
            btn.setBackground(new Color(255, 0, 15));
            btn.setForeground(Color.BLACK);
            return btn;
        });

        // FIXED DELETE LOGIC: Use getTaskFromRow helper
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = taskTable.columnAtPoint(e.getPoint());
                int viewRow = taskTable.rowAtPoint(e.getPoint());

                if (viewColumn == 5 && viewRow >= 0) {
                    Task taskToDelete = getTaskFromRow(viewRow);
                    if (taskToDelete == null) return;

                    int confirm = JOptionPane.showConfirmDialog(Main.this,
                            "Delete task: " + taskToDelete.getText() + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            TaskDAO.deleteTask(taskToDelete.getId());
                            tasks.remove(taskToDelete);
                            filterTasks();
                            NotificationManager.showSuccessNotification("Task deleted!");
                        } catch (SQLException ex) {
                            NotificationManager.showError("Delete failed: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        filterTasks();

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==================== INDEX FIX HELPER ====================
    private Task getTaskFromRow(int viewRow) {
        if (viewRow == -1) return null;
        // Convert the clicked row index to the underlying data model index
        int modelRow = taskTable.convertRowIndexToModel(viewRow);
        // Get the ID from our hidden column 6
        int taskId = (int) tableModel.getValueAt(modelRow, 6);

        return tasks.stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .orElse(null);
    }

    // UNIFIED FILTERING METHOD
    private void filterTasks() {
        tableModel.setRowCount(0);
        String selectedCategory = (String) filterCombo.getSelectedItem();
        String selectedStatus = (String) statusCombo.getSelectedItem();

        for (Task task : tasks) {
            // Get the task's current status
            String taskStatus = getTaskStatus(task);

            // Apply category filter
            boolean categoryMatch = selectedCategory.equals("All") ||
                    task.getCategory().equals(selectedCategory);

            // Apply status filter
            boolean statusMatch = selectedStatus.equals("All Status") ||
                    taskStatus.equals(selectedStatus);

            // Only show if both filters match
            if (categoryMatch && statusMatch) {
                addTaskToTable(task);
            }
        }
    }

    private void addTaskToTable(Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String deadlineStr = sdf.format(task.getDeadline());
        String status = getTaskStatus(task);

        String statusWithColor = status;
        if (status.equals("Expired")) {
            statusWithColor = "<html><font color='red'>⏰ EXPIRED</font></html>";
        } else if (status.equals("Due Soon")) {
            statusWithColor = "<html><font color='orange'>⚠️ Due Soon</font></html>";
        } else if (status.equals("Completed")) {
            statusWithColor = "<html><font color='green'>✓ Completed</font></html>";
        } else if (status.equals("Active")) {
            statusWithColor = "<html><font color='blue'>✓ Active</font></html>";
        }

        // Index 6 is the ID
        Object[] rowData = {
                task.isCompleted(),
                task.getText(),
                task.getCategory(),
                deadlineStr,
                statusWithColor,
                "Delete",
                task.getId()
        };
        tableModel.addRow(rowData);
    }

    private void addTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem markCompletedItem = new JMenuItem("Mark as Completed");
        markCompletedItem.addActionListener(e -> {
            Task task = getTaskFromRow(taskTable.getSelectedRow());
            if (task != null) {
                try {
                    TaskDAO.updateTaskCompletion(task.getId(), true);
                    task.setCompleted(true);
                    filterTasks();
                    NotificationManager.showSuccessNotification("Task completed!");
                } catch (SQLException ex) {
                    NotificationManager.showError(ex.getMessage());
                }
            }
        });

        JMenuItem extendDeadlineItem = new JMenuItem("Extend Deadline");
        extendDeadlineItem.addActionListener(e -> {
            Task task = getTaskFromRow(taskTable.getSelectedRow());
            if (task != null) extendTaskDeadline(task);
        });

        popupMenu.add(markCompletedItem);
        popupMenu.add(extendDeadlineItem);
        taskTable.setComponentPopupMenu(popupMenu);
    }

    // ==================== REST OF YOUR METHODS ====================
    private String getTaskStatus(Task task) {
        if (task.isCompleted()) return "Completed";
        long timeLeft = task.getDeadline().getTime() - System.currentTimeMillis();
        if (timeLeft < 0) return "Expired";
        long hours = timeLeft / (1000 * 60 * 60);
        if (hours <= 24) return "Due Soon";
        return "Active";
    }

    public void updateTaskInTable(Task task) {
        SwingUtilities.invokeLater(() -> {
            for (Task t : tasks) {
                if (t.getId() == task.getId()) {
                    t.setCompleted(true);
                    break;
                }
            }
            filterTasks();
        });
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 255, 255), 2, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel addTaskLabel = new JLabel("Add New Task");
        addTaskLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(addTaskLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        fieldsPanel.add(new JLabel("Task:"), gbc);

        gbc.gridy = 1;
        taskField = new JTextField();
        taskField.setBackground(Color.ORANGE);
        taskField.setPreferredSize(new Dimension(200, 35));
        fieldsPanel.add(taskField, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.2;
        fieldsPanel.add(new JLabel("Category:"), gbc);

        gbc.gridy = 1;
        categoryCombo = new JComboBox<>(new String[]{"Work", "Personal", "Other"});
        categoryCombo.setBackground(Color.ORANGE);
        fieldsPanel.add(categoryCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2;
        fieldsPanel.add(new JLabel("Deadline:"), gbc);

        gbc.gridy = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm"));
        fieldsPanel.add(dateSpinner, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        JButton addButton = new JButton("Add Task");
        addButton.setBackground(Color.ORANGE);
        addButton.addActionListener(e -> addTask());
        fieldsPanel.add(addButton, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addTask() {
        String text = taskField.getText().trim();
        if (text.isEmpty()) return;

        Task task = new Task();
        task.setText(text);
        task.setCategory((String) categoryCombo.getSelectedItem());
        task.setDeadline((Date) dateSpinner.getValue());

        try {
            int id = TaskDAO.saveTask(task);
            task.setId(id);
            tasks.add(task);
            taskField.setText("");
            filterTasks();
            NotificationManager.showSuccessNotification("Task added!");
        } catch (SQLException e) {
            NotificationManager.showError(e.getMessage());
        }
    }

    private void extendTaskDeadline(Task task) {
        JSpinner newDateSpinner = new JSpinner(new SpinnerDateModel());
        newDateSpinner.setValue(new Date());
        int result = JOptionPane.showConfirmDialog(this, newDateSpinner, "Extend Deadline", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            task.setDeadline((Date) newDateSpinner.getValue());
            try {
                TaskDAO.updateTask(task);
                filterTasks();
            } catch (SQLException e) {
                NotificationManager.showError(e.getMessage());
            }
        }
    }

    private void startNotificationChecker() {
        notificationTimer = new javax.swing.Timer(30000, e -> checkNotifications());
        notificationTimer.start();
    }

    private void checkInitialNotifications() { checkNotifications(); }

    private void checkNotifications() {
        try {
            // Get tasks that are due in the next 30 seconds AND not snoozed
            List<Task> imminent = TaskDAO.getImminentTasks(30);
            List<Task> expired = TaskDAO.getExpiredTasks();

            // Show expired tasks notification
            if (!expired.isEmpty()) {
                NotificationManager.showExpiredTasksNotification(expired);
            }

            // Check imminent tasks (due in next 30 seconds)
            for (Task task : imminent) {
                if (!task.isNotified()) {
                    // Show individual notification for each task
                    boolean markedAsDone = NotificationManager.showDeadlineNotification(task);

                    if (markedAsDone) {
                        // If user marked as done in notification, task is already updated
                        task.setCompleted(true);
                    } else {
                        // Otherwise, just mark as notified so it doesn't show again
                        task.setNotified(true);
                    }

                    // Update task in database
                    TaskDAO.updateTask(task);
                }
            }

            updateTableStatuses();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void updateTableStatuses() {
        SwingUtilities.invokeLater(() -> {
            filterTasks();  // Just re-run the filter to refresh everything
        });
    }

    class ExpiredTaskRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value != null && value.toString().contains("EXPIRED")) {
                c.setForeground(Color.RED);
                setBackground(new Color(255, 230, 230));
            }
            return c;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(Main::new);
    }
}