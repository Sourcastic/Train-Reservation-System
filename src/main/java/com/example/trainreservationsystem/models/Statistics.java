package com.example.trainreservationsystem.models;

import java.time.LocalTime;
import java.util.Map;

/**
 * Stores aggregated statistics for a particular schedule run.
 */
public class Statistics {
    private int id;
    private int scheduleId;
    private String dayOfWeek; // e.g., "Monday"
    private LocalTime departureTime;
    private int seatClassId; // optional – can be omitted when using the map
    private int seatsSold;

    // When we want per‑class breakdown we use this map
    private Map<Integer, Long> seatsSoldPerClass;

    public Statistics() {
    }

    // Getters & setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(int seatClassId) {
        this.seatClassId = seatClassId;
    }

    public int getSeatsSold() {
        return seatsSold;
    }

    public void setSeatsSold(int seatsSold) {
        this.seatsSold = seatsSold;
    }

    public Map<Integer, Long> getSeatsSoldPerClass() {
        return seatsSoldPerClass;
    }

    public void setSeatsSoldPerClass(Map<Integer, Long> seatsSoldPerClass) {
        this.seatsSoldPerClass = seatsSoldPerClass;
    }
}
