import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {

    public static boolean setupDatabase() {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "abrshiz";
        String password = "abrsh123";

        try {
            // Connect to MySQL (without database)
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();

            // Create database if not exists
            stmt.execute("CREATE DATABASE IF NOT EXISTS todolist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("✅ Database 'todolist' created/verified");

            // Use the database
            stmt.execute("USE todolist");

            // Create tasks table
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    task_text VARCHAR(500) NOT NULL,
                    description TEXT,
                    category VARCHAR(50) NOT NULL,
                    deadline DATETIME NOT NULL,
                    completed BOOLEAN DEFAULT FALSE,
                    notified BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_deadline (deadline),
                    INDEX idx_completed (completed),
                    INDEX idx_category (category)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;

            stmt.execute(createTableSQL);
            System.out.println("✅ Table 'tasks' created/verified");

            // REMOVED SAMPLE DATA - No automatic insertion
            System.out.println("✅ No sample data inserted (as requested)");

            stmt.close();
            conn.close();

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Database setup failed!\n\nError: " + e.getMessage() +
                            "\n\nPlease ensure:\n1. MySQL is running\n2. Password is correct\n3. You have privileges",
                    "Setup Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        if (setupDatabase()) {
            JOptionPane.showMessageDialog(null,
                    "✅ Database setup completed successfully!\n" +
                            "No sample data inserted.\n" +
                            "You can now run the ToDo List application.",
                    "Setup Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
