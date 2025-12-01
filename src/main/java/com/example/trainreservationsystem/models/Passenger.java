package com.example.trainreservationsystem.models;

public class Passenger {
  private int id;
  private String name;
  private int age;
  private boolean bringPet;
  private boolean hasWheelchair;

  public Passenger() {
  }

  public Passenger(String name, int age, boolean bringPet, boolean hasWheelchair) {
    this.name = name;
    this.age = age;
    this.bringPet = bringPet;
    this.hasWheelchair = hasWheelchair;
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

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public boolean isBringPet() {
    return bringPet;
  }

  public void setBringPet(boolean bringPet) {
    this.bringPet = bringPet;
  }

  public boolean isHasWheelchair() {
    return hasWheelchair;
  }

  public void setHasWheelchair(boolean hasWheelchair) {
    this.hasWheelchair = hasWheelchair;
  }
}
