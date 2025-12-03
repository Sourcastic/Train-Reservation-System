package com.example.trainreservationsystem.controllers.member.payment;

import java.util.HashMap;
import java.util.Map;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.member.Booking;
import com.example.trainreservationsystem.services.member.LoyaltyPointsService;
import com.example.trainreservationsystem.services.shared.ServiceFactory;
import com.example.trainreservationsystem.services.shared.UserSession;
import com.example.trainreservationsystem.utils.member.booking.BookingDisplayHelper;
import com.example.trainreservationsystem.utils.shared.payment.PaymentAdapter;
import com.example.trainreservationsystem.utils.shared.payment.PaymentProcessor;
import com.example.trainreservationsystem.utils.shared.payment.PaymentSuccessHelper;
import com.example.trainreservationsystem.utils.shared.payment.PaymentValidator;
import com.example.trainreservationsystem.utils.shared.payment.PaymentValidator.ValidationResult;
import com.example.trainreservationsystem.utils.shared.payment.adapters.BankTransferPaymentAdapter;
import com.example.trainreservationsystem.utils.shared.payment.adapters.CardPaymentAdapter;
import com.example.trainreservationsystem.utils.shared.payment.adapters.WalletPaymentAdapter;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;
import com.example.trainreservationsystem.utils.shared.ui.IconHelper;
import com.example.trainreservationsystem.utils.shared.ui.InputFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

/**
 * Controller for payment processing.
 * Handles: Card, Bank Transfer, and Wallet (Loyalty Points) payment methods.
 */
public class SimplePaymentController {
  // Display labels
  @FXML
  private Label journeyLabel, dateLabel, passengersLabel, seatsLabel, amountLabel, discountLabel, discountMessageLabel;

  // Discount code field
  @FXML
  private TextField discountCodeField;

  // Icon labels
  @FXML
  private Label paymentHeaderIcon, infoIcon, cardHintIcon, bankTransferHintIcon, walletInfoIcon;

  // Payment accordion
  @FXML
  private Accordion paymentAccordion;
  @FXML
  private TitledPane cardPane, bankTransferPane, walletPane;

  // Card fields
  @FXML
  private TextField cardNumberField, cardNameField, expiryField;
  @FXML
  private PasswordField cvvField;

  // Bank Transfer fields
  @FXML
  private TextField ibanField;

  // Wallet fields
  @FXML
  private Label loyaltyPointsLabel;

  private final com.example.trainreservationsystem.services.booking.BookingService bookingService = ServiceFactory
      .getBookingService();
  private final com.example.trainreservationsystem.services.payment.PaymentService paymentService = ServiceFactory
      .getPaymentService();
  private final LoyaltyPointsService loyaltyPointsService = ServiceFactory.getLoyaltyPointsService();
  private Booking booking;
  private String appliedDiscountCode;
  private double originalAmount;
  private double discountAmount = 0;

  @FXML
  public void initialize() {
    setupIcons();
    booking = UserSession.getInstance().getPendingBooking();
    if (booking != null && booking.getSchedule() != null) {
      displayBookingDetails();
      setupPaymentAccordion();
      setupInputValidation();
      loadLoyaltyPoints();
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

    if (bankTransferHintIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.LIGHTBULB, 14, "#666");
      bankTransferHintIcon.setGraphic(icon);
    }

    if (walletInfoIcon != null) {
      FontIcon icon = IconHelper.createIcon(FontAwesomeSolid.INFO_CIRCLE, 16, "#170d13");
      walletInfoIcon.setGraphic(icon);
    }

    // Set TitledPane icons
    if (cardPane != null) {
      FontIcon cardIcon = IconHelper.createIcon(FontAwesomeSolid.CREDIT_CARD, 16, "#170d13");
      cardPane.setGraphic(cardIcon);
    }

    if (bankTransferPane != null) {
      FontIcon bankIcon = IconHelper.createIcon(FontAwesomeSolid.UNIVERSITY, 16, "#170d13");
      bankTransferPane.setGraphic(bankIcon);
    }

    if (walletPane != null) {
      FontIcon walletIcon = IconHelper.createIcon(FontAwesomeSolid.WALLET, 16, "#170d13");
      walletPane.setGraphic(walletIcon);
    }
  }

  private void displayBookingDetails() {
    BookingDisplayHelper.display(booking, journeyLabel, dateLabel,
        passengersLabel, seatsLabel, amountLabel);
    originalAmount = booking.getTotalAmount();
    updateAmountDisplay();
  }

  private void loadLoyaltyPoints() {
    if (UserSession.getInstance().isLoggedIn() && loyaltyPointsLabel != null) {
      int userId = UserSession.getInstance().getCurrentUser().getId();
      int points = loyaltyPointsService.getLoyaltyPoints(userId);
      loyaltyPointsLabel.setText(points + " points");
    } else if (loyaltyPointsLabel != null) {
      loyaltyPointsLabel.setText("0 points (Login required)");
    }
  }

