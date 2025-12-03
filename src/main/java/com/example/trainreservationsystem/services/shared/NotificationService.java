package com.example.trainreservationsystem.services.shared;

import java.util.ArrayList;
import java.util.List;

import com.example.trainreservationsystem.models.member.Notification;
import com.example.trainreservationsystem.repositories.member.NotificationRepository;

public class NotificationService {
  private static NotificationService instance;
  private final List<String> messages = new ArrayList<>();
  private final List<NotificationListener> listeners = new ArrayList<>();
  private final NotificationRepository repository = com.example.trainreservationsystem.repositories.RepositoryFactory
      .getNotificationRepository();

  private NotificationService() {
  }

  public static synchronized NotificationService getInstance() {
    if (instance == null)
      instance = new NotificationService();
    return instance;
  }

  public void add(String message) {
    messages.add(0, message); // latest first
    notifyListeners();
  }

  /**
   * Adds a notification and persists to database.
   * Write-through operation.
   */
  public void add(String message, int userId) {
    // Add to in-memory list
    messages.add(0, message); // latest first

    // Persist to database
    try {
      Notification notification = new Notification();
      notification.setUserId(userId);
      notification.setMessage(message);
      notification.setSent(false);
      repository.saveNotification(notification);
    } catch (Exception e) {
      System.err.println("Error persisting notification: " + e.getMessage());
    }

    notifyListeners();
  }

  /**
   * Loads notifications for a user from database.
   * Called on login.
   */
  public void loadNotificationsForUser(int userId) {
    try {
      messages.clear();
      List<Notification> notifications = repository.getNotificationsByUserId(userId);
      for (Notification n : notifications) {
        messages.add(n.getMessage());
      }
      notifyListeners();
      System.out.println("📧 Loaded " + messages.size() + " notifications for user " + userId);
    } catch (Exception e) {
      System.err.println("Error loading notifications: " + e.getMessage());
    }
  }

  public List<String> getAll() {
    return new ArrayList<>(messages);
  }

  public void remove(int index) {
    if (index >= 0 && index < messages.size()) {
      messages.remove(index);
      notifyListeners();
    }
  }

  public void clear() {
    messages.clear();
    notifyListeners();
  }

  public int getCount() {
    return messages.size();
  }

  public void addListener(NotificationListener listener) {
    listeners.add(listener);
  }

  public void removeListener(NotificationListener listener) {
    listeners.remove(listener);
  }

  private void notifyListeners() {
    for (NotificationListener listener : listeners) {
      listener.onNotificationUpdate(messages.size());
    }
  }

  public interface NotificationListener {
    void onNotificationUpdate(int count);
  }
}
