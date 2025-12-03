package com.example.trainreservationsystem.utils.shared.ui;

import javafx.scene.control.Alert;

/**
 * Utility for showing alerts consistently.
 * Simplifies alert creation across the app.
 */
public class AlertUtils {

  private static final String STYLESHEET_PATH = "/com/example/trainreservationsystem/stylesheet.css";

  public static void showInfo(String title, String content) {
    showAlert(Alert.AlertType.INFORMATION, title, content, "custom-alert");
  }

  public static void showError(String title, String content) {
    showAlert(Alert.AlertType.ERROR, title, content, "custom-alert");
  }

  public static void showWarning(String title, String content) {
    showAlert(Alert.AlertType.WARNING, title, content, "custom-alert");
  }

  public static void showSuccess(String title, String content) {
    showAlert(Alert.AlertType.INFORMATION, title, content, "success-alert");
  }

  public static boolean showConfirmation(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    applyStyles(alert, "custom-alert");
    return alert.showAndWait().filter(response -> response == javafx.scene.control.ButtonType.OK).isPresent();
  }

  private static void showAlert(Alert.AlertType type, String title, String content, String styleClass) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    applyStyles(alert, styleClass);
    alert.showAndWait();
  }

  private static void applyStyles(Alert alert, String styleClass) {
    if (alert.getDialogPane().getScene() != null &&
        alert.getDialogPane().getScene().getWindow() != null) {
      alert.getDialogPane().getStylesheets().add(
          AlertUtils.class.getResource(STYLESHEET_PATH).toExternalForm());
      alert.getDialogPane().getStyleClass().add(styleClass);
    }
  }
}
