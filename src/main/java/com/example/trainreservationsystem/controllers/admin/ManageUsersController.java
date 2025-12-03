package com.example.trainreservationsystem.controllers.admin;

import java.util.List;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.shared.User;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.shared.UserRepository;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * Controller for managing users (admin only).
 * Allows admin to view and delete users.
 */
public class ManageUsersController {
  @FXML
  private TableView<User> usersTable;
  @FXML
  private TableColumn<User, Integer> colUserId;
  @FXML
  private TableColumn<User, String> colUsername;
  @FXML
  private TableColumn<User, String> colEmail;
  @FXML
  private TableColumn<User, String> colPhone;
  @FXML
  private TableColumn<User, String> colUserType;
  @FXML
  private TableColumn<User, Integer> colLoyaltyPoints;
  @FXML
  private TableColumn<User, Void> colActions;

  private final UserRepository userRepository = RepositoryFactory.getUserRepository();

  @FXML
  public void initialize() {
    setupTableColumns();
    loadUsers();
  }

  private void setupTableColumns() {
    colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNo"));
    colUserType.setCellValueFactory(new PropertyValueFactory<>("userType"));
    colLoyaltyPoints.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));

    // Actions column with promote and delete buttons
    colActions.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
      @Override
      public TableCell<User, Void> call(TableColumn<User, Void> param) {
        return new TableCell<User, Void>() {
          private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5);
          private final Button promoteButton = new Button("Promote to Staff");
          private final Button deleteButton = new Button("Delete");

          {
            promoteButton.setStyle(
                "-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 11; -fx-background-radius: 5;");
            deleteButton.setStyle(
                "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11; -fx-background-radius: 5;");

            promoteButton.setOnAction(event -> {
              User user = getTableView().getItems().get(getIndex());
              if (user != null) {
                handlePromoteUser(user);
              }
            });

            deleteButton.setOnAction(event -> {
              User user = getTableView().getItems().get(getIndex());
              if (user != null) {
                handleDeleteUser(user);
              }
            });

            buttonBox.getChildren().addAll(promoteButton, deleteButton);
          }

          @Override
          protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setGraphic(null);
            } else {
              User user = getTableView().getItems().get(getIndex());
              // Only show promote button for CUSTOMER users
              if (user != null && "CUSTOMER".equalsIgnoreCase(user.getUserType())) {
                promoteButton.setVisible(true);
                promoteButton.setManaged(true);
              } else {
                promoteButton.setVisible(false);
                promoteButton.setManaged(false);
              }
              setGraphic(buttonBox);
            }
          }
        };
      }
    });
  }

  private void loadUsers() {
    try {
      List<User> users = userRepository.getAllUsers();
      usersTable.setItems(FXCollections.observableArrayList(users));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load users: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void handlePromoteUser(User user) {
    if (user == null) {
      return;
    }

    if (!"CUSTOMER".equalsIgnoreCase(user.getUserType())) {
      AlertUtils.showWarning("Invalid Action", "Only CUSTOMER users can be promoted to STAFF.");
      return;
    }

    String message = String.format(
        "Are you sure you want to promote this user to Staff?\n\n" +
            "User ID: %d\n" +
            "Username: %s\n" +
            "Email: %s\n\n" +
            "This will grant them staff privileges.",
        user.getId(),
        user.getUsername(),
        user.getEmail());

    if (AlertUtils.showConfirmation("Promote to Staff", message)) {
      try {
        boolean updated = userRepository.updateUserType(user.getId(), "STAFF");
        if (updated) {
          AlertUtils.showSuccess("Success", "User has been promoted to Staff.");
          loadUsers(); // Refresh the table
        } else {
          AlertUtils.showError("Error", "Failed to promote user.");
        }
      } catch (Exception e) {
        AlertUtils.showError("Error", "Failed to promote user: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private void handleDeleteUser(User user) {
    if (user == null) {
      return;
    }

    String message = String.format(
        "Are you sure you want to delete user?\n\n" +
            "User ID: %d\n" +
            "Username: %s\n" +
            "Email: %s\n\n" +
            "This action cannot be undone.",
        user.getId(),
        user.getUsername(),
        user.getEmail());

    if (AlertUtils.showConfirmation("Delete User", message)) {
      try {
        boolean deleted = userRepository.deleteUser(user.getId());
        if (deleted) {
          AlertUtils.showSuccess("Success", "User has been deleted.");
          loadUsers();
        } else {
          AlertUtils.showError("Error", "Failed to delete user.");
        }
      } catch (Exception e) {
        AlertUtils.showError("Error", "Failed to delete user: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @FXML
  public void handleRefresh() {
    loadUsers();
  }

  @FXML
  public void handleBack() {
    HomeController.getInstance().showStaffDashboard();
  }
}
