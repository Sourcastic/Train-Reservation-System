package com.example.trainreservationsystem.repositories;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.trainreservationsystem.models.shared.User;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class UserRepository {

  public User authenticateUser(String email, String password) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "SELECT * FROM sp_authenticate_user(?, ?)";
      try (CallableStatement stmt = conn.prepareCall(sql)) {
        stmt.setString(1, email);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          int userId = rs.getInt("id");
          return getUserById(userId);
        }
        return null;
      }
    }
  }

  public void registerUser(String username, String password, String email, String phoneNo) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "CALL sp_register_user(?, ?, ?, ?, ?)";
      try (CallableStatement stmt = conn.prepareCall(sql)) {
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.setString(3, email);
        stmt.setString(4, phoneNo);
        stmt.setString(5, "CUSTOMER");
        stmt.execute();
      }
    }
  }

  public User getUserById(int id) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "SELECT * FROM users WHERE id = ?";
      try (var stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          return new User(
              rs.getInt("id"),
              rs.getString("username"),
              rs.getString("password"),
              rs.getString("email"),
              rs.getString("phone_no"),
              rs.getString("user_type"),
              rs.getInt("loyalty_points"));
        }
        return null;
      }
    }
  }

  public User getUserByUsername(String username) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "SELECT * FROM users WHERE username = ?";
      try (var stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          return new User(
              rs.getInt("id"),
              rs.getString("username"),
              rs.getString("password"),
              rs.getString("email"),
              rs.getString("phone_no"),
              rs.getString("user_type"),
              rs.getInt("loyalty_points"));
        }
        return null;
      }
    }
  }

  public void updatePassword(String email, String newPassword) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "CALL sp_update_password(?, ?)";
      try (CallableStatement stmt = conn.prepareCall(sql)) {
        stmt.setString(1, email);
        stmt.setString(2, newPassword);
        stmt.execute();
      }
    }
  }

  public void updateUser(User user) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "CALL sp_update_user(?, ?, ?, ?)";
      try (CallableStatement stmt = conn.prepareCall(sql)) {
        stmt.setInt(1, user.getId());
        stmt.setString(2, user.getUsername());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPhoneNo());
        stmt.execute();
      }
    }
  }

  /**
   * Updates loyalty points for a user.
   */
  public void updateLoyaltyPoints(int userId, int loyaltyPoints) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "UPDATE users SET loyalty_points = ? WHERE id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, loyaltyPoints);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
      }
    }
  }

  /**
   * Adds loyalty points to a user's current balance.
   */
  public void addLoyaltyPoints(int userId, int pointsToAdd) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "UPDATE users SET loyalty_points = loyalty_points + ? WHERE id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, pointsToAdd);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
      }
    }
  }

  /**
   * Deducts loyalty points from a user's current balance.
   */
  public void deductLoyaltyPoints(int userId, int pointsToDeduct) throws Exception {
    try (Connection conn = Database.getConnection()) {
      String sql = "UPDATE users SET loyalty_points = loyalty_points - ? WHERE id = ? AND loyalty_points >= ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, pointsToDeduct);
        stmt.setInt(2, userId);
        stmt.setInt(3, pointsToDeduct);
        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected == 0) {
          throw new Exception("Insufficient loyalty points");
        }
      }
    }
  }
}
