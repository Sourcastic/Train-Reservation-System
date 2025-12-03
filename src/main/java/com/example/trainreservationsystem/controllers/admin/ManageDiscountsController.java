package com.example.trainreservationsystem.controllers.admin;

import java.time.LocalDate;
import java.util.List;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.admin.Discount;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.repositories.RepositoryFactory;
import com.example.trainreservationsystem.repositories.admin.DiscountRepository;
import com.example.trainreservationsystem.repositories.admin.ScheduleRepository;
import com.example.trainreservationsystem.utils.shared.ui.AlertUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Controller for managing discount codes (admin only).
 */
public class ManageDiscountsController {
  @FXML
  private TableView<Discount> discountsTable;
  @FXML
  private TableColumn<Discount, String> codeCol, nameCol, typeCol, descriptionCol, statusCol;
  @FXML
  private TableColumn<Discount, Double> percentageCol, amountCol;
  @FXML
  private TableColumn<Discount, LocalDate> validFromCol, validToCol;
  @FXML
  private TextField nameField, codeField, percentageField, amountField, maxUsesField;
  @FXML
  private TextArea descriptionField;
  @FXML
  private ComboBox<String> typeCombo;
  @FXML
  private ComboBox<Schedule> scheduleCombo;
  @FXML
  private DatePicker validFromPicker, validToPicker;
  @FXML
  private CheckBox activeCheckbox;
  @FXML
  private Button saveButton, deleteButton, clearButton;
  @FXML
  private VBox formBox;

  private final DiscountRepository discountRepository = RepositoryFactory.getDiscountRepository();
  private final ScheduleRepository scheduleRepository = RepositoryFactory.getScheduleRepository();
  private Discount selectedDiscount;

  @FXML
  public void initialize() {
    setupTableColumns();
    setupForm();
    loadDiscounts();
    loadSchedules();
  }

