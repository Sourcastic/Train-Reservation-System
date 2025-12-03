package com.example.trainreservationsystem.models;

/**
 * Represents a physical seat on a schedule.
 */
public class Seat {
    private int id;
    private SeatClass seatClass;
    private boolean isBooked;

    public Seat() {
    }

    public Seat(int id, SeatClass seatClass) {
        this.id = id;
        this.seatClass = seatClass;
        this.isBooked = false;
    }

    // Getters & setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}
