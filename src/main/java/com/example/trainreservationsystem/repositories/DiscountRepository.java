package com.example.trainreservationsystem.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.admin.Discount;
import com.example.trainreservationsystem.utils.shared.database.Database;

/**
 * Repository for discount code operations.
 */
public class DiscountRepository {

  /**
   * Finds a discount by code.
   */
  public Discount findByCode(String code) {
    String query = "SELECT * FROM discounts WHERE code = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, code);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return mapResultSetToDiscount(rs);
      }
    } catch (Exception e) {
      System.err.println("Error finding discount by code: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Finds a discount by code and schedule ID.
   * Returns discount if it applies to the schedule (schedule_id matches or is
   * null).
   */
  public Discount findByCodeAndSchedule(String code, Integer scheduleId) {
    String query = "SELECT * FROM discounts WHERE code = ? AND (schedule_id IS NULL OR schedule_id = ?)";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, code);
      if (scheduleId != null) {
        stmt.setInt(2, scheduleId);
      } else {
        stmt.setNull(2, Types.INTEGER);
      }
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return mapResultSetToDiscount(rs);
      }
    } catch (Exception e) {
      System.err.println("Error finding discount by code and schedule: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets all discounts for a specific schedule.
   */
  public List<Discount> getDiscountsBySchedule(Integer scheduleId) {
    List<Discount> discounts = new ArrayList<>();
    String query = "SELECT * FROM discounts WHERE (schedule_id IS NULL OR schedule_id = ?) AND is_active = true";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      if (scheduleId != null) {
        stmt.setInt(1, scheduleId);
      } else {
        stmt.setNull(1, Types.INTEGER);
      }
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        discounts.add(mapResultSetToDiscount(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting discounts by schedule: " + e.getMessage());
      e.printStackTrace();
    }
    return discounts;
  }

  /**
   * Gets discounts by type.
   */
  public List<Discount> getDiscountsByType(String type) {
    List<Discount> discounts = new ArrayList<>();
    String query = "SELECT * FROM discounts WHERE type = ? AND is_active = true";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, type);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        discounts.add(mapResultSetToDiscount(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting discounts by type: " + e.getMessage());
      e.printStackTrace();
    }
    return discounts;
  }

  /**
   * Gets all active discounts.
   */
  public List<Discount> getAllActiveDiscounts() {
    List<Discount> discounts = new ArrayList<>();
    String query = "SELECT * FROM discounts WHERE is_active = true";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        discounts.add(mapResultSetToDiscount(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting active discounts: " + e.getMessage());
      e.printStackTrace();
    }
    return discounts;
  }

  /**
   * Gets all discounts (for admin view).
   */
  public List<Discount> getAllDiscounts() {
    List<Discount> discounts = new ArrayList<>();
    String query = "SELECT * FROM discounts ORDER BY id DESC";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        discounts.add(mapResultSetToDiscount(rs));
      }
    } catch (Exception e) {
      System.err.println("Error getting all discounts: " + e.getMessage());
      e.printStackTrace();
    }
    return discounts;
  }

  /**
   * Updates an existing discount.
   */
  public void updateDiscount(Discount discount) {
    String query = "UPDATE discounts SET schedule_id = ?, name = ?, code = ?, type = ?, description = ?, "
        +
        "discount_percentage = ?, discount_amount = ?, valid_from = ?, valid_to = ?, is_active = ?, max_uses = ? WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      if (discount.getScheduleId() != null) {
        stmt.setInt(1, discount.getScheduleId());
      } else {
        stmt.setNull(1, Types.INTEGER);
      }
      stmt.setString(2, discount.getName());
      stmt.setString(3, discount.getCode());
      stmt.setString(4, discount.getType() != null ? discount.getType().name() : "DISCOUNT_CODE");
      stmt.setString(5, discount.getDescription());
      stmt.setDouble(6, discount.getDiscountPercentage());
      stmt.setDouble(7, discount.getDiscountAmount());
      stmt.setObject(8, discount.getValidFrom());
      stmt.setObject(9, discount.getValidTo());
      stmt.setBoolean(10, discount.isActive());
      stmt.setInt(11, discount.getMaxUses());
      stmt.setInt(12, discount.getId());
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error updating discount: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to update discount", e);
    }
  }

  /**
   * Deletes a discount.
   */
  public void deleteDiscount(int discountId) {
    String query = "DELETE FROM discounts WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, discountId);
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error deleting discount: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to delete discount", e);
    }
  }

  /**
   * Increments the usage count of a discount.
   */
  public void incrementUsage(int discountId) {
    String query = "UPDATE discounts SET current_uses = current_uses + 1 WHERE id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, discountId);
      stmt.executeUpdate();
    } catch (Exception e) {
      System.err.println("Error incrementing discount usage: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Saves a new discount code.
   */
  public void saveDiscount(Discount discount) {
    String query = "INSERT INTO discounts (schedule_id, name, code, type, description, discount_percentage, discount_amount, "
        +
        "valid_from, valid_to, is_active, max_uses, current_uses) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      if (discount.getScheduleId() != null) {
        stmt.setInt(1, discount.getScheduleId());
      } else {
        stmt.setNull(1, Types.INTEGER);
      }
      stmt.setString(2, discount.getName());
      stmt.setString(3, discount.getCode());
      stmt.setString(4, discount.getType() != null ? discount.getType().name() : "DISCOUNT_CODE");
      stmt.setString(5, discount.getDescription());
      stmt.setDouble(6, discount.getDiscountPercentage());
      stmt.setDouble(7, discount.getDiscountAmount());
      stmt.setObject(8, discount.getValidFrom());
      stmt.setObject(9, discount.getValidTo());
      stmt.setBoolean(10, discount.isActive());
      stmt.setInt(11, discount.getMaxUses());
      stmt.setInt(12, discount.getCurrentUses());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        discount.setId(rs.getInt(1));
      }
    } catch (Exception e) {
      System.err.println("Error saving discount: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to save discount", e);
    }
  }

  private Discount mapResultSetToDiscount(ResultSet rs) throws Exception {
    Discount discount = new Discount();
    discount.setId(rs.getInt("id"));

    int scheduleId = rs.getInt("schedule_id");
    if (!rs.wasNull()) {
      discount.setScheduleId(scheduleId);
    }

    discount.setName(rs.getString("name"));
    discount.setCode(rs.getString("code"));

    String typeStr = rs.getString("type");
    if (typeStr != null) {
      try {
        discount.setType(Discount.DiscountType.valueOf(typeStr));
      } catch (IllegalArgumentException e) {
        discount.setType(Discount.DiscountType.DISCOUNT_CODE); // Default fallback
      }
    } else {
      discount.setType(Discount.DiscountType.DISCOUNT_CODE);
    }

    discount.setDescription(rs.getString("description"));
    discount.setDiscountPercentage(rs.getDouble("discount_percentage"));
    discount.setDiscountAmount(rs.getDouble("discount_amount"));

    java.sql.Date validFromDate = rs.getDate("valid_from");
    if (validFromDate != null) {
      discount.setValidFrom(validFromDate.toLocalDate());
    }

    java.sql.Date validToDate = rs.getDate("valid_to");
    if (validToDate != null) {
      discount.setValidTo(validToDate.toLocalDate());
    }

    discount.setActive(rs.getBoolean("is_active"));
    discount.setMaxUses(rs.getInt("max_uses"));
    discount.setCurrentUses(rs.getInt("current_uses"));

    return discount;
  }
}
