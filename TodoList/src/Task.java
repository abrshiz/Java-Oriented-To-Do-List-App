import java.util.Date;

public class Task {
    private int id;
    private String text;
    private String category;
    private Date deadline;
    private boolean completed;
    private boolean notified;
    private Date createdAt;
    private Date snoozedUntil; // Added field for snooze tracking
    public Task() {}

    public Task(String text, String category, Date deadline) {
        this.text = text;
        this.category = category;
        this.deadline = deadline;
        this.completed = false;
        this.notified = false;
        this.createdAt = new Date();
        this.snoozedUntil = null;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.snoozedUntil = null; // Clear snooze if task is completed
        }
    }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // New getter and setter for snoozedUntil
    public Date getSnoozedUntil() { return snoozedUntil; }
    public void setSnoozedUntil(Date snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    // Helper method to check if task is currently snoozed
    public boolean isSnoozed() {
        if (snoozedUntil == null) return false;
        return new Date().before(snoozedUntil);
    }

    @Override
    public String toString() {
        return text + " (" + category + ") - Due: " + deadline;
    }
}