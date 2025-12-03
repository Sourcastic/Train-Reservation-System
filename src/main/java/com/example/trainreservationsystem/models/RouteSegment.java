package com.example.trainreservationsystem.models;

/**
 * Represents a segment connecting two stops on a route.
 */
public class RouteSegment {
    private int id;
    private Stop fromStop;
    private Stop toStop;
    private double distance; // in kilometers
    private double price; // price for this segment

    public RouteSegment() {
    }

    public RouteSegment(int id, Stop fromStop, Stop toStop, double distance, double price) {
        this.id = id;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.distance = distance;
        this.price = price;
    }

    // Getters & setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Stop getFromStop() {
        return fromStop;
    }

    public void setFromStop(Stop fromStop) {
        this.fromStop = fromStop;
    }

    public Stop getToStop() {
        return toStop;
    }

    public void setToStop(Stop toStop) {
        this.toStop = toStop;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
