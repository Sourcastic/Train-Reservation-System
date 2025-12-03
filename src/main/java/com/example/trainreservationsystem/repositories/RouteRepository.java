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
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Route route = new Route(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("source"),
                        rs.getString("destination"));
                route.setSegments(getSegmentsByRouteId(route.getId()));
                routes.add(route);
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
        String sql = "SELECT * FROM route_segments WHERE route_id = ? ORDER BY id";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, routeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Stop fromStop = stopRepository.getStopById(rs.getInt("from_stop_id"));
                    Stop toStop = stopRepository.getStopById(rs.getInt("to_stop_id"));
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
}
