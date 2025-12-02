//not mock
package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Notification;
import java.sql.*;

public class NotificationRepository {

    public void saveNotification(Notification notification) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO notifications (user_id, message, sent) VALUES (?, ?, ?)"
             )) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setBoolean(3, notification.isSent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