  private void setupTableColumns() {
    codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().getType() != null ? cellData.getValue().getType().name() : ""));
    descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    percentageCol.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
    amountCol.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
    validFromCol.setCellValueFactory(new PropertyValueFactory<>("validFrom"));
    validToCol.setCellValueFactory(new PropertyValueFactory<>("validTo"));
    statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().isActive() ? "Active" : "Inactive"));

    discountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        loadDiscountToForm(newVal);
      }
    });
  }

  private void setupForm() {
    typeCombo.getItems().addAll("PROMO", "VOUCHER", "DISCOUNT_CODE");
    typeCombo.setValue("DISCOUNT_CODE");

    // Clear form when clear button is clicked
    clearButton.setOnAction(e -> clearForm());

    // Save button
    saveButton.setOnAction(e -> handleSave());

    // Delete button
    deleteButton.setOnAction(e -> handleDelete());
  }

  private void loadSchedules() {
    try {
      List<Schedule> schedules = scheduleRepository.getAllSchedules();
      scheduleCombo.getItems().clear();
      scheduleCombo.getItems().add(null); // Add "All Schedules" option
      scheduleCombo.getItems().addAll(schedules);
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load schedules: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void loadDiscounts() {
    try {
      List<Discount> discounts = discountRepository.getAllDiscounts();
      discountsTable.setItems(FXCollections.observableArrayList(discounts));
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to load discounts: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void loadDiscountToForm(Discount discount) {
    selectedDiscount = discount;
    nameField.setText(discount.getName());
    codeField.setText(discount.getCode());
    descriptionField.setText(discount.getDescription());
    typeCombo.setValue(discount.getType() != null ? discount.getType().name() : "DISCOUNT_CODE");

    percentageField.setText(String.valueOf(discount.getDiscountPercentage()));
    amountField.setText(String.valueOf(discount.getDiscountAmount()));
    maxUsesField.setText(String.valueOf(discount.getMaxUses()));

    validFromPicker.setValue(discount.getValidFrom());
    validToPicker.setValue(discount.getValidTo());
    activeCheckbox.setSelected(discount.isActive());

    // Set schedule
    if (discount.getScheduleId() != null) {
      scheduleCombo.getItems().stream()
          .filter(s -> s != null && s.getId() == discount.getScheduleId())
          .findFirst()
          .ifPresent(scheduleCombo::setValue);
    } else {
      scheduleCombo.setValue(null);
    }

    deleteButton.setDisable(false);
  }

  private void clearForm() {
    selectedDiscount = null;
    nameField.clear();
    codeField.clear();
    descriptionField.clear();
    typeCombo.setValue("DISCOUNT_CODE");
    percentageField.clear();
    amountField.clear();
    maxUsesField.clear();
    validFromPicker.setValue(null);
    validToPicker.setValue(null);
    activeCheckbox.setSelected(true);
    scheduleCombo.setValue(null);
    deleteButton.setDisable(true);
    discountsTable.getSelectionModel().clearSelection();
  }

  private void handleSave() {
    try {
      if (!validateForm()) {
        return;
      }

      Discount discount;
      if (selectedDiscount != null) {
        discount = selectedDiscount;
      } else {
        discount = new Discount();
      }

      discount.setName(nameField.getText().trim());
      discount.setCode(codeField.getText().trim().toUpperCase());
      discount.setDescription(descriptionField.getText().trim());
      discount.setType(Discount.DiscountType.valueOf(typeCombo.getValue()));

      double percentage = percentageField.getText().isEmpty() ? 0 : Double.parseDouble(percentageField.getText());
      double amount = amountField.getText().isEmpty() ? 0 : Double.parseDouble(amountField.getText());
      discount.setDiscountPercentage(percentage);
      discount.setDiscountAmount(amount);

      int maxUses = maxUsesField.getText().isEmpty() ? 0 : Integer.parseInt(maxUsesField.getText());
      discount.setMaxUses(maxUses);
      discount.setCurrentUses(selectedDiscount != null ? selectedDiscount.getCurrentUses() : 0);

      discount.setValidFrom(validFromPicker.getValue());
      discount.setValidTo(validToPicker.getValue());
      discount.setActive(activeCheckbox.isSelected());

      Schedule selectedSchedule = scheduleCombo.getValue();
      discount.setScheduleId(selectedSchedule != null ? selectedSchedule.getId() : null);

      if (selectedDiscount != null) {
        // Update existing discount
        discountRepository.updateDiscount(discount);
        AlertUtils.showSuccess("Success", "Discount updated successfully");
      } else {
        // Create new discount
        discountRepository.saveDiscount(discount);
        AlertUtils.showSuccess("Success", "Discount created successfully");
      }

      clearForm();
      loadDiscounts();
    } catch (Exception e) {
      AlertUtils.showError("Error", "Failed to save discount: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean validateForm() {
    if (nameField.getText().trim().isEmpty()) {
      AlertUtils.showWarning("Validation Error", "Name is required");
      return false;
    }
    if (codeField.getText().trim().isEmpty()) {
      AlertUtils.showWarning("Validation Error", "Code is required");
      return false;
    }
    if (percentageField.getText().isEmpty() && amountField.getText().isEmpty()) {
      AlertUtils.showWarning("Validation Error", "Either percentage or amount must be specified");
      return false;
    }
    try {
      if (!percentageField.getText().isEmpty()) {
        double pct = Double.parseDouble(percentageField.getText());
        if (pct < 0 || pct > 100) {
          AlertUtils.showWarning("Validation Error", "Percentage must be between 0 and 100");
          return false;
        }
      }
      if (!amountField.getText().isEmpty()) {
        double amt = Double.parseDouble(amountField.getText());
        if (amt < 0) {
          AlertUtils.showWarning("Validation Error", "Amount must be positive");
          return false;
        }
      }
    } catch (NumberFormatException e) {
      AlertUtils.showWarning("Validation Error", "Invalid number format");
      return false;
    }
    return true;
  }

  private void handleDelete() {
    if (selectedDiscount == null) {
      return;
    }

    if (AlertUtils.showConfirmation("Delete Discount",
        "Are you sure you want to delete discount code: " + selectedDiscount.getCode() + "?")) {
      try {
        discountRepository.deleteDiscount(selectedDiscount.getId());
        AlertUtils.showSuccess("Success", "Discount deleted successfully");
        clearForm();
        loadDiscounts();
      } catch (Exception e) {
        AlertUtils.showError("Error", "Failed to delete discount: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @FXML
  public void handleBack() {
    HomeController.getInstance().showStaffDashboard();
  }
}
