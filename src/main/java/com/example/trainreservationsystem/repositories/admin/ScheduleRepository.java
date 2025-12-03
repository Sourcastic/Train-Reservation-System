package com.example.trainreservationsystem.repositories.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.admin.Route;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.shared.Seat;
import com.example.trainreservationsystem.repositories.shared.SeatRepository;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class ScheduleRepository {

    private final SeatRepository seatRepository = new SeatRepository();

    public Schedule addSchedule(Schedule schedule) throws Exception {
        String sql = "INSERT INTO schedules (route_id, departure_date, departure_time, arrival_time, price, capacity, days_of_week) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, schedule.getRoute().getId());
            stmt.setDate(2, java.sql.Date.valueOf(schedule.getDepartureDate()));
            stmt.setObject(3, schedule.getDepartureTime());
            stmt.setObject(4, schedule.getArrivalTime());
            stmt.setDouble(5, schedule.getPrice());
            stmt.setInt(6, schedule.getCapacity());

            // Serialize daysOfWeek as comma-separated string
            String daysOfWeekStr = schedule.getDaysOfWeek().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(","));
            stmt.setString(7, daysOfWeekStr.isEmpty() ? null : daysOfWeekStr);

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
        // Use JOIN to fetch routes in a single query (fixes N+1)
        String sql = "SELECT s.*, r.id as route_id, r.name as route_name, r.source, r.destination " +
                "FROM schedules s " +
                "LEFT JOIN routes r ON s.route_id = r.id " +
                "ORDER BY s.id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            // Collect schedule IDs for batch loading seats
            List<Integer> scheduleIds = new ArrayList<>();
            java.util.Map<Integer, Schedule> scheduleMap = new java.util.HashMap<>();

            while (rs.next()) {
                int scheduleId = rs.getInt("id");
                scheduleIds.add(scheduleId);

                // Build route from JOIN result
                Route route = null;
                if (rs.getInt("route_id") > 0) {
                    route = new Route(
                            rs.getInt("route_id"),
                            rs.getString("route_name"),
                            rs.getString("source"),
                            rs.getString("destination"));
                }

                Schedule schedule = new Schedule(
                        scheduleId,
                        route,
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getTime("departure_time").toLocalTime(),
                        rs.getTime("arrival_time").toLocalTime(),
                        rs.getDouble("price"),
                        rs.getInt("capacity"));

                // Deserialize daysOfWeek from comma-separated string
                String daysOfWeekStr = rs.getString("days_of_week");
                if (daysOfWeekStr != null && !daysOfWeekStr.isEmpty()) {
                    List<Schedule.DayOfWeek> daysOfWeek = java.util.Arrays.stream(daysOfWeekStr.split(","))
                            .map(Schedule.DayOfWeek::valueOf)
                            .collect(java.util.stream.Collectors.toList());
                    schedule.setDaysOfWeek(daysOfWeek);
                }

                scheduleMap.put(scheduleId, schedule);
                schedules.add(schedule);
            }

            // Batch load all seats for all schedules (fixes N+1)
            if (!scheduleIds.isEmpty()) {
                java.util.Map<Integer, List<Seat>> seatsBySchedule = seatRepository
                        .getSeatsByScheduleIds(scheduleIds);
                for (Schedule schedule : schedules) {
                    List<Seat> seats = seatsBySchedule
                            .getOrDefault(schedule.getId(), new ArrayList<>());
                    schedule.setSeats(seats);
                }
            }
        }
        return schedules;
    }

    public Schedule getScheduleById(int id) throws Exception {
        // Use JOIN to fetch route in a single query (fixes N+1)
        String sql = "SELECT s.*, r.id as route_id, r.name as route_name, r.source, r.destination " +
                "FROM schedules s " +
                "LEFT JOIN routes r ON s.route_id = r.id " +
                "WHERE s.id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Build route from JOIN result
                    Route route = null;
                    if (rs.getInt("route_id") > 0) {
                        route = new Route(
                                rs.getInt("route_id"),
                                rs.getString("route_name"),
                                rs.getString("source"),
                                rs.getString("destination"));
                    }

                    Schedule schedule = new Schedule(
                            rs.getInt("id"),
                            route,
                            rs.getDate("departure_date").toLocalDate(),
                            rs.getTime("departure_time").toLocalTime(),
                            rs.getTime("arrival_time").toLocalTime(),
                            rs.getDouble("price"),
                            rs.getInt("capacity"));

                    // Deserialize daysOfWeek from comma-separated string
                    String daysOfWeekStr = rs.getString("days_of_week");
                    if (daysOfWeekStr != null && !daysOfWeekStr.isEmpty()) {
                        List<Schedule.DayOfWeek> daysOfWeek = java.util.Arrays.stream(daysOfWeekStr.split(","))
                                .map(Schedule.DayOfWeek::valueOf)
                                .collect(java.util.stream.Collectors.toList());
                        schedule.setDaysOfWeek(daysOfWeek);
                    }

                    schedule.setSeats(seatRepository.getSeatsByScheduleId(id));
                    return schedule;
                }
            }
        }
        return null;
    }

    public void updateSchedule(Schedule schedule) throws Exception {
        String sql = "UPDATE schedules SET route_id = ?, departure_date = ?, departure_time = ?, arrival_time = ?, price = ?, capacity = ?, days_of_week = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getRoute().getId());
            stmt.setDate(2, java.sql.Date.valueOf(schedule.getDepartureDate()));
            stmt.setObject(3, schedule.getDepartureTime());
            stmt.setObject(4, schedule.getArrivalTime());
            stmt.setDouble(5, schedule.getPrice());
            stmt.setInt(6, schedule.getCapacity());

            // Serialize daysOfWeek as comma-separated string
            String daysOfWeekStr = schedule.getDaysOfWeek().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(","));
            stmt.setString(7, daysOfWeekStr.isEmpty() ? null : daysOfWeekStr);

            stmt.setInt(8, schedule.getId());
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
