package com.example.trainreservationsystem.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.utils.database.Database;

public class UserRepository {
  public User getUserByUsername(String username) {
    if (Database.isMockMode()) {
      if ("demo".equals(username)) {
        return new User(1, "demo", "demo123", "demo@example.com");
      }
      return null;
    }

    String query = "SELECT * FROM users WHERE username = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("email"));
      }
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().equals("MOCK_MODE")) {
        return getUserByUsername(username); // Retry with mock
      }
      System.err.println("Error getting user: " + e.getMessage());
      e.printStackTrace();
      return getUserByUsername(username); // Fallback to mock
    }
    return null;
  }
}
