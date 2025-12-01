package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Notification;
import com.example.trainreservationsystem.repositories.NotificationsRepository;
import com.example.trainreservationsystem.repositories.NotificationsRepository;

public class NotificationsService {

    private NotificationsRepository repository;

    public NotificationsService(NotificationsRepository repository) {
        this.repository = repository;
    }

    public void sendNotification(int userId, String message) {

        // 1. Store inside the DB
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        notif.setSent(false);

        repository.save(notif);

        // 2. Trigger email/SMS (fake for now)
        System.out.println("SENDING EMAIL/SMS TO USER " + userId + ": " + message);

        // 3. Update DB → mark as sent
        // (in real project, you retrieve last insert id)
    }
}