  @FXML
  private void handleApplyDiscount() {
    String code = discountCodeField.getText().trim();
    if (code.isEmpty()) {
      discountMessageLabel.setText("");
      discountAmount = 0;
      appliedDiscountCode = null;
      updateAmountDisplay();
      return;
    }

    // Get schedule ID from booking if available
    Integer scheduleId = null;
    if (booking != null && booking.getSchedule() != null) {
      scheduleId = booking.getSchedule().getId();
    }

    discountAmount = paymentService.applyDiscountCode(code, originalAmount, scheduleId);
    if (discountAmount > 0) {
      appliedDiscountCode = code.toUpperCase();
      discountMessageLabel.setText("✓ Discount applied: $" + String.format("%.2f", discountAmount));
      discountMessageLabel.setStyle("-fx-text-fill: #2d7a3e;");
    } else {
      appliedDiscountCode = null;
      discountMessageLabel.setText("✗ Invalid or expired discount code");
      discountMessageLabel.setStyle("-fx-text-fill: #d32f2f;");
      discountAmount = 0;
    }
    updateAmountDisplay();
  }

  private void updateAmountDisplay() {
    double finalAmount = originalAmount - discountAmount;
    if (discountAmount > 0) {
      discountLabel.setText("(Original: $" + String.format("%.2f", originalAmount) + ")");
      amountLabel.setText("$" + String.format("%.2f", finalAmount));
    } else {
      discountLabel.setText("");
      amountLabel.setText("$" + String.format("%.2f", originalAmount));
    }

    // Update loyalty points display if wallet pane is visible
    if (UserSession.getInstance().isLoggedIn() && loyaltyPointsLabel != null) {
      int userId = UserSession.getInstance().getCurrentUser().getId();
      int points = loyaltyPointsService.getLoyaltyPoints(userId);
      loyaltyPointsLabel.setText(points + " points");
    }
  }

  private void setupPaymentAccordion() {
    paymentAccordion.setExpandedPane(cardPane);
    paymentAccordion.expandedPaneProperty().addListener((obs, oldPane, newPane) -> {
      if (newPane != null) {
        clearOtherForms(newPane);
        if (newPane == walletPane) {
          loadLoyaltyPoints();
        }
      }
    });
  }

  private void clearOtherForms(TitledPane activePane) {
    if (activePane != cardPane) {
      clearCardFields();
    }
    if (activePane != bankTransferPane) {
      ibanField.clear();
    }
  }

  private void clearCardFields() {
    cardNumberField.clear();
    cardNameField.clear();
    expiryField.clear();
    cvvField.clear();
  }

  private void setupInputValidation() {
    InputFormatter.formatCardNumber(cardNumberField);
    InputFormatter.formatExpiryDate(expiryField);
    InputFormatter.restrictToDigits(cvvField, 3);
  }

  @FXML
  public void handleCardPayment() {
    if (!isBookingValid())
      return;
    ValidationResult result = PaymentValidator.validateCard(
        cardNumberField.getText(), cardNameField.getText(),
        expiryField.getText(), cvvField.getText());

    if (!result.isValid()) {
      AlertUtils.showWarning("Validation Error", result.getMessage());
      return;
    }

    // Create adapter and payment details
    PaymentAdapter adapter = new CardPaymentAdapter();
    Map<String, String> paymentDetails = new HashMap<>();
    paymentDetails.put("cardNumber", cardNumberField.getText().replaceAll("\\s", ""));
    paymentDetails.put("cardName", cardNameField.getText());
    paymentDetails.put("expiry", expiryField.getText());
    paymentDetails.put("cvv", cvvField.getText());

    processPayment(adapter, paymentDetails);
  }

  @FXML
  public void handleBankTransferPayment() {
    if (!isBookingValid())
      return;
    ValidationResult result = PaymentValidator.validateBankTransfer(ibanField.getText());

    if (!result.isValid()) {
      AlertUtils.showWarning("Validation Error", result.getMessage());
      return;
    }

    // Create adapter and payment details
    PaymentAdapter adapter = new BankTransferPaymentAdapter();
    Map<String, String> paymentDetails = new HashMap<>();
    paymentDetails.put("iban", ibanField.getText().replaceAll("\\s", "").toUpperCase());

    processPayment(adapter, paymentDetails);
  }

  @FXML
  public void handleWalletPayment() {
    if (!isBookingValid())
      return;

    if (!UserSession.getInstance().isLoggedIn()) {
      AlertUtils.showError("Error", "You must be logged in to use wallet payment");
      return;
    }

    int userId = UserSession.getInstance().getCurrentUser().getId();
    int availablePoints = loyaltyPointsService.getLoyaltyPoints(userId);
    double finalAmount = originalAmount - discountAmount;

    ValidationResult result = PaymentValidator.validateWallet(availablePoints, finalAmount);

    if (!result.isValid()) {
      AlertUtils.showWarning("Validation Error", result.getMessage());
      return;
    }

    // Create adapter and payment details
    PaymentAdapter adapter = new WalletPaymentAdapter();
    Map<String, String> paymentDetails = new HashMap<>();
    paymentDetails.put("amount", String.valueOf(finalAmount));
    paymentDetails.put("points", String.valueOf(availablePoints));

    processPayment(adapter, paymentDetails);
  }

  private boolean isBookingValid() {
    if (booking == null || booking.getSchedule() == null) {
      AlertUtils.showError("Error", "Invalid booking information");
      return false;
    }
    return true;
  }

  private void processPayment(PaymentAdapter adapter, Map<String, String> paymentDetails) {
    PaymentProcessor.process(booking, bookingService, adapter, paymentDetails, appliedDiscountCode,
        () -> showSuccessAndRedirect(adapter.getMethodName()),
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
