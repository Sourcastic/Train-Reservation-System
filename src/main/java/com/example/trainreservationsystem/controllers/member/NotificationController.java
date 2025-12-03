package com.example.trainreservationsystem.controllers.member;

import java.util.List;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.example.trainreservationsystem.services.shared.NotificationService;
import com.example.trainreservationsystem.utils.shared.ui.IconHelper;
import com.example.trainreservationsystem.utils.shared.ui.IconHelper.IconType;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class NotificationController {
  @FXML
  private VBox notificationList;
  @FXML
  private VBox emptyState;
  @FXML
  private Button allTab;
  @FXML
  private Button bookingsTab;
  @FXML
  private Button paymentsTab;
  @FXML
  private Button alertsTab;
  @FXML
  private Button clearAllBtn;

  private String currentFilter = "all";

  @FXML
  public void initialize() {
    loadNotifications();
  }

  private void loadNotifications() {
    List<String> notifications = NotificationService.getInstance().getAll();
    notificationList.getChildren().clear();

    if (notifications.isEmpty()) {
      showEmptyState();
    } else {
      hideEmptyState();
      // Add notifications in reverse order (newest first)
      for (int i = notifications.size() - 1; i >= 0; i--) {
        addNotificationCard(notifications.get(i), i);
      }
    }
  }

  private void addNotificationCard(String message, int index) {
    // Filter based on current tab
    if (!matchesFilter(message))
      return;

    HBox card = new HBox();
    card.getStyleClass().add("notification-card");
    card.setSpacing(12);
    card.setPadding(new Insets(12, 16, 12, 16));
    card.setAlignment(Pos.CENTER_LEFT);

    // Icon based on notification type using FontAwesome
    Label iconLabel = new Label();
    IconType iconType = IconHelper.detectIconType(message);
    FontIcon icon = IconHelper.createNotificationIcon(iconType, 20);
    iconLabel.setGraphic(icon);

    // Add style class based on type
    switch (iconType) {
      case PAYMENT:
        card.getStyleClass().add("payment-notification");
        break;
      case BOOKING:
        card.getStyleClass().add("booking-notification");
        break;
      case SUCCESS:
        card.getStyleClass().add("success-notification");
        break;
      default:
        card.getStyleClass().add("info-notification");
    }

    // Message content
    VBox content = new VBox(4);
    HBox.setHgrow(content, Priority.ALWAYS);

    Label messageLabel = new Label(IconHelper.cleanMessage(message));
    messageLabel.getStyleClass().add("notification-message");
    messageLabel.setWrapText(true);

    Label timeLabel = new Label(getRelativeTime(index));
    timeLabel.getStyleClass().add("notification-time");

    content.getChildren().addAll(messageLabel, timeLabel);

    // Dismiss button with icon
    Button dismissBtn = new Button();
    FontIcon closeIcon = IconHelper.createIcon(FontAwesomeSolid.TIMES, 14, "#666");
    dismissBtn.setGraphic(closeIcon);
    dismissBtn.getStyleClass().add("dismiss-button");
    dismissBtn.setOnAction(e -> {
      NotificationService.getInstance().remove(index);
      loadNotifications();
    });

    card.getChildren().addAll(iconLabel, content, dismissBtn);
    notificationList.getChildren().add(card);
  }

  private boolean matchesFilter(String message) {
    switch (currentFilter) {
      case "bookings":
        return message.contains("Booking") || message.contains("ticket") || message.contains("seat");
      case "payments":
        return message.contains("Payment") || message.contains("paid") || message.contains("$");
      case "alerts":
        return message.contains("Error") || message.contains("Failed") || message.contains("warning");
      default:
        return true;
    }
  }

  private String getRelativeTime(int index) {
    // Simulate time based on index (newer = lower index)
    if (index == 0)
      return "Just now";
    if (index == 1)
      return "1 minute ago";
    if (index < 5)
      return index + " minutes ago";
    if (index < 10)
      return "30 minutes ago";
    return "1 hour ago";
  }

  private void showEmptyState() {
    emptyState.setVisible(true);
    emptyState.setManaged(true);
  }

  private void hideEmptyState() {
    emptyState.setVisible(false);
    emptyState.setManaged(false);
  }

  private void updateTabStyles() {
    allTab.getStyleClass().remove("tab-active");
    bookingsTab.getStyleClass().remove("tab-active");
    paymentsTab.getStyleClass().remove("tab-active");
    alertsTab.getStyleClass().remove("tab-active");
  }

  @FXML
  public void showAll() {
    updateTabStyles();
    allTab.getStyleClass().add("tab-active");
    currentFilter = "all";
    loadNotifications();
  }

  @FXML
  public void showBookings() {
    updateTabStyles();
    bookingsTab.getStyleClass().add("tab-active");
    currentFilter = "bookings";
    loadNotifications();
  }

  @FXML
  public void showPayments() {
    updateTabStyles();
    paymentsTab.getStyleClass().add("tab-active");
    currentFilter = "payments";
    loadNotifications();
  }

  @FXML
  public void showAlerts() {
    updateTabStyles();
    alertsTab.getStyleClass().add("tab-active");
    currentFilter = "alerts";
    loadNotifications();
  }

  @FXML
  public void handleClearAll() {
    NotificationService.getInstance().clear();
    loadNotifications();
  }
}
