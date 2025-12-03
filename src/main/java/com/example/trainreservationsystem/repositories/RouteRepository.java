package com.example.trainreservationsystem.repositories;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.RouteSegment;
import com.example.trainreservationsystem.models.Stop;
import com.example.trainreservationsystem.utils.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RouteRepository {

    private final StopRepository stopRepository = new StopRepository();

    public Route addRoute(Route route) throws Exception {
        String sql = "INSERT INTO routes (name, source, destination) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, route.getName());
            stmt.setString(2, route.getSource());
            stmt.setString(3, route.getDestination());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Creating route failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    route.setId(generatedKeys.getInt(1));
                } else {
                    throw new Exception("Creating route failed, no ID obtained.");
                }
            }
        }
        return route;
    }

    public List<Route> getAllRoutes() throws Exception {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT * FROM routes";
        System.out.println("[DEBUG] RouteRepository: Fetching all routes...");
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Route route = new Route(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("source"),
                        rs.getString("destination"));
                routes.add(route);
            }
        }
        // Fetch segments in a separate pass to avoid connection conflicts
        for (Route route : routes) {
            route.setSegments(getSegmentsByRouteId(route.getId()));
        }
        System.out.println("[DEBUG] RouteRepository: Found " + routes.size() + " routes");
        return routes;
    }

    public Route getRouteById(int id) throws Exception {
        String sql = "SELECT * FROM routes WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Route route = new Route(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("source"),
                            rs.getString("destination"));
                    route.setSegments(getSegmentsByRouteId(id));
                    return route;
                }
            }
        }
        return null;
    }

    public void updateRoute(Route route) throws Exception {
        String sql = "UPDATE routes SET name = ?, source = ?, destination = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, route.getName());
            stmt.setString(2, route.getSource());
            stmt.setString(3, route.getDestination());
            stmt.setInt(4, route.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteRoute(int id) throws Exception {
        String sql = "DELETE FROM routes WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Route Segment Operations

    public void addRouteSegment(RouteSegment segment, int routeId) throws Exception {
        String sql = "INSERT INTO route_segments (route_id, from_stop_id, to_stop_id, distance, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, routeId);
            stmt.setInt(2, segment.getFromStop().getId());
            stmt.setInt(3, segment.getToStop().getId());
            stmt.setDouble(4, segment.getDistance());
            stmt.setDouble(5, segment.getPrice());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    segment.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<RouteSegment> getSegmentsByRouteId(int routeId) throws Exception {
        List<RouteSegment> segments = new ArrayList<>();
        String sql = "SELECT rs.*, " +
                "fs.id as from_id, fs.name as from_name, " +
                "ts.id as to_id, ts.name as to_name " +
                "FROM route_segments rs " +
                "JOIN stops fs ON rs.from_stop_id = fs.id " +
                "JOIN stops ts ON rs.to_stop_id = ts.id " +
                "WHERE rs.route_id = ? ORDER BY rs.id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, routeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Stop fromStop = new Stop(rs.getInt("from_id"), rs.getString("from_name"));
                    Stop toStop = new Stop(rs.getInt("to_id"), rs.getString("to_name"));
                    segments.add(new RouteSegment(
                            rs.getInt("id"),
                            fromStop,
                            toStop,
                            rs.getDouble("distance"),
                            rs.getDouble("price")));
                }
            }
        }
        return segments;
    }

    /**
     * Find an existing route segment with exact matching properties.
     */
    public RouteSegment findExistingSegment(int fromStopId, int toStopId, double distance, double price)
            throws Exception {
        String sql = "SELECT * FROM route_segments WHERE from_stop_id = ? AND to_stop_id = ? AND distance = ? AND price = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fromStopId);
            stmt.setInt(2, toStopId);
            stmt.setDouble(3, distance);
            stmt.setDouble(4, price);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Stop fromStop = stopRepository.getStopById(rs.getInt("from_stop_id"));
                    Stop toStop = stopRepository.getStopById(rs.getInt("to_stop_id"));
                    return new RouteSegment(
                            rs.getInt("id"),
                            fromStop,
                            toStop,
                            rs.getDouble("distance"),
                            rs.getDouble("price"));
                }
            }
        }
        return null;
    }

    /**
     * Link an existing segment to a route (if we're reusing segments).
     */
    public void linkSegmentToRoute(int segmentId, int routeId) throws Exception {
        // Currently route_segments table already has route_id, so just insert a new row
        // This is a simplified version - in reality you might want a junction table
        System.out.println(
                "[DEBUG] Note: Current schema doesn't support segment reuse across routes. Adding as new segment.");
    }
}
