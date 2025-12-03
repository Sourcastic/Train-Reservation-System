package com.example.trainreservationsystem.repositories.admin;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.admin.Route;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class TrainRepository {

  public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
    List<Schedule> schedules = new ArrayList<>();
    String query = "SELECT s.*, r.name as route_name, r.source, r.destination " +
        "FROM schedules s " +
        "JOIN routes r ON s.route_id = r.id " +
        "WHERE LOWER(r.source) = LOWER(?) AND LOWER(r.destination) = LOWER(?) AND s.departure_date = ?";

    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setString(1, source);
      stmt.setString(2, destination);
      stmt.setDate(3, Date.valueOf(date));

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        schedules.add(mapResultSetToSchedule(rs));
      }
    } catch (Exception e) {
      System.err.println("Error searching schedules: " + e.getMessage());
      e.printStackTrace();
    }
    return schedules;
  }

  public Schedule getScheduleById(int id) {
    String query = "SELECT s.*, r.name as route_name, r.source, r.destination " +
        "FROM schedules s " +
        "JOIN routes r ON s.route_id = r.id " +
        "WHERE s.id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return mapResultSetToSchedule(rs);
      }
    } catch (Exception e) {
      System.err.println("Error getting schedule: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  private Schedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
    Route route = new Route(
        rs.getInt("route_id"),
        rs.getString("route_name"),
        rs.getString("source"),
        rs.getString("destination"));

    return new Schedule(
        rs.getInt("id"),
        route,
        rs.getDate("departure_date").toLocalDate(),
        rs.getTime("departure_time").toLocalTime(),
        rs.getTime("arrival_time").toLocalTime(),
        rs.getDouble("price"),
        rs.getInt("capacity"));
  }
}
