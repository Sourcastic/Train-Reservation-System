package com.example.trainreservationsystem.repositories.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.trainreservationsystem.models.admin.Route;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class TrainRepository {

  public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
    List<Schedule> schedules = new ArrayList<>();
    // Get day of week from the selected date (MONDAY, TUESDAY, etc.)
    java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
    String dayOfWeekName = dayOfWeek.name(); // Convert to Schedule.DayOfWeek format

    // Search for schedules that run on this day of week
    String query = "SELECT s.*, r.source, r.destination " +
        "FROM schedules s " +
        "JOIN routes r ON s.route_id = r.id " +
        "WHERE LOWER(r.source) = LOWER(?) AND LOWER(r.destination) = LOWER(?) " +
        "AND (s.days_of_week IS NULL OR s.days_of_week LIKE ? OR s.days_of_week LIKE ? OR s.days_of_week LIKE ?)";

    try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setString(1, source);
      stmt.setString(2, destination);
      // Match day of week at start, middle, or end of comma-separated string
      stmt.setString(3, dayOfWeekName + ",%"); // Day at start
      stmt.setString(4, "%," + dayOfWeekName + ",%"); // Day in middle
      stmt.setString(5, "%," + dayOfWeekName); // Day at end

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
    String query = "SELECT s.*, r.source, r.destination " +
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
        rs.getString("source"),
        rs.getString("destination"));

    Schedule schedule = new Schedule(
        rs.getInt("id"),
        route,
        rs.getTime("departure_time").toLocalTime(),
        rs.getTime("arrival_time").toLocalTime(),
        rs.getDouble("price"),
        rs.getInt("capacity"));

    // Deserialize daysOfWeek from comma-separated string
    String daysOfWeekStr = rs.getString("days_of_week");
    if (daysOfWeekStr != null && !daysOfWeekStr.isEmpty()) {
      List<Schedule.DayOfWeek> daysOfWeek = java.util.Arrays.stream(daysOfWeekStr.split(","))
          .map(Schedule.DayOfWeek::valueOf)
          .collect(Collectors.toList());
      schedule.setDaysOfWeek(daysOfWeek);
    }

    return schedule;
  }
}
