package com.scc.Model;

public class ActivityLog {
    private int id;
    private int userId;
    private String username;
    private String action;
    private String timestamp;

    public ActivityLog(int id, int userId, String username, String action, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] User: %s (ID: %d) - %s", timestamp, username, userId, action);
    }
}