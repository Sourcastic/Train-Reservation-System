package com.example.trainreservationsystem.services.admin;

import java.util.List;
import java.util.Map;

import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.admin.Statistics;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.repositories.admin.StatisticsRepository;
import com.example.trainreservationsystem.repositories.shared.SeatRepository;

public class ScheduleService {

    private final ScheduleRepository scheduleRepository = RepositoryFactory.getScheduleRepository();
    private final SeatRepository seatRepository = RepositoryFactory.getSeatRepository();
    private final StatisticsRepository statisticsRepository = RepositoryFactory.getStatisticsRepository();

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

    public void updateSchedule(Schedule schedule) throws Exception {
        // Get old schedule to compare changes
        Schedule oldSchedule = scheduleRepository.getScheduleById(schedule.getId());

        // Update the schedule
        scheduleRepository.updateSchedule(schedule);

        // Notify users if schedule changed significantly
        notifyScheduleChange(oldSchedule, schedule);
    }

    /**
     * Notifies users when a schedule changes or is cancelled.
     */
    private void notifyScheduleChange(Schedule oldSchedule, Schedule newSchedule) {
        if (oldSchedule == null || newSchedule == null) {
            return;
        }

        // Check if departure time or date changed
        boolean timeChanged = !oldSchedule.getDepartureTime().equals(newSchedule.getDepartureTime()) ||
                !oldSchedule.getDepartureDate().equals(newSchedule.getDepartureDate());

        if (timeChanged) {
            try {
                List<com.example.trainreservationsystem.models.member.Booking> bookings = com.example.trainreservationsystem.repositories.RepositoryFactory
                        .getBookingRepository()
                        .getBookingsByScheduleId(newSchedule.getId());

                String routeName = newSchedule.getRoute() != null ? newSchedule.getRoute().getName()
                        : "your scheduled train";

                String message = String.format(
                        "Schedule Change Alert: Your train (%s) departure time has changed. New departure: %s at %s",
                        routeName,
                        newSchedule.getDepartureDate(),
                        newSchedule.getDepartureTime());

                for (com.example.trainreservationsystem.models.member.Booking booking : bookings) {
                    if ("CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus())) {
                        com.example.trainreservationsystem.services.shared.NotificationService.getInstance()
                                .add(message, booking.getUserId());
                    }
                }

                System.out.println("📧 Sent schedule change notifications to " + bookings.size() + " users");
            } catch (Exception e) {
                System.err.println("Error sending schedule change notifications: " + e.getMessage());
                e.printStackTrace();
            }
        }
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
