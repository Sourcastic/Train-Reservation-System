package com.example.trainreservationsystem.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.example.trainreservationsystem.models.Complaint;
import com.example.trainreservationsystem.utils.database.Database;

public class ComplaintRepository {
  public void saveComplaint(Complaint complaint) {
    if (Database.isMockMode()) {
      System.out.println(
          "Mock: Saved complaint: " + complaint.getSubject() + " (Tracking: " + complaint.getTrackingId() + ")");
      return;
    }

    String query = "INSERT INTO complaints (user_id, subject, description, tracking_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, complaint.getUserId());
      stmt.setString(2, complaint.getSubject());
      stmt.setString(3, complaint.getDescription());
      stmt.setString(4, complaint.getTrackingId());
      stmt.executeUpdate();
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        saveComplaint(complaint); // Retry with mock
        return;
      }
      System.err.println("Error saving complaint: " + e.getMessage());
      e.printStackTrace();
      saveComplaint(complaint); // Fallback to mock
    }
  }
}
