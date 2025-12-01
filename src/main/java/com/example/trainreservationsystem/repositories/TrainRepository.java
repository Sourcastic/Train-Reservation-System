package com.example.trainreservationsystem.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.Schedule;

public class TrainRepository {

  // Mock Data
  public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
    List<Schedule> schedules = new ArrayList<>();
    // Hardcoded mock return
    Route r = new Route(1, "Express 101", source, destination);
    Schedule s = new Schedule(1, r, date, LocalTime.of(9, 0), LocalTime.of(12, 0), 50.0, 100);
    schedules.add(s);
    return schedules;
  }

  public Schedule getScheduleById(int id) {
    Route r = new Route(1, "Express 101", "Mock Source", "Mock Dest");
    return new Schedule(id, r, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(12, 0), 50.0, 100);
  }

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT s.id, s.departure_date, s.departure_time, s.arrival_time, s.capacity, s.price, s.status, r.id AS route_id, r.name AS route_name, r.source, r.destination " +
                     "FROM schedules s JOIN routes r ON s.route_id = r.id")) {
            while (rs.next()) {
                Route route = new Route(
                        rs.getInt("route_id"),
                        rs.getString("route_name"),
                        rs.getString("source"),
                        rs.getString("destination")
                );
                Schedule s = new Schedule(
                        rs.getInt("id"),
                        route,
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getTime("departure_time").toLocalTime(),
                        rs.getTime("arrival_time").toLocalTime(),
                        rs.getDouble("price"),
                        rs.getInt("capacity")
                );
                s.setStatus(rs.getString("status")); // add a setStatus method in Schedule
                schedules.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public void updateScheduleStatus(int scheduleId, String status) {
        String sql = "UPDATE schedules SET status = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, scheduleId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void notifyPassengers(int scheduleId, String message) {
        String sql = "INSERT INTO notifications (user_id, message) " +
                "SELECT b.user_id, ? FROM bookings b " +
                "WHERE b.schedule_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message);
            ps.setInt(2, scheduleId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
