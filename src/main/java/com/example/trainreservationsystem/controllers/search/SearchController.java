package com.example.trainreservationsystem.controllers.search;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.SearchableComboBox;

import com.example.trainreservationsystem.controllers.HomeController;
import com.example.trainreservationsystem.models.Schedule;
import com.example.trainreservationsystem.services.ServiceFactory;
import com.example.trainreservationsystem.services.TrainService;
import com.example.trainreservationsystem.services.UserSession;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Controller for searching train schedules.
 * Handles: route selection, date picking, schedule display.
 */
public class SearchController {
  @FXML
  private SearchableComboBox<String> sourceCombo, destinationCombo;
  @FXML
  private DatePicker datePicker;
  @FXML
  private TableView<Schedule> resultsTable;
  @FXML
  private TableColumn<Schedule, String> sourceCol, destCol, timeCol, actionCol;
  @FXML
  private TableColumn<Schedule, Double> priceCol;
  @FXML
  private VBox emptyStateBox;

  private final TrainService trainService = ServiceFactory.getTrainService();
  private final List<String> allStations = new ArrayList<>();

  @FXML
  public void initialize() {
    initializeStations();
    setupTableColumns();
    setupTableInteractions();
    setupStationFilters();
  }

  private void initializeStations() {
    allStations.addAll(List.of("New York", "Boston", "Chicago", "St. Louis"));
    sourceCombo.setItems(FXCollections.observableArrayList(allStations));
    destinationCombo.setItems(FXCollections.observableArrayList(allStations));
  }

  private void setupTableColumns() {
    sourceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute().getSource()));
    destCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute().getDestination()));
    timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartureTime() != null
        ? cellData.getValue().getDepartureTime().toString()
        : ""));
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    actionCol.setCellValueFactory(cellData -> new SimpleStringProperty(""));
    actionCol.setCellFactory(column -> createBookButtonCell());
  }

  private javafx.scene.control.TableCell<Schedule, String> createBookButtonCell() {
    return new javafx.scene.control.TableCell<>() {
      private final Button bookBtn = new Button("Book");

      {
        bookBtn.getStyleClass().add("book-button");
        bookBtn.setOnAction(e -> {
          Schedule schedule = getTableView().getItems().get(getIndex());
          handleBook(schedule);
        });
      }

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : bookBtn);
      }
    };
  }

  private void setupTableInteractions() {
    resultsTable.setRowFactory(tv -> {
      TableRow<Schedule> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (!row.isEmpty() && event.getClickCount() == 2) {
          handleBook(row.getItem());
        }
      });
      return row;
    });

    if (emptyStateBox != null) {
      emptyStateBox.visibleProperty().bind(Bindings.isEmpty(resultsTable.getItems()));
      emptyStateBox.managedProperty().bind(emptyStateBox.visibleProperty());
    }
  }

  private void setupStationFilters() {
    sourceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      filterDestinationStations(newVal);
    });

    destinationCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      filterSourceStations(newVal);
    });
  }

  private void filterDestinationStations(String selectedSource) {
    if (selectedSource == null) {
      destinationCombo.setItems(FXCollections.observableArrayList(allStations));
    } else {
      List<String> filtered = allStations.stream()
          .filter(city -> !city.equals(selectedSource))
          .toList();
      destinationCombo.setItems(FXCollections.observableArrayList(filtered));
      if (selectedSource.equals(destinationCombo.getValue())) {
        destinationCombo.setValue(null);
      }
    }
  }

  private void filterSourceStations(String selectedDest) {
    if (selectedDest == null) {
      sourceCombo.setItems(FXCollections.observableArrayList(allStations));
    } else {
      List<String> filtered = allStations.stream()
          .filter(city -> !city.equals(selectedDest))
          .toList();
      sourceCombo.setItems(FXCollections.observableArrayList(filtered));
      if (selectedDest.equals(sourceCombo.getValue())) {
        sourceCombo.setValue(null);
      }
    }
  }

  @FXML
  private void handleSearch() {
    String source = sourceCombo.getValue();
    String dest = destinationCombo.getValue();
    LocalDate date = datePicker.getValue();

    if (source != null && dest != null && date != null) {
      List<Schedule> schedules = trainService.searchSchedules(source, dest, date);
      resultsTable.setItems(FXCollections.observableArrayList(schedules));
    }
  }

  private void handleBook(Schedule schedule) {
    UserSession.getInstance().setSelectedSchedule(schedule);
    HomeController.getInstance().loadView("/com/example/trainreservationsystem/booking/booking-view.fxml");
  }
}
