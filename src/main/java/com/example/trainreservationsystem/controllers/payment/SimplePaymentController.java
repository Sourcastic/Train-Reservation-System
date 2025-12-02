package com.example.trainreservationsystem.controllers.payment;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.example.trainreservationsystem.controllers.HomeController;
import com.example.trainreservationsystem.models.Booking;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.services.booking.BookingService;
import com.example.trainreservationsystem.utils.booking.BookingDisplayHelper;
import com.example.trainreservationsystem.utils.payment.PaymentProcessor;
import com.example.trainreservationsystem.utils.payment.PaymentSuccessHelper;
import com.example.trainreservationsystem.utils.payment.PaymentValidator;
import com.example.trainreservationsystem.utils.payment.PaymentValidator.ValidationResult;
import com.example.trainreservationsystem.utils.ui.AlertUtils;
import com.example.trainreservationsystem.utils.ui.IconHelper;
import com.example.trainreservationsystem.utils.ui.InputFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

/**
 * Controller for payment processing.
 * Handles: card, cash, and JazzCash payment methods.
 */
public class SimplePaymentController {
  // Display labels
  @FXML
  private Label journeyLabel, dateLabel, passengersLabel, seatsLabel, amountLabel;

  // Icon labels
  @FXML
  private Label paymentHeaderIcon, cashIcon, infoIcon, cardHintIcon, jazzCashHintIcon;

  // Payment accordion
  @FXML
  private Accordion paymentAccordion;
  @FXML
  private TitledPane cardPane, cashPane, jazzCashPane;

  // Card fields
  @FXML
  private TextField cardNumberField, cardNameField, expiryField;
  @FXML
  private PasswordField cvvField;

  // Cash fields
  @FXML
  private TextField cashContactField;

  // JazzCash fields
  @FXML
  private TextField jazzCashNumberField;
  @FXML
  private PasswordField jazzCashPinField;

  private final BookingService bookingService = ServiceFactory.getBookingService();
  private Booking booking;

  @FXML
  public void initialize() {
    setupIcons();
    booking = UserSession.getInstance().getPendingBooking();
    if (booking != null && booking.getSchedule() != null) {
      displayBookingDetails();
      setupPaymentAccordion();
      setupInputValidation();
    } else {
      AlertUtils.showError("Error", "No booking information found");
    }
  }

  private void setupIcons() {
    // Header icon
    if (paymentHeaderIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.CREDIT_CARD, 32, "#170d13");
      paymentHeaderIcon.setGraphic(icon);
    }

    // Cash icon
    if (cashIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.UNIVERSITY, 48, "#170d13");
      cashIcon.setGraphic(icon);
    }

    // Info icon
    if (infoIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.INFO_CIRCLE, 16, "#170d13");
      infoIcon.setGraphic(icon);
    }

    // Hint icons
    if (cardHintIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.LIGHTBULB, 14, "#666");
      cardHintIcon.setGraphic(icon);
    }

    if (jazzCashHintIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.LIGHTBULB, 14, "#666");
      jazzCashHintIcon.setGraphic(icon);
    }

    // Set TitledPane icons
    if (cardPane != null) {
      FontIcon cardIcon = IconHelper.createIcon(FontAwesomeSolid.CREDIT_CARD, 16, "#170d13");
      cardPane.setGraphic(cardIcon);
    }

    if (cashPane != null) {
      FontIcon cashPaneIcon = IconHelper.createIcon(FontAwesomeSolid.MONEY_BILL_ALT, 16, "#170d13");
      cashPane.setGraphic(cashPaneIcon);
    }

    if (jazzCashPane != null) {
      FontIcon jazzIcon = IconHelper.createIcon(FontAwesomeSolid.MOBILE_ALT, 16, "#170d13");
      jazzCashPane.setGraphic(jazzIcon);
    }
  }

  private void displayBookingDetails() {
    BookingDisplayHelper.display(booking, journeyLabel, dateLabel,
        passengersLabel, seatsLabel, amountLabel);
  }

  private void setupPaymentAccordion() {
    paymentAccordion.setExpandedPane(cardPane);
    paymentAccordion.expandedPaneProperty().addListener((obs, oldPane, newPane) -> {
      if (newPane != null) {
        clearOtherForms(newPane);
      }
    });
  }

  private void clearOtherForms(TitledPane activePane) {
    if (activePane != cardPane) {
      clearCardFields();
    }
    if (activePane != cashPane) {
      cashContactField.clear();
    }
    if (activePane != jazzCashPane) {
      clearJazzCashFields();
    }
  }

  private void clearCardFields() {
    cardNumberField.clear();
    cardNameField.clear();
    expiryField.clear();
    cvvField.clear();
  }

  private void clearJazzCashFields() {
    jazzCashNumberField.clear();
    jazzCashPinField.clear();
  }

  private void setupInputValidation() {
    InputFormatter.formatCardNumber(cardNumberField);
    InputFormatter.formatExpiryDate(expiryField);
    InputFormatter.restrictToDigits(cvvField, 3);
    InputFormatter.restrictToDigits(jazzCashPinField, 5);
  }

  @FXML
  public void handleCardPayment() {
    if (!isBookingValid())
      return;
    ValidationResult result = PaymentValidator.validateCard(
        cardNumberField.getText(), cardNameField.getText(),
        expiryField.getText(), cvvField.getText());
    processIfValid(result, "Credit/Debit Card");
  }

  @FXML
  public void handleCashPayment() {
    if (!isBookingValid())
      return;
    ValidationResult result = PaymentValidator.validateCash(cashContactField.getText());
    processIfValid(result, "Cash on Delivery");
  }

  @FXML
  public void handleJazzCashPayment() {
    if (!isBookingValid())
      return;
    ValidationResult result = PaymentValidator.validateJazzCash(
        jazzCashNumberField.getText(), jazzCashPinField.getText());
    processIfValid(result, "JazzCash");
  }

  private void processIfValid(ValidationResult result, String paymentMethod) {
    if (!result.isValid()) {
      AlertUtils.showWarning("Validation Error", result.getMessage());
      return;
    }
    processPayment(paymentMethod, result.getDetails());
  }

  private boolean isBookingValid() {
    if (booking == null || booking.getSchedule() == null) {
      AlertUtils.showError("Error", "Invalid booking information");
      return false;
    }
    return true;
  }

  private void processPayment(String paymentMethod, String details) {
    PaymentProcessor.process(booking, bookingService, paymentMethod, details,
        () -> showSuccessAndRedirect(paymentMethod),
        () -> AlertUtils.showError("Payment Failed", "Failed to process payment"));
  }

  private void showSuccessAndRedirect(String paymentMethod) {
    PaymentSuccessHelper.showSuccessAndRedirect(booking, amountLabel, paymentMethod);
  }

  @FXML
  public void handleCancel() {
    if (AlertUtils.showConfirmation("Cancel Payment", "Are you sure you want to cancel this payment?")) {
      UserSession.getInstance().setPendingBooking(null);
      HomeController.getInstance().showSearch();
    }
  }
}
