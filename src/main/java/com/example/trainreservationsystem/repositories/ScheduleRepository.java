package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.utils.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    private final RouteRepository routeRepository = new RouteRepository();
    private final SeatRepository seatRepository = new SeatRepository();

    public Schedule addSchedule(Schedule schedule) throws Exception {
        String sql = "INSERT INTO schedules (route_id, departure_date, departure_time, arrival_time, price, capacity) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, schedule.getRoute().getId());
            stmt.setDate(2, java.sql.Date.valueOf(schedule.getDepartureDate()));
            stmt.setObject(3, schedule.getDepartureTime());
            stmt.setObject(4, schedule.getArrivalTime());
            stmt.setDouble(5, schedule.getPrice());
            stmt.setInt(6, schedule.getCapacity());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Creating schedule failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setId(generatedKeys.getInt(1));
                } else {
                    throw new Exception("Creating schedule failed, no ID obtained.");
                }
            }
        }
        return schedule;
    }

    public List<Schedule> getAllSchedules() throws Exception {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Route route = routeRepository.getRouteById(rs.getInt("route_id"));
                Schedule schedule = new Schedule(
                        rs.getInt("id"),
                        route,
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getTime("departure_time").toLocalTime(),
                        rs.getTime("arrival_time").toLocalTime(),
                        rs.getDouble("price"),
                        rs.getInt("capacity"));
                schedule.setSeats(seatRepository.getSeatsByScheduleId(schedule.getId()));
                schedules.add(schedule);
            }
        }
        return schedules;
    }

    public Schedule getScheduleById(int id) throws Exception {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Route route = routeRepository.getRouteById(rs.getInt("route_id"));
                    Schedule schedule = new Schedule(
                            rs.getInt("id"),
                            route,
                            rs.getDate("departure_date").toLocalDate(),
                            rs.getTime("departure_time").toLocalTime(),
                            rs.getTime("arrival_time").toLocalTime(),
                            rs.getDouble("price"),
                            rs.getInt("capacity"));
                    schedule.setSeats(seatRepository.getSeatsByScheduleId(id));
                    return schedule;
                }
            }
        }
        return null;
    }

    public void updateSchedule(Schedule schedule) throws Exception {
        String sql = "UPDATE schedules SET route_id = ?, departure_date = ?, departure_time = ?, arrival_time = ?, price = ?, capacity = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getRoute().getId());
            stmt.setDate(2, java.sql.Date.valueOf(schedule.getDepartureDate()));
            stmt.setObject(3, schedule.getDepartureTime());
            stmt.setObject(4, schedule.getArrivalTime());
            stmt.setDouble(5, schedule.getPrice());
            stmt.setInt(6, schedule.getCapacity());
            stmt.setInt(7, schedule.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteSchedule(int id) throws Exception {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
