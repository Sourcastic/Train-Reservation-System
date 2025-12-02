//not mock

package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.Schedule;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainRepository {

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
                s.setStatus(rs.getString("status"));
                schedules.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public Schedule getScheduleById(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT s.id, s.departure_date, s.departure_time, s.arrival_time, s.capacity, s.price, s.status, r.id AS route_id, r.name AS route_name, r.source, r.destination " +
                             "FROM schedules s JOIN routes r ON s.route_id = r.id WHERE s.id = ?"
             )) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
                s.setStatus(rs.getString("status"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateScheduleStatus(int scheduleId, String status) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE schedules SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, scheduleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.id, s.departure_date, s.departure_time, s.arrival_time, s.capacity, s.price, s.status, " +
                "r.id AS route_id, r.name AS route_name, r.source, r.destination " +
                "FROM schedules s JOIN routes r ON s.route_id = r.id " +
                "WHERE r.source = ? AND r.destination = ? AND s.departure_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, destination);
            ps.setDate(3, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
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
                s.setStatus(rs.getString("status"));
                schedules.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

}
