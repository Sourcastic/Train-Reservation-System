package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Notification;
import com.example.trainreservationsystem.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class NotificationRepository {

    public void saveNotification(Notification notification) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO notifications (user_id, message, sent) VALUES (?, ?, ?)")) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setBoolean(3, notification.isSent());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
