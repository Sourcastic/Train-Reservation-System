package com.example.trainreservationsystem.models;

/**
 * Represents a class of seat (e.g., Business, First, Coach).
 */
public class SeatClass {
    private int id;
    private String name;
    private double baseFare; // additional fare on top of route price
    private String description;

    public SeatClass() {
    }

    public SeatClass(int id, String name, double baseFare, String description) {
        this.id = id;
        this.name = name;
        this.baseFare = baseFare;
        this.description = description;
    }

    // Getters & setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
