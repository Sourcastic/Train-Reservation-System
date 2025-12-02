package com.example.trainreservationsystem.repositories;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.utils.database.Database;

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

  private User getUserById(int id) throws Exception {
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
}
