package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Notification;
import com.example.trainreservationsystem.repositories.NotificationRepository;
import com.example.trainreservationsystem.models.Booking;

public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void sendNotification(int userId, String message) {
        Notification n = new Notification(userId, message);
        repo.saveNotification(n);
        System.out.println("Notification sent to user " + userId + ": " + message);
    }

    // Sends reminder for a specific booking
    public void sendReminderForBooking(Booking booking) {
        String message = "Reminder: Your journey is scheduled on " +
                booking.getJourneyDate().toLocalDate() + ". Please arrive on time.";
        sendNotification(booking.getUserId(), message);
    }

}
