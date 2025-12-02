package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ManageUsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;

    @FXML private Button btnEdit;
    @FXML private Button btnDeactivate;
    @FXML private Button btnDelete;

    private final UserService userService = new UserService();
    private ObservableList<User> users;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colUsername.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        loadUsers();
    }

    private void loadUsers() {
        users = FXCollections.observableArrayList(userService.getAllUsers());
        userTable.setItems(users);
    }

    @FXML
    public void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a user first!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getEmail());
        dialog.setHeaderText("Edit User Email");
        dialog.setContentText("New Email:");

        dialog.showAndWait().ifPresent(newEmail -> {
            selected.setEmail(newEmail);
            if (userService.updateUser(selected)) {
                showAlert("User updated successfully!");
                loadUsers();
            }
        });
    }

    @FXML
    public void handleDeactivate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a user first!");
            return;
        }

        if (userService.deactivateUser(selected.getId())) {
            showAlert("User deactivated!");
            loadUsers();
        }
    }

    @FXML
    public void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a user first!");
            return;
        }

        if (userService.deleteUser(selected.getId())) {
            showAlert("User deleted!");
            loadUsers();
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }
}
