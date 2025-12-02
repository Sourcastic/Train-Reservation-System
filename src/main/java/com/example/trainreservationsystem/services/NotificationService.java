package com.example.trainreservationsystem.services;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
  private static NotificationService instance;
  private final List<String> messages = new ArrayList<>();
  private final List<NotificationListener> listeners = new ArrayList<>();

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
