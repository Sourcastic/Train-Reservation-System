package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT id, username, password, email, user_type FROM users");

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public User getUserById(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, username, password, email FROM users WHERE id = ?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(User user) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET username=?, email=?, password=? WHERE id=?")) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deactivateUser(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET user_type='INACTIVE' WHERE id=?")) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM users WHERE id=?")) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
