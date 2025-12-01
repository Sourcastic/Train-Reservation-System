package com.example.trainreservationsystem.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.Schedule;

public class TrainRepository {

  // Mock Data
  public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
    List<Schedule> schedules = new ArrayList<>();
    // Hardcoded mock return
    Route r = new Route(1, "Express 101", source, destination);
    Schedule s = new Schedule(1, r, date, LocalTime.of(9, 0), LocalTime.of(12, 0), 50.0, 100);
    schedules.add(s);
    return schedules;
  }

  public Schedule getScheduleById(int id) {
    Route r = new Route(1, "Express 101", "Mock Source", "Mock Dest");
    return new Schedule(id, r, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(12, 0), 50.0, 100);
  }
}
