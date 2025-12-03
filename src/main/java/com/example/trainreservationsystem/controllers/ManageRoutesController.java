package com.example.trainreservationsystem.controllers;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.RouteSegment;
import com.example.trainreservationsystem.models.Stop;
import com.example.trainreservationsystem.repositories.RouteRepository;
import com.example.trainreservationsystem.repositories.StopRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class ManageRoutesController {

    @FXML
    private TableView<Route> routesTable;
    @FXML
    private TableColumn<Route, Integer> idColumn;
    @FXML
    private TableColumn<Route, String> sourceColumn;
    @FXML
    private TableColumn<Route, String> destinationColumn;
    @FXML
    private TableColumn<Route, Double> distanceColumn;
    @FXML
    private TableColumn<Route, Double> priceColumn;
    @FXML
    private TableColumn<Route, Void> actionColumn;

    @FXML
    private javafx.scene.layout.VBox segmentsContainer;
    @FXML
    private Button addRouteButton;
    @FXML
    private Label messageLabel;

    private final RouteRepository routeRepository = new RouteRepository();
    private final StopRepository stopRepository = new StopRepository();
    private ObservableList<Route> routesList = FXCollections.observableArrayList();
    private List<Stop> allStops = new ArrayList<>();
    private List<SegmentRow> segmentRows = new ArrayList<>();
    private boolean routesLoaded = false; // Cache flag

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        distanceColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().totalDistance())
                        .asObject());
        priceColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().totalPrice())
                        .asObject());

        // Set up action column with delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteButton.setOnAction(event -> {
                    Route route = getTableView().getItems().get(getIndex());
                    handleDeleteRoute(route);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        loadStops();
        loadRoutes();
        addInitialSegmentRow();
    }

    private void loadStops() {
        try {
            System.out.println("[DEBUG] ManageRoutesController: Loading stops...");
            allStops = stopRepository.getAllStops();
            System.out.println("[DEBUG] ManageRoutesController: Loaded " + allStops.size() + " stops");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error loading stops: " + e.getMessage(), false);
        }
    }

    private void loadRoutes() {
        if (routesLoaded) {
            System.out.println("[DEBUG] ManageRoutesController: Routes already loaded, skipping.");
            return;
        }

        new Thread(() -> {
            try {
                System.out.println("[DEBUG] ManageRoutesController: Loading routes in background...");
                List<Route> routes = routeRepository.getAllRoutes();
                javafx.application.Platform.runLater(() -> {
                    routesList.clear();
                    routesList.addAll(routes);
                    routesTable.setItems(routesList);
                    routesLoaded = true;
                    System.out.println("[DEBUG] ManageRoutesController: Loaded " + routes.size() + " routes");
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform
                        .runLater(() -> showMessage("Error loading routes: " + e.getMessage(), false));
            }
        }).start();
    }

    private void addInitialSegmentRow() {
        addSegmentRow(true);
    }

    private void addSegmentRow(boolean isFirst) {
        System.out.println("[DEBUG] ManageRoutesController: addSegmentRow called, allStops size: " + allStops.size());
        HBox row = new HBox(10);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: #ECF0F1; -fx-background-radius: 5; -fx-padding: 10;");

        ComboBox<Stop> fromStopCombo = new ComboBox<>();
        fromStopCombo.setItems(FXCollections.observableArrayList(allStops));
        fromStopCombo.setPromptText("From Stop");
        fromStopCombo.setPrefWidth(120);
        fromStopCombo.setConverter(new javafx.util.StringConverter<Stop>() {
            @Override
            public String toString(Stop stop) {
                return stop != null ? stop.getName() : "";
            }

            @Override
            public Stop fromString(String string) {
                return null;
            }
        });

        ComboBox<Stop> toStopCombo = new ComboBox<>();
        toStopCombo.setItems(FXCollections.observableArrayList(allStops));
        toStopCombo.setPromptText("To Stop");
        toStopCombo.setPrefWidth(120);
        toStopCombo.setConverter(new javafx.util.StringConverter<Stop>() {
            @Override
            public String toString(Stop stop) {
                return stop != null ? stop.getName() : "";
            }

            @Override
            public Stop fromString(String string) {
                return null;
            }
        });

        TextField distanceField = new TextField();
        distanceField.setPromptText("Distance");
        distanceField.setPrefWidth(80);

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefWidth(80);

        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> addSegmentRow(false));

        row.getChildren().addAll(fromStopCombo, toStopCombo, distanceField, priceField, addButton);

        if (!isFirst) {
            Button removeButton = new Button("-");
            removeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
            removeButton.setOnAction(e -> {
                segmentsContainer.getChildren().remove(row);
                segmentRows.removeIf(sr -> sr.row == row);
            });
            row.getChildren().add(removeButton);
        }

        segmentRows.add(new SegmentRow(row, fromStopCombo, toStopCombo, distanceField, priceField));
        segmentsContainer.getChildren().add(row);
    }

    @FXML
    private void handleAddRoute() {
        if (segmentRows.isEmpty()) {
            showMessage("Please add at least one route segment", false);
            return;
        }

        try {
            List<RouteSegment> segments = new ArrayList<>();
            String source = null;
            String destination = null;

            for (int i = 0; i < segmentRows.size(); i++) {
                SegmentRow sr = segmentRows.get(i);

                Stop fromStop = sr.fromStopCombo.getValue();
                Stop toStop = sr.toStopCombo.getValue();
                String distanceStr = sr.distanceField.getText().trim();
                String priceStr = sr.priceField.getText().trim();

                if (fromStop == null || toStop == null || distanceStr.isEmpty() || priceStr.isEmpty()) {
                    showMessage("Please fill in all fields for segment " + (i + 1), false);
                    return;
                }

                // Validate segment connectivity
                if (i > 0) {
                    Stop previousToStop = segmentRows.get(i - 1).toStopCombo.getValue();
                    if (fromStop.getId() != previousToStop.getId()) {
                        showMessage("Segment " + (i + 1) + " must start where segment " + i + " ends ("
                                + previousToStop.getName() + ")", false);
                        return;
                    }
                }

                double distance = Double.parseDouble(distanceStr);
                double price = Double.parseDouble(priceStr);

                if (i == 0) {
                    source = fromStop.getName();
                }
                if (i == segmentRows.size() - 1) {
                    destination = toStop.getName();
                }

                RouteSegment segment = new RouteSegment();
                segment.setFromStop(fromStop);
                segment.setToStop(toStop);
                segment.setDistance(distance);
                segment.setPrice(price);
                segments.add(segment);
            }

            // Create route with auto-generated name
            Route newRoute = new Route();
            newRoute.setName(source + " to " + destination);
            newRoute.setSource(source);
            newRoute.setDestination(destination);

            // Add route to database
            Route savedRoute = routeRepository.addRoute(newRoute);

            // Add segments (checking for duplicates)
            for (RouteSegment segment : segments) {
                // Check if this exact segment already exists
                RouteSegment existingSegment = routeRepository.findExistingSegment(
                        segment.getFromStop().getId(),
                        segment.getToStop().getId(),
                        segment.getDistance(),
                        segment.getPrice());

                if (existingSegment != null) {
                    // Use existing segment ID, just link it to this route
                    System.out.println("[DEBUG] Using existing segment ID: " + existingSegment.getId());
                    routeRepository.linkSegmentToRoute(existingSegment.getId(), savedRoute.getId());
                } else {
                    // Add new segment
                    routeRepository.addRouteSegment(segment, savedRoute.getId());
                }
            }

            showMessage("Route added successfully!", true);
            clearSegmentRows();
            addInitialSegmentRow();
            routesLoaded = false; // Force reload
            loadRoutes();
        } catch (NumberFormatException e) {
            showMessage("Invalid distance or price. Please enter valid numbers.", false);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error adding route: " + e.getMessage(), false);
        }
    }

    private void clearSegmentRows() {
        segmentRows.clear();
        segmentsContainer.getChildren().clear();
    }

    private void handleDeleteRoute(Route route) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Route");
        confirmAlert.setContentText("Are you sure you want to delete route from " + route.getSource() + " to "
                + route.getDestination() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    routeRepository.deleteRoute(route.getId());
                    showMessage("Route deleted successfully!", true);
                    loadRoutes();
                } catch (Exception e) {
                    showMessage("Error deleting route: " + e.getMessage(), false);
                }
            }
        });
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (success ? "#27AE60" : "#E74C3C") + ";");
    }

    // Helper class to hold segment row components
    private static class SegmentRow {
        HBox row;
        ComboBox<Stop> fromStopCombo;
        ComboBox<Stop> toStopCombo;
        TextField distanceField;
        TextField priceField;

        SegmentRow(HBox row, ComboBox<Stop> fromStopCombo, ComboBox<Stop> toStopCombo,
                TextField distanceField, TextField priceField) {
            this.row = row;
            this.fromStopCombo = fromStopCombo;
            this.toStopCombo = toStopCombo;
            this.distanceField = distanceField;
            this.priceField = priceField;
        }
    }
}
