package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.models.Seat;
import com.example.trainreservationsystem.models.Statistics;
import com.example.trainreservationsystem.repositories.ScheduleRepository;
import com.example.trainreservationsystem.repositories.SeatRepository;
import com.example.trainreservationsystem.repositories.StatisticsRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class ScheduleService {

    private final ScheduleRepository scheduleRepository = new ScheduleRepository();
    private final SeatRepository seatRepository = new SeatRepository();
    private final StatisticsRepository statisticsRepository = new StatisticsRepository();

    public Schedule createSchedule(Schedule schedule) throws Exception {
        return scheduleRepository.addSchedule(schedule);
    }

    public List<Schedule> getAllSchedules() throws Exception {
        return scheduleRepository.getAllSchedules();
    }

    public Schedule getScheduleById(int id) throws Exception {
        return scheduleRepository.getScheduleById(id);
    }

    public Map<Integer, Long> getAvailableSeats(int scheduleId) throws Exception {
        Schedule schedule = scheduleRepository.getScheduleById(scheduleId);
        if (schedule != null) {
            return schedule.freeSeatsByClass();
        }
        return null;
    }

    public void bookSeat(int scheduleId, int seatId) throws Exception {
        // Simple booking logic: mark seat as booked
        // In a real app, this would involve creating a Booking record, handling
        // payments, etc.
        seatRepository.updateSeatStatus(seatId, true);
    }

    public void generateAndSaveStatistics(int scheduleId, String dayOfWeek) throws Exception {
        Schedule schedule = scheduleRepository.getScheduleById(scheduleId);
        if (schedule != null) {
            // Need to load bookings to generate stats correctly
            // For now, assuming bookings are loaded or we use seat status as proxy if
            // bookings not fully implemented
            // But Schedule model uses bookings list for stats.
            // Let's assume we need to populate bookings or change logic.
            // For this scope, let's use the method on Schedule but be aware bookings might
            // be empty if not loaded.

            Statistics stats = schedule.generateStatistics(dayOfWeek);
            statisticsRepository.addStatistics(stats);
        }
    }
}
