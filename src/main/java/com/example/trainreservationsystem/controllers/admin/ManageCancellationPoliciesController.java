package com.example.trainreservationsystem.controllers.admin;

import java.util.List;

import com.example.trainreservationsystem.models.admin.CancellationPolicy;
import com.example.trainreservationsystem.repositories.CancellationPolicyRepository;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for managing cancellation policies (admin only).
 */
public class ManageCancellationPoliciesController {
  @FXML
  private TableView<CancellationPolicy> policiesTable;
  @FXML
  private TableColumn<CancellationPolicy, String> nameCol, descriptionCol;
  @FXML
  private TableColumn<CancellationPolicy, Integer> hoursCol, minHoursCol;
  @FXML
  private TableColumn<CancellationPolicy, Double> refundCol;
  @FXML
  private TableColumn<CancellationPolicy, Boolean> allowCol;
  @FXML
  private TextField nameField, hoursField, minHoursField, refundField;
  @FXML
  private TextArea descriptionField;
  @FXML
  private CheckBox allowCancellationCheckbox;
  @FXML
  private Button saveButton, deleteButton, clearButton, setActiveButton;
  @FXML
  private Label activePolicyLabel;

  private final CancellationPolicyRepository policyRepository = RepositoryFactory.getCancellationPolicyRepository();
  private CancellationPolicy selectedPolicy;

  @FXML
  public void initialize() {
    setupTableColumns();
    setupForm();
    loadPolicies();
    updateActivePolicyLabel();
  }

  private void setupTableColumns() {
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    hoursCol.setCellValueFactory(new PropertyValueFactory<>("hoursBeforeDeparture"));
    minHoursCol.setCellValueFactory(new PropertyValueFactory<>("minHoursBeforeDeparture"));
    refundCol.setCellValueFactory(new PropertyValueFactory<>("refundPercentage"));
    allowCol.setCellValueFactory(new PropertyValueFactory<>("allowCancellation"));

    policiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        loadPolicyToForm(newVal);
      }
    });
  }

  private void setupForm() {
    clearButton.setOnAction(e -> clearForm());
    saveButton.setOnAction(e -> handleSave());
    deleteButton.setOnAction(e -> handleDelete());
    setActiveButton.setOnAction(e -> handleSetActive());
  }

  private void loadPolicies() {
    try {
      List<CancellationPolicy> policies = policyRepository.getAllPolicies();
      policiesTable.setItems(FXCollections.observableArrayList(policies));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load policies: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void updateActivePolicyLabel() {
    try {
      CancellationPolicy active = policyRepository.getActivePolicy();
      if (active != null && active.getId() > 0) {
        activePolicyLabel.setText("Active Policy: " + active.getName());
      } else {
        activePolicyLabel.setText("Active Policy: Default (No policy set)");
      }
    } catch (Exception e) {
      activePolicyLabel.setText("Active Policy: Error loading");
    }
  }

  private void loadPolicyToForm(CancellationPolicy policy) {
    selectedPolicy = policy;
    nameField.setText(policy.getName());
    descriptionField.setText(policy.getDescription());
    hoursField.setText(String.valueOf(policy.getHoursBeforeDeparture()));
    minHoursField.setText(String.valueOf(policy.getMinHoursBeforeDeparture()));
    refundField.setText(String.valueOf(policy.getRefundPercentage()));
    allowCancellationCheckbox.setSelected(policy.isAllowCancellation());
    deleteButton.setDisable(false);
    setActiveButton.setDisable(false);
  }

  private void clearForm() {
    selectedPolicy = null;
    nameField.clear();
    descriptionField.clear();
    hoursField.clear();
    minHoursField.clear();
    refundField.clear();
    allowCancellationCheckbox.setSelected(true);
    deleteButton.setDisable(true);
    setActiveButton.setDisable(true);
    policiesTable.getSelectionModel().clearSelection();
  }

  private void handleSave() {
    try {
      if (!validateForm()) {
        return;
      }

      CancellationPolicy policy;
      if (selectedPolicy != null) {
        policy = selectedPolicy;
      } else {
        policy = new CancellationPolicy();
      }

      policy.setName(nameField.getText().trim());
      policy.setDescription(descriptionField.getText().trim());
      policy.setHoursBeforeDeparture(Integer.parseInt(hoursField.getText()));
      policy.setMinHoursBeforeDeparture(Integer.parseInt(minHoursField.getText()));
      policy.setRefundPercentage(Double.parseDouble(refundField.getText()));
      policy.setAllowCancellation(allowCancellationCheckbox.isSelected());

      if (selectedPolicy != null) {
        policyRepository.updatePolicy(policy);
        AlertUtils.showSuccess("Success", "Policy updated successfully");
      } else {
        policyRepository.savePolicy(policy);
        AlertUtils.showSuccess("Success", "Policy created successfully");
        // Set as active if it's the first policy
        if (policiesTable.getItems().isEmpty()) {
          policyRepository.setActivePolicy(policy.getId());
        }
      }

      clearForm();
      loadPolicies();
      updateActivePolicyLabel();
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to save policy: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean validateForm() {
    if (nameField.getText().trim().isEmpty()) {
      AlertUtils.showWarning("Validation Error", "Name is required");
      return false;
    }
    try {
      int hours = Integer.parseInt(hoursField.getText());
      int minHours = Integer.parseInt(minHoursField.getText());
      double refund = Double.parseDouble(refundField.getText());

      if (hours < 0) {
        AlertUtils.showWarning("Validation Error", "Hours before departure must be positive");
        return false;
      }
      if (minHours < 0 || minHours > hours) {
        AlertUtils.showWarning("Validation Error", "Minimum hours must be between 0 and max hours");
        return false;
      }
      if (refund < 0 || refund > 100) {
        AlertUtils.showWarning("Validation Error", "Refund percentage must be between 0 and 100");
        return false;
      }
    } catch (NumberFormatException e) {
      AlertUtils.showWarning("Validation Error", "Invalid number format");
      return false;
    }
    return true;
  }

  private void handleDelete() {
    if (selectedPolicy == null) {
      return;
    }

    if (AlertUtils.showConfirmation("Delete Policy",
        "Are you sure you want to delete policy: " + selectedPolicy.getName() + "?")) {
      try {
        policyRepository.deletePolicy(selectedPolicy.getId());
        AlertUtils.showSuccess("Success", "Policy deleted successfully");
        clearForm();
        loadPolicies();
        updateActivePolicyLabel();
      } catch (Exception e) {
        AlertUtils.showError("Error", "Failed to delete policy: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private void handleSetActive() {
    if (selectedPolicy == null) {
      return;
    }

    try {
      policyRepository.setActivePolicy(selectedPolicy.getId());
      AlertUtils.showSuccess("Success", "Policy set as active");
      updateActivePolicyLabel();
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to set active policy: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  public void handleBack() {
    HomeController.getInstance().showStaffDashboard();
  }
}
