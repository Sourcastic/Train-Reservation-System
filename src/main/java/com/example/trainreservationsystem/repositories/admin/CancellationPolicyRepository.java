package com.example.trainreservationsystem.repositories.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.admin.CancellationPolicy;
import com.example.trainreservationsystem.utils.shared.database.Database;

/**
 * Repository for cancellation policy operations.
 */
public class CancellationPolicyRepository {

  /**
   * Gets the active cancellation policy.
   * For simplicity, we'll use the first active policy or create a default one.
   */
  public CancellationPolicy getActivePolicy() {
    String query = "SELECT * FROM cancellation_policies WHERE is_active = true ORDER BY id DESC LIMIT 1";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return mapResultSetToPolicy(rs);
      }
    } catch (Exception e) {
      System.err.println("Error getting active cancellation policy: " + e.getMessage());
      e.printStackTrace();
    }
    // Return default policy if none exists
    return getDefaultPolicy();
  }

  /**
   * Gets all cancellation policies.
   */
  public List<CancellationPolicy> getAllPolicies() {
    List<CancellationPolicy> policies = new ArrayList<>();
    String query = "SELECT * FROM cancellation_policies ORDER BY id DESC";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        policies.add(mapResultSetToPolicy(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting cancellation policies: " + e.getMessage());
      e.printStackTrace();
    }
    return policies;
  }

  /**
   * Saves a cancellation policy.
   */
  public void savePolicy(CancellationPolicy policy) {
    String query = "INSERT INTO cancellation_policies (name, description, hours_before_departure, refund_percentage, allow_cancellation, min_hours_before_departure, is_active) "
        +
        "VALUES (?, ?, ?, ?, ?, ?, true) RETURNING id";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, policy.getName());
      stmt.setString(2, policy.getDescription());
      stmt.setInt(3, policy.getHoursBeforeDeparture());
      stmt.setDouble(4, policy.getRefundPercentage());
      stmt.setBoolean(5, policy.isAllowCancellation());
      stmt.setInt(6, policy.getMinHoursBeforeDeparture());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        policy.setId(rs.getInt(1));
      }
    } catch (Exception e) {
      System.err.println("Error saving cancellation policy: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to save cancellation policy", e);
    }
  }

  /**
   * Updates a cancellation policy.
   */
  public void updatePolicy(CancellationPolicy policy) {
    String query = "UPDATE cancellation_policies SET name = ?, description = ?, hours_before_departure = ?, refund_percentage = ?, allow_cancellation = ?, min_hours_before_departure = ? WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, policy.getName());
      stmt.setString(2, policy.getDescription());
      stmt.setInt(3, policy.getHoursBeforeDeparture());
      stmt.setDouble(4, policy.getRefundPercentage());
      stmt.setBoolean(5, policy.isAllowCancellation());
      stmt.setInt(6, policy.getMinHoursBeforeDeparture());
      stmt.setInt(7, policy.getId());
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error updating cancellation policy: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to update cancellation policy", e);
    }
  }

  /**
   * Deactivates all policies and activates the given one.
   */
  public void setActivePolicy(int policyId) {
    String deactivateQuery = "UPDATE cancellation_policies SET is_active = false";
    String activateQuery = "UPDATE cancellation_policies SET is_active = true WHERE id = ?";
    try (Connection conn = Database.getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement deactivateStmt = conn.prepareStatement(deactivateQuery);
          PreparedStatement activateStmt = conn.prepareStatement(activateQuery)) {
        deactivateStmt.executeUpdate();
        activateStmt.setInt(1, policyId);
        activateStmt.executeUpdate();
        conn.commit();
      } catch (Exception e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (Exception e) {
      System.err.println("Error setting active policy: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to set active policy", e);
    }
  }

  /**
   * Deletes a cancellation policy.
   */
  public void deletePolicy(int policyId) {
    String query = "DELETE FROM cancellation_policies WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, policyId);
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error deleting cancellation policy: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to delete cancellation policy", e);
    }
  }

  private CancellationPolicy mapResultSetToPolicy(ResultSet rs) throws Exception {
    return new CancellationPolicy(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getInt("hours_before_departure"),
        rs.getDouble("refund_percentage"),
        rs.getBoolean("allow_cancellation"),
        rs.getInt("min_hours_before_departure"));
  }

  /**
   * Returns a default cancellation policy.
   */
  private CancellationPolicy getDefaultPolicy() {
    CancellationPolicy policy = new CancellationPolicy();
    policy.setId(0);
    policy.setName("Default Policy");
    policy.setDescription("Default cancellation policy");
    policy.setHoursBeforeDeparture(24);
    policy.setRefundPercentage(80.0);
    policy.setAllowCancellation(true);
    policy.setMinHoursBeforeDeparture(2);
    return policy;
  }
}
