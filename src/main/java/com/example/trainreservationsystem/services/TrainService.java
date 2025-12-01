package com.example.trainreservationsystem.services;

import java.time.LocalDate;
import java.util.List;

import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.repositories.TrainRepository;

public class TrainService {
  private final TrainRepository repository;

  public TrainService(TrainRepository repository) {
    this.repository = repository;
  }

  public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
    return repository.searchSchedules(source, destination, date);
  }

  public Schedule getSchedule(int id) {
    return repository.getScheduleById(id);
  }
}
