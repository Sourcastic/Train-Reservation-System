package com.example.trainreservationsystem.utils.ui;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Formats text input fields (card numbers, dates, etc.).
 */
public class InputFormatter {

  public static void formatCardNumber(TextField field) {
    field.textProperty().addListener((obs, old, val) -> {
      if (val == null)
        return;
      String digits = val.replaceAll("[^\\d]", "");
      if (digits.length() > 16) {
        digits = digits.substring(0, 16);
      }
      String formatted = formatAsGroups(digits, 4, " ");
      if (!formatted.equals(val)) {
        field.setText(formatted);
      }
    });
  }

  public static void formatExpiryDate(TextField field) {
    field.textProperty().addListener((obs, old, val) -> {
      if (val == null)
        return;
      if (!val.matches("\\d{0,2}/?\\d{0,2}")) {
        field.setText(old);
        return;
      }
      if (val.length() == 2 && !val.contains("/")) {
        field.setText(val + "/");
      }
    });
  }

  public static void restrictToDigits(TextField field, int maxLength) {
    field.textProperty().addListener((obs, old, val) -> {
      if (val == null)
        return;
      if (!val.matches("\\d{0," + maxLength + "}")) {
        field.setText(old);
      }
    });
  }

  public static void restrictToDigits(PasswordField field, int maxLength) {
    field.textProperty().addListener((obs, old, val) -> {
      if (val == null)
        return;
      if (!val.matches("\\d{0," + maxLength + "}")) {
        field.setText(old);
      }
    });
  }

  private static String formatAsGroups(String input, int groupSize, String separator) {
    if (input.isEmpty())
      return "";
    StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      if (i > 0 && i % groupSize == 0) {
        formatted.append(separator);
      }
      formatted.append(input.charAt(i));
    }
    return formatted.toString();
  }
}
