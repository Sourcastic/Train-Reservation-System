package com.example.trainreservationsystem.services;

import java.time.LocalDate;
import java.util.List;

import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.repositories.BookingRepository;
import com.example.trainreservationsystem.repositories.TrainRepository;

public class TrainService {

    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public TrainService(TrainRepository trainRepository,
                        BookingRepository bookingRepository,
                        NotificationService notificationService) {
        this.trainRepository = trainRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    public List<Schedule> searchSchedules(String source, String destination, LocalDate date) {
        return trainRepository.searchSchedules(source, destination, date);
    }

    public Schedule getSchedule(int id) {
        return trainRepository.getScheduleById(id);
    }

    public List<Schedule> getAllSchedules() {
        return trainRepository.getAllSchedules();
    }

    // Any schedule change triggers notifications
    public void updateScheduleAndNotify(int scheduleId, String newStatus) {

        // 1) Update schedule
        trainRepository.updateScheduleStatus(scheduleId, newStatus);

        // 2) Find affected bookings
        List<Booking> bookings = bookingRepository.getBookingsBySchedule(scheduleId);

        // 3) Build message
        Schedule schedule = trainRepository.getScheduleById(scheduleId);
        String message = "Your train from " + schedule.getRoute().getSource() +
                " to " + schedule.getRoute().getDestination() +
                " has a schedule update: " + newStatus;

        // 4) Notify all passengers
        for (Booking b : bookings) {
            notificationService.sendNotification(b.getUserId(), message);
        }
    }

    // Fetch all upcoming bookings for a specific date and send reminders
    public void sendRemindersForUpcomingJourneys(LocalDate date) {
        List<Booking> bookings = bookingRepository.getBookingsByJourneyDate(date);
        for (Booking b : bookings) {
            notificationService.sendReminderForBooking(b);
        }
    }

}
