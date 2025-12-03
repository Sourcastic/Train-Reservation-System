package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.admin.Statistics;
import com.example.trainreservationsystem.utils.shared.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsRepository {

    public void addStatistics(Statistics statistics) throws Exception {
        String sql = "INSERT INTO statistics (schedule_id, day_of_week, departure_time, seat_class_id, seats_sold) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // If we have per-class breakdown
            if (statistics.getSeatsSoldPerClass() != null && !statistics.getSeatsSoldPerClass().isEmpty()) {
                for (Map.Entry<Integer, Long> entry : statistics.getSeatsSoldPerClass().entrySet()) {
                    stmt.setInt(1, statistics.getScheduleId());
                    stmt.setString(2, statistics.getDayOfWeek());
                    stmt.setObject(3, statistics.getDepartureTime());
                    stmt.setInt(4, entry.getKey());
                    stmt.setInt(5, entry.getValue().intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                // Fallback or single entry if needed (though model suggests per-class is
                // primary)
                stmt.setInt(1, statistics.getScheduleId());
                stmt.setString(2, statistics.getDayOfWeek());
                stmt.setObject(3, statistics.getDepartureTime());
                stmt.setInt(4, statistics.getSeatClassId());
                stmt.setInt(5, statistics.getSeatsSold());
                stmt.executeUpdate();
            }
        }
    }

    public List<Statistics> getStatisticsByScheduleId(int scheduleId) throws Exception {
        List<Statistics> statsList = new ArrayList<>();
        String sql = "SELECT * FROM statistics WHERE schedule_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Statistics stats = new Statistics();
                    stats.setId(rs.getInt("id"));
                    stats.setScheduleId(rs.getInt("schedule_id"));
                    stats.setDayOfWeek(rs.getString("day_of_week"));
                    stats.setDepartureTime(rs.getTime("departure_time").toLocalTime());
                    stats.setSeatClassId(rs.getInt("seat_class_id"));
                    stats.setSeatsSold(rs.getInt("seats_sold"));
                    statsList.add(stats);
                }
            }
        }
        return statsList;
    }
}
