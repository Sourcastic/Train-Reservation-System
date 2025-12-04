package com.example.trainreservationsystem.repositories.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.trainreservationsystem.models.admin.Route;
import com.example.trainreservationsystem.models.admin.RouteSegment;
import com.example.trainreservationsystem.models.admin.Stop;
import com.example.trainreservationsystem.utils.shared.database.Database;

public class RouteRepository {

    public Route addRoute(Route route) throws Exception {
        String sql = "INSERT INTO routes (source, destination) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, route.getSource());
            stmt.setString(2, route.getDestination());
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
        String sql = "SELECT * FROM routes ORDER BY id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            // Collect route IDs for batch loading segments
            List<Integer> routeIds = new ArrayList<>();
            java.util.Map<Integer, Route> routeMap = new java.util.HashMap<>();

            while (rs.next()) {
                int routeId = rs.getInt("id");
                routeIds.add(routeId);

                Route route = new Route(
                        routeId,
                        rs.getString("source"),
                        rs.getString("destination"));
                routeMap.put(routeId, route);
                routes.add(route);
            }

            // Batch load all segments for all routes (fixes N+1)
            if (!routeIds.isEmpty()) {
                java.util.Map<Integer, List<RouteSegment>> segmentsByRoute = getSegmentsByRouteIds(routeIds);
                for (Route route : routes) {
                    List<RouteSegment> segments = segmentsByRoute.getOrDefault(route.getId(), new ArrayList<>());
                    route.setSegments(segments);
                }
            }
        }
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
        String sql = "UPDATE routes SET source = ?, destination = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, route.getSource());
            stmt.setString(2, route.getDestination());
            stmt.setInt(3, route.getId());
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
        List<Integer> routeIds = new ArrayList<>();
        routeIds.add(routeId);
        Map<Integer, List<RouteSegment>> result = getSegmentsByRouteIds(routeIds);
        return result.getOrDefault(routeId, new ArrayList<>());
    }

    /**
     * Batch loads segments for multiple routes with JOINs to avoid N+1 queries.
     * Returns a map of routeId -> list of segments.
     */
    public Map<Integer, List<RouteSegment>> getSegmentsByRouteIds(List<Integer> routeIds) throws Exception {
        Map<Integer, List<RouteSegment>> segmentsByRoute = new java.util.HashMap<>(); // Using fully qualified name
        if (routeIds == null || routeIds.isEmpty()) {
            return segmentsByRoute;
        }

        // Use JOINs to fetch stops in a single query (fixes N+1)
        String placeholders = routeIds.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "SELECT rs.*, " +
                "fs.id as from_stop_id, fs.name as from_stop_name, " +
                "ts.id as to_stop_id, ts.name as to_stop_name " +
                "FROM route_segments rs " +
                "LEFT JOIN stops fs ON rs.from_stop_id = fs.id " +
                "LEFT JOIN stops ts ON rs.to_stop_id = ts.id " +
                "WHERE rs.route_id IN (" + placeholders + ") " +
                "ORDER BY rs.route_id, rs.id";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < routeIds.size(); i++) {
                stmt.setInt(i + 1, routeIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int routeId = rs.getInt("route_id");

                    // Build stops from JOIN results
                    Stop fromStop = null;
                    if (rs.getInt("from_stop_id") > 0) {
                        fromStop = new Stop(rs.getInt("from_stop_id"), rs.getString("from_stop_name"));
                    }

                    Stop toStop = null;
                    if (rs.getInt("to_stop_id") > 0) {
                        toStop = new Stop(rs.getInt("to_stop_id"), rs.getString("to_stop_name"));
                    }

                    RouteSegment segment = new RouteSegment(
                            rs.getInt("id"),
                            fromStop,
                            toStop,
                            rs.getDouble("distance"),
                            rs.getDouble("price"));

                    segmentsByRoute.computeIfAbsent(routeId, k -> new ArrayList<>()).add(segment);
                }
            }
        }
        return segmentsByRoute;
    }
}
