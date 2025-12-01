package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Notification;

import java.sql.*;

public class NotificationsRepository {

    private Connection connection;

    public NotificationsRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(Notification notification) {
        try {
            String sql = "INSERT INTO notifications (user_id, message, sent) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setBoolean(3, notification.isSent());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markAsSent(int id) {
        try {
            String sql = "UPDATE notifications SET sent = TRUE WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
