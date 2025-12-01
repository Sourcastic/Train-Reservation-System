package com.example.trainreservationsystem.controllers;

import java.time.LocalDate;
import java.util.List;

import org.controlsfx.control.SearchableComboBox;

import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.TrainService;
import com.example.trainreservationsystem.services.UserSession;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SearchController {

  @FXML
  private SearchableComboBox<String> sourceCombo;
  @FXML
  private SearchableComboBox<String> destinationCombo;
  @FXML
  private DatePicker datePicker;
  @FXML
  private TableView<Schedule> resultsTable;
  @FXML
  private TableColumn<Schedule, String> sourceCol;
  @FXML
  private TableColumn<Schedule, String> destCol;
  @FXML
  private TableColumn<Schedule, String> timeCol;
  @FXML
  private TableColumn<Schedule, Double> priceCol;

  private final TrainService trainService = ServiceFactory.getTrainService();

  @FXML
  public void initialize() {
    sourceCombo.setItems(FXCollections.observableArrayList("New York", "Boston", "Chicago"));
    destinationCombo.setItems(FXCollections.observableArrayList("New York", "Boston", "St. Louis"));

    if (sourceCol != null) {
      sourceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute().getSource()));
      destCol
          .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute().getDestination()));
      timeCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
      priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    if (resultsTable != null) {
      resultsTable.setRowFactory(tv -> {
        TableRow<Schedule> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
          if (!row.isEmpty() && event.getClickCount() == 2) {
            Schedule clickedRow = row.getItem();
            handleBook(clickedRow);
          }
        });
        return row;
      });
    }
  }

  @FXML
  private void handleSearch() {
    String source = sourceCombo.getValue();
    String dest = destinationCombo.getValue();
    LocalDate date = datePicker.getValue();

    if (source != null && dest != null && date != null) {
      List<Schedule> schedules = trainService.searchSchedules(source, dest, date);
      if (resultsTable != null) {
        resultsTable.setItems(FXCollections.observableArrayList(schedules));
      }
      System.out.println("Found " + schedules.size() + " schedules");
    }
  }

  private void handleBook(Schedule schedule) {
    UserSession.getInstance().setSelectedSchedule(schedule);
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/booking-view.fxml");
  }
}
