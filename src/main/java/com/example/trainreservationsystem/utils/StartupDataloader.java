package com.example.trainreservationsystem.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// Simple DTOs for in-memory caching
class Route {
    int id;
    String name;
    String source;
    String destination;

    Route(int id, String name, String source, String destination) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.destination = destination;
    }
}

class Schedule {
    int id;
    int routeId;
    String departureDate;
    String departureTime;
    String arrivalTime;
    int capacity;
    double price;

    Schedule(int id, int routeId, String departureDate, String departureTime, String arrivalTime, int capacity, double price) {
        this.id = id;
        this.routeId = routeId;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.capacity = capacity;
        this.price = price;
    }
}

public class StartupDataloader {

    public static List<Route> routes = new ArrayList<>();
    public static List<Schedule> schedules = new ArrayList<>();

    public static void loadData() {
        try (Connection conn = Database.getConnection()) {

            // Load Routes
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM routes");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    routes.add(new Route(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("source"),
                            rs.getString("destination")
                    ));
                }
            }

            // Load Schedules
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM schedules");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedules.add(new Schedule(
                            rs.getInt("id"),
                            rs.getInt("route_id"),
                            rs.getDate("departure_date").toString(),
                            rs.getTime("departure_time").toString(),
                            rs.getTime("arrival_time").toString(),
                            rs.getInt("capacity"),
                            rs.getDouble("price")
                    ));
                }
            }

            System.out.println("Startup data loaded: " + routes.size() + " routes, " + schedules.size() + " schedules.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
