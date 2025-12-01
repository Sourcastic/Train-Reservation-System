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

    public List<Schedule> getAllSchedules() {
        return repository.getAllSchedules();
    }

    public void updateScheduleStatusAndNotify(int scheduleId, String status) {
        // Update status
        repository.updateScheduleStatus(scheduleId, status);

        // Notify passengers
        String message = "Your train " + repository.getScheduleById(scheduleId).getRoute().getSource() +
                " → " + repository.getScheduleById(scheduleId).getRoute().getDestination() +
                " has been updated to " + status + ".";
        repository.notifyPassengers(scheduleId, message);
    }


}
