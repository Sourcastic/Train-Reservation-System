package com.example.trainreservationsystem.models;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean sent;
    private LocalDateTime createdAt;

    public Notification(int userId, String message) {
        this.userId = userId;
        this.message = message;
        this.sent = false;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isSent() { return sent; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setSent(boolean sent) { this.sent = sent; }
}
