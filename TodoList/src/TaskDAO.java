import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDAO {
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            // Create table with all necessary columns from the start
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    task_text VARCHAR(500) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    deadline DATETIME NOT NULL,
                    completed BOOLEAN DEFAULT FALSE,
                    notified BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    snoozed_until DATETIME NULL
                )
                """);

            // Safety check for existing databases missing the snooze column
            try { stmt.execute("ALTER TABLE tasks ADD COLUMN snoozed_until DATETIME NULL"); }
            catch (SQLException e) { /* Column already exists */ }
        }
    }

    public static int saveTask(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (task_text, category, deadline, completed, notified, snoozed_until) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getText());
            pstmt.setString(2, task.getCategory());
            pstmt.setTimestamp(3, new Timestamp(task.getDeadline().getTime()));
            pstmt.setBoolean(4, task.isCompleted());
            pstmt.setBoolean(5, task.isNotified());
            pstmt.setNull(6, Types.TIMESTAMP);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public static List<Task> getImminentTasks(int secondsAhead) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        // Logic: Due soon AND (Never snoozed OR snooze time has passed)
        String sql = "SELECT * FROM tasks WHERE completed = FALSE AND notified = FALSE " +
                "AND (snoozed_until IS NULL OR snoozed_until <= NOW()) " +
                "AND deadline BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? SECOND)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, secondsAhead);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) tasks.add(mapResultSetToTask(rs));
        }
        return tasks;
    }

    public static List<Task> getExpiredTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE completed = FALSE AND deadline < NOW() " +
                "AND (snoozed_until IS NULL OR snoozed_until <= NOW())";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) tasks.add(mapResultSetToTask(rs));
        }
        return tasks;
    }

    public static void updateTaskSnooze(int taskId, Date snoozedUntil) throws SQLException {
        String sql = "UPDATE tasks SET snoozed_until = ?, notified = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (snoozedUntil != null) {
                pstmt.setTimestamp(1, new Timestamp(snoozedUntil.getTime()));
                pstmt.setBoolean(2, true); // Mark as notified so the thread ignores it during snooze
            } else {
                pstmt.setNull(1, Types.TIMESTAMP);
                pstmt.setBoolean(2, false); // Ready to be notified again
            }
            pstmt.setInt(3, taskId);
            pstmt.executeUpdate();
        }
    }

    public static void updateTaskCompletion(int taskId, boolean completed) throws SQLException {
        String sql = "UPDATE tasks SET completed = ?, snoozed_until = NULL WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        }
    }

    public static List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks ORDER BY deadline ASC");
            while (rs.next()) tasks.add(mapResultSetToTask(rs));
        }
        return tasks;
    }

    public static void updateTask(Task task) throws SQLException {
        String sql = "UPDATE tasks SET task_text=?, category=?, deadline=?, completed=?, notified=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getText());
            pstmt.setString(2, task.getCategory());
            pstmt.setTimestamp(3, new Timestamp(task.getDeadline().getTime()));
            pstmt.setBoolean(4, task.isCompleted());
            pstmt.setBoolean(5, task.isNotified());
            pstmt.setInt(6, task.getId());
            pstmt.executeUpdate();
        }
    }

    public static void deleteTask(int taskId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tasks WHERE id=?")) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        }
    }

    private static Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task t = new Task(rs.getString("task_text"), rs.getString("category"), new Date(rs.getTimestamp("deadline").getTime()));
        t.setId(rs.getInt("id"));
        t.setCompleted(rs.getBoolean("completed"));
        t.setNotified(rs.getBoolean("notified"));
        Timestamp snooze = rs.getTimestamp("snoozed_until");
        if (snooze != null) t.setSnoozedUntil(new Date(snooze.getTime()));
        return t;
    }
}