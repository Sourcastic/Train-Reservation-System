package com.example.trainreservationsystem.models;

public class Route {
  private int id;
  private String name;
  private String source;
  private String destination;

  public Route() {
  }

  public Route(int id, String name, String source, String destination) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.destination = destination;
  }

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
}
