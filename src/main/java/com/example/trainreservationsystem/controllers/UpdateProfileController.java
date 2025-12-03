package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.services.AuthService;
import com.example.trainreservationsystem.services.UserSession;
import com.example.trainreservationsystem.utils.ui.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button confirmButton;

    private final AuthService authService = AuthService.getInstance();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    public void initialize() {
        User currentUser = userSession.getCurrentUser();
        if (currentUser != null) {
            nameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNo());
        }
    }

    @FXML
    private void handleConfirm() {
        // Clear previous messages
        errorLabel.setText("");
        successLabel.setText("");

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        try {
            User currentUser = userSession.getCurrentUser();
            currentUser.setUsername(name);
            currentUser.setEmail(email);
            currentUser.setPhoneNo(phone);

            authService.updateUserProfile(currentUser);

            successLabel.setText("Profile updated successfully!");

            // Update session user to reflect changes (though we modified the object
            // reference directly above,
            // good to be explicit if session logic changes)
            // In this case, currentUser is a reference to the object in session, so it's
            // already updated.

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("An error occurred while updating profile.");
        }
    }
}
