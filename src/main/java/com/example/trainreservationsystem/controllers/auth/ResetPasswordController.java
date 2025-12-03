package com.example.trainreservationsystem.controllers.auth;

import com.example.trainreservationsystem.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ResetPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField otpField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button sendOtpButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button backButton;

    private final AuthService authService = AuthService.getInstance();
    private static final Map<String, String> otpStorage = new HashMap<>();

    @FXML
    public void handleSendOTP() {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            showError("Please enter your email address.");
            return;
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);

        // Print to console as requested
        System.out.println("OTP for " + email + ": " + otp);

        showSuccess("OTP sent to console (simulated).");
        errorLabel.setVisible(false);
    }

    @FXML
    public void handleResetPassword() {
        String email = emailField.getText();
        String otp = otpField.getText();
        String newPassword = newPasswordField.getText();

        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty() || newPassword == null
                || newPassword.trim().isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            boolean success = authService.resetPassword(email, newPassword);
            if (success) {
                showSuccess("Password reset successfully! Redirecting to login...");
                otpStorage.remove(email); // Clear OTP
                // Redirect to login after a short delay or immediately
                try {
                    // Small delay to let user see success message could be added here if using a
                    // timeline,
                    // but for simplicity we'll redirect immediately or let them click back.
                    // Actually, let's redirect immediately for better flow as requested "brought
                    // back to login screen"
                    handleBack();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showError("Failed to reset password. Please try again.");
            }
        } else {
            showError("Invalid OTP.");
        }
    }

    @FXML
    public void handleBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/trainreservationsystem/login-view.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 1280, 800));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load login page.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}
