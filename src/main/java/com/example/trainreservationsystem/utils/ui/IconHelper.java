package com.example.trainreservationsystem.utils.ui;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Helper for creating FontAwesome icons consistently.
 * Replaces emoji usage with proper icon library.
 */
public class IconHelper {

  // Icon types for notifications
  public enum IconType {
    BOOKING, PAYMENT, SUCCESS, INFO, WARNING, ERROR
  }

  /**
   * Creates a FontIcon for a notification type.
   */
  public static FontIcon createNotificationIcon(IconType type, int size) {
    FontIcon icon = new FontIcon(getIconForType(type));
    icon.setIconSize(size);
    icon.setIconColor(Color.web("#170d13"));
    return icon;
  }

  /**
   * Creates a FontIcon with custom color.
   */
  public static FontIcon createIcon(FontAwesomeSolid iconType, int size, String color) {
    FontIcon icon = new FontIcon(iconType);
    icon.setIconSize(size);
    icon.setIconColor(Color.web(color));
    return icon;
  }

  /**
   * Adds an icon to a label (replaces text with icon).
   */
  public static void setLabelIcon(Label label, FontAwesomeSolid iconType, int size) {
    FontIcon icon = createIcon(iconType, size, "#170d13");
    label.setGraphic(icon);
    label.setText("");
  }

  private static FontAwesomeSolid getIconForType(IconType type) {
    switch (type) {
      case BOOKING:
        return FontAwesomeSolid.TICKET_ALT;
      case PAYMENT:
        return FontAwesomeSolid.CREDIT_CARD;
      case SUCCESS:
        return FontAwesomeSolid.CHECK_CIRCLE;
      case INFO:
        return FontAwesomeSolid.INFO_CIRCLE;
      case WARNING:
        return FontAwesomeSolid.EXCLAMATION_TRIANGLE;
      case ERROR:
        return FontAwesomeSolid.TIMES_CIRCLE;
      default:
        return FontAwesomeSolid.INFO_CIRCLE;
    }
  }

  /**
   * Removes emoji markers from notification messages.
   */
  public static String cleanMessage(String message) {
    return message.replaceAll("[🎫💳✅⚠️💰ℹ️]", "").trim();
  }

  /**
   * Detects icon type from message content.
   */
  public static IconType detectIconType(String message) {
    String lower = message.toLowerCase();
    if (lower.contains("booking") || lower.contains("ticket") || lower.contains("seat")) {
      return IconType.BOOKING;
    }
    if (lower.contains("payment") || lower.contains("paid") || lower.contains("$")) {
      return IconType.PAYMENT;
    }
    if (lower.contains("success") || lower.contains("confirmed") || lower.contains("completed")) {
      return IconType.SUCCESS;
    }
    if (lower.contains("error") || lower.contains("failed")) {
      return IconType.ERROR;
    }
    if (lower.contains("warning") || lower.contains("⚠")) {
      return IconType.WARNING;
    }
    return IconType.INFO;
  }
}
