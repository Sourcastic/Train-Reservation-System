package com.example.trainreservationsystem.models.admin;

import java.util.ArrayList;
import java.util.List;

/**
 * A route is a collection of ordered RouteSegments.
 */
public class Route {
  private int id;
  private String name;
  private String source;
  private String destination;
  private List<RouteSegment> segments = new ArrayList<>();

  public Route() {
  }

  public Route(int id, String name, String source, String destination) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.destination = destination;
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

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public List<RouteSegment> getSegments() {
    return segments;
  }

  public void setSegments(List<RouteSegment> segments) {
    this.segments = segments;
  }

  /**
   * Add a segment to the route (maintains order).
   */
  public void addSegment(RouteSegment segment) {
    this.segments.add(segment);
  }

  /**
   * Total distance of the whole route (sum of segment distances).
   */
  public double totalDistance() {
    return segments.stream()
        .mapToDouble(RouteSegment::getDistance)
        .sum();
  }

  /**
   * Total price of the whole route (sum of segment prices).
   */
  public double totalPrice() {
    return segments.stream()
        .mapToDouble(RouteSegment::getPrice)
        .sum();
  }
}
