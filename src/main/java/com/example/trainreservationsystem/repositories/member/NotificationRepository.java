package com.example.trainreservationsystem.repositories.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.member.Notification;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class NotificationRepository {

    public List<Notification> getNotificationsByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getBoolean("sent")));
            }
        } catch (Exception e) {
            System.err.println("Error getting notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    public void saveNotification(Notification notification) {
        String query = "INSERT INTO notifications (user_id, message, sent, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setBoolean(3, notification.isSent());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                notification.setId(rs.getInt(1));
            }
        } catch (Exception e) {
            System.err.println("Error saving notification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    public boolean markAsRead(int notificationId) {
        String query = "UPDATE notifications SET sent = true WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNotification(int notificationId) {
        String query = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
