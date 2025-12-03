package com.example.trainreservationsystem.controllers.member.search;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.controlsfx.control.SearchableComboBox;

import com.example.trainreservationsystem.controllers.shared.HomeController;
import com.example.trainreservationsystem.models.admin.Route;
import com.example.trainreservationsystem.models.admin.Schedule;
import com.example.trainreservationsystem.models.member.BookingClass;
import com.example.trainreservationsystem.services.admin.RouteService;
import com.example.trainreservationsystem.services.admin.TrainService;
import com.example.trainreservationsystem.services.member.booking.BookingService;
import com.example.trainreservationsystem.services.shared.ServiceFactory;
import com.example.trainreservationsystem.services.shared.UserSession;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
  private VBox resultsArea;
  @FXML
  private VBox resultsContainer;
  @FXML
  private StackPane emptyStatePane;
  @FXML
  private Label searchSummaryLabel;
  @FXML
  private ComboBox<String> sortCombo;

  private final TrainService trainService = ServiceFactory.getTrainService();
  private final BookingService bookingService = ServiceFactory.getBookingService();
  private final RouteService routeService = new RouteService();
  private final List<String> allStations = new ArrayList<>();
  private List<Schedule> currentSchedules = new ArrayList<>();

  @FXML
  public void initialize() {
    setupStationFilters();
    setupSortCombo();
    emptyStatePane.managedProperty().bind(emptyStatePane.visibleProperty());
    if (resultsArea != null) {
      resultsArea.managedProperty().bind(resultsArea.visibleProperty());
    }
    // Load stations asynchronously to avoid blocking UI
    initializeStationsAsync();
  }

  private void initializeStationsAsync() {
    // Show loading state
    sourceCombo.setItems(FXCollections.observableArrayList("Loading..."));
    destinationCombo.setItems(FXCollections.observableArrayList("Loading..."));
    sourceCombo.setDisable(true);
    destinationCombo.setDisable(true);

    // Load stations in background thread
    Thread loadStationsThread = new Thread(() -> {
      try {
        // Load stations from routes in database
        List<Route> routes = routeService.getAllRoutes();
        Set<String> uniqueStations = new HashSet<>();

        for (Route route : routes) {
          if (route.getSource() != null && !route.getSource().isEmpty()) {
            uniqueStations.add(route.getSource());
          }
          if (route.getDestination() != null && !route.getDestination().isEmpty()) {
            uniqueStations.add(route.getDestination());
          }
        }

        List<String> sortedStations = new ArrayList<>(uniqueStations);
        sortedStations.sort(String.CASE_INSENSITIVE_ORDER);

        // Update UI on JavaFX thread
        Platform.runLater(() -> {
          allStations.clear();
          allStations.addAll(sortedStations);

          sourceCombo.setItems(FXCollections.observableArrayList(allStations));
          destinationCombo.setItems(FXCollections.observableArrayList(allStations));
          sourceCombo.setDisable(false);
          destinationCombo.setDisable(false);
        });
      } catch (Exception e) {
        System.err.println("Error loading stations: " + e.getMessage());
        e.printStackTrace();
        // Update UI on JavaFX thread with error state
        Platform.runLater(() -> {
          allStations.clear();
          sourceCombo.setItems(FXCollections.observableArrayList());
          destinationCombo.setItems(FXCollections.observableArrayList());
          sourceCombo.setDisable(false);
          destinationCombo.setDisable(false);
        });
      }
    });
    loadStationsThread.setDaemon(true);
    loadStationsThread.start();
  }

  private void setupSortCombo() {
    sortCombo.setItems(
        FXCollections.observableArrayList("Recommended", "Departure Time", "Arrival Time", "Duration", "Price"));
    sortCombo.setValue("Recommended");
    sortCombo.setOnAction(e -> sortAndDisplayResults());
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
      // Use simple loop instead of stream - easier to understand and same performance
      List<String> filtered = new ArrayList<>();
      for (String city : allStations) {
        if (!city.equals(selectedSource)) {
          filtered.add(city);
        }
      }
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
      // Use simple loop instead of stream - easier to understand and same performance
      List<String> filtered = new ArrayList<>();
      for (String city : allStations) {
        if (!city.equals(selectedDest)) {
          filtered.add(city);
        }
      }
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
      currentSchedules = trainService.searchSchedules(source, dest, date);
      displayResults();
    }
  }

  private void displayResults() {
    resultsContainer.getChildren().clear();

    if (currentSchedules.isEmpty()) {
      resultsArea.setVisible(true);
      emptyStatePane.setVisible(true);
      searchSummaryLabel.setText("");
      return;
    }

    resultsArea.setVisible(true);
    emptyStatePane.setVisible(false);
    searchSummaryLabel.setText("We have found " + currentSchedules.size() + " trains on or near this route");

    sortAndDisplayResults();
  }

  private void sortAndDisplayResults() {
    if (currentSchedules.isEmpty())
      return;

    List<Schedule> sorted = new ArrayList<>(currentSchedules);
    String sortBy = sortCombo.getValue();

    // Use Collections.sort with Comparator - clearer than lambda for beginners
    if ("Departure Time".equals(sortBy)) {
      Collections.sort(sorted, new Comparator<Schedule>() {
        @Override
        public int compare(Schedule a, Schedule b) {
          LocalTime timeA = a.getDepartureTime() != null ? a.getDepartureTime() : LocalTime.MIN;
          LocalTime timeB = b.getDepartureTime() != null ? b.getDepartureTime() : LocalTime.MIN;
          return timeA.compareTo(timeB);
        }
      });
    } else if ("Arrival Time".equals(sortBy)) {
      Collections.sort(sorted, new Comparator<Schedule>() {
        @Override
        public int compare(Schedule a, Schedule b) {
          LocalTime timeA = a.getArrivalTime() != null ? a.getArrivalTime() : LocalTime.MAX;
          LocalTime timeB = b.getArrivalTime() != null ? b.getArrivalTime() : LocalTime.MAX;
          return timeA.compareTo(timeB);
        }
      });
    } else if ("Price".equals(sortBy)) {
      Collections.sort(sorted, new Comparator<Schedule>() {
        @Override
        public int compare(Schedule a, Schedule b) {
          return Double.compare(a.getPrice(), b.getPrice());
        }
      });
    }

    resultsContainer.getChildren().clear();
    for (Schedule schedule : sorted) {
      resultsContainer.getChildren().add(createTrainCard(schedule));
    }
  }

  private VBox createTrainCard(Schedule schedule) {
    VBox card = new VBox();
    card.getStyleClass().add("train-card");
    card.setSpacing(16);
    card.setPadding(new Insets(20));

    // Train Header
    HBox header = new HBox();
    header.setSpacing(12);
    header.setAlignment(Pos.CENTER_LEFT);

    Label trainName = new Label(
        schedule.getRoute().getName() != null ? schedule.getRoute().getName() : "Train " + schedule.getId());
    trainName.getStyleClass().add("train-name");

    header.getChildren().add(trainName);
    header.getChildren().add(new Label("•"));

    // Get days of week from schedule
    String daysOfWeekStr = formatDaysOfWeek(schedule.getDaysOfWeek());
    header.getChildren().add(new Label("Runs on: " + daysOfWeekStr));

    // Main Content
    HBox mainContent = new HBox();
    mainContent.setSpacing(24);
    mainContent.setAlignment(Pos.CENTER_LEFT);

    // Departure Section
    VBox departureBox = new VBox(4);
    departureBox.setAlignment(Pos.CENTER_LEFT);
    Label depTime = new Label(formatTime(schedule.getDepartureTime()));
    depTime.getStyleClass().add("time-label");
    Label depStation = new Label(
        getStationCode(schedule.getRoute().getSource()) + " - " + schedule.getRoute().getSource().toUpperCase());
    depStation.getStyleClass().add("station-label");
    departureBox.getChildren().addAll(depTime, depStation);

    // Journey Info
    VBox journeyBox = new VBox(4);
    journeyBox.setAlignment(Pos.CENTER);
    journeyBox.setPrefWidth(200);
    String duration = calculateDuration(schedule.getDepartureTime(), schedule.getArrivalTime());
    Label durationLabel = new Label(duration);
    durationLabel.getStyleClass().add("journey-duration");

    // Calculate halts and distance from route segments
    Route route = schedule.getRoute();
    int halts = route != null && route.getSegments() != null ? route.getSegments().size() : 0;
    double distance = route != null ? route.totalDistance() : 0.0;
    String journeyInfoText = halts + " halts • " + String.format("%.0f", distance) + " kms";
    Label journeyInfo = new Label(journeyInfoText);
    journeyInfo.getStyleClass().add("journey-info");
    journeyBox.getChildren().addAll(durationLabel, journeyInfo);

    // Arrival Section
    VBox arrivalBox = new VBox(4);
    arrivalBox.setAlignment(Pos.CENTER_LEFT);
    Label arrTime = new Label(formatTime(schedule.getArrivalTime()));
    arrTime.getStyleClass().add("time-label");
    Label arrStation = new Label(getStationCode(schedule.getRoute().getDestination()) + " - "
        + schedule.getRoute().getDestination().toUpperCase());
    arrStation.getStyleClass().add("station-label");
    arrivalBox.getChildren().addAll(arrTime, arrStation);

    mainContent.getChildren().addAll(departureBox, journeyBox, arrivalBox);

    // Booking Classes Section - Dynamic with carousel
    VBox bookingClassesContainer = createBookingClassesSection(schedule);
    card.getChildren().addAll(header, mainContent, bookingClassesContainer);

    return card;
  }

  private VBox createBookingClassesSection(Schedule schedule) {
    VBox container = new VBox(8);

    // Get dynamic booking classes
    List<BookingClass> classes = getBookingClasses(schedule);

    if (classes.isEmpty()) {
      return container;
    }

    // Create carousel if more than 3 classes, otherwise show all
    if (classes.size() > 3) {
      container.getChildren().add(createBookingClassesCarousel(classes, schedule));
    } else {
      HBox classesBox = new HBox(12);
      classesBox.setAlignment(Pos.CENTER_LEFT);
      for (BookingClass bookingClass : classes) {
        classesBox.getChildren().add(createBookingClassCard(bookingClass, schedule));
      }
      container.getChildren().add(classesBox);
    }

    return container;
  }

  private VBox createBookingClassesCarousel(List<BookingClass> classes, Schedule schedule) {
    VBox carousel = new VBox(8);

    HBox carouselControls = new HBox(8);
    carouselControls.setAlignment(Pos.CENTER);

    Button prevButton = new Button("◀");
    prevButton.getStyleClass().add("carousel-button");
    prevButton.setPrefWidth(40);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setFitToHeight(true);
    scrollPane.setPannable(true);
    scrollPane.setStyle("-fx-background-color: transparent;");

    HBox classesBox = new HBox(12);
    classesBox.setAlignment(Pos.CENTER_LEFT);
    classesBox.setPadding(new Insets(8));

    for (BookingClass bookingClass : classes) {
      classesBox.getChildren().add(createBookingClassCard(bookingClass, schedule));
    }

    scrollPane.setContent(classesBox);
    scrollPane.setPrefViewportWidth(500);

    Button nextButton = new Button("▶");
    nextButton.getStyleClass().add("carousel-button");
    nextButton.setPrefWidth(40);

    // Carousel navigation
    prevButton.setOnAction(e -> {
      double currentHValue = scrollPane.getHvalue();
      scrollPane.setHvalue(Math.max(0, currentHValue - 0.3));
    });

    nextButton.setOnAction(e -> {
      double currentHValue = scrollPane.getHvalue();
      scrollPane.setHvalue(Math.min(1, currentHValue + 0.3));
    });

    carouselControls.getChildren().addAll(prevButton, scrollPane, nextButton);
    carousel.getChildren().add(carouselControls);

    return carousel;
  }

  private List<BookingClass> getBookingClasses(Schedule schedule) {
    List<BookingClass> classes = new ArrayList<>();

    // Get occupied seats to calculate availability
    List<Integer> occupiedSeats = bookingService.getOccupiedSeats(schedule.getId());

    // Define class ranges (SL: 1-20, 3A: 21-40, 2A: 41-60)
    classes.add(createBookingClass("SL", "Sleeper Class", 0.6, 1, 20, occupiedSeats));
    classes.add(createBookingClass("3A", "AC 3 Tier", 1.0, 21, 40, occupiedSeats));
    classes.add(createBookingClass("2A", "AC 2 Tier", 1.5, 41, 60, occupiedSeats));

    // Add more classes dynamically if needed (e.g., based on schedule capacity)
    if (schedule.getCapacity() > 60) {
      classes.add(createBookingClass("1A", "AC First Class", 2.0, 61, 80, occupiedSeats));
    }

    return classes;
  }

  private BookingClass createBookingClass(String code, String name, double multiplier,
      int seatStart, int seatEnd, List<Integer> occupiedSeats) {
    int totalSeats = seatEnd - seatStart + 1;

    // Use simple loop instead of stream - easier to understand, same O(n)
    // complexity
    int occupiedCount = 0;
    for (Integer seat : occupiedSeats) {
      if (seat >= seatStart && seat <= seatEnd) {
        occupiedCount++;
      }
    }

    int availableSeats = totalSeats - occupiedCount;
    return new BookingClass(code, name, multiplier, seatStart, seatEnd, availableSeats);
  }

  private VBox createBookingClassCard(BookingClass bookingClass, Schedule schedule) {
    VBox classCard = new VBox(8);
    classCard.getStyleClass().add("booking-class-card");
    classCard.setPadding(new Insets(12));
    classCard.setPrefWidth(140);
    classCard.setMinWidth(140);
    classCard.setAlignment(Pos.CENTER);

    Label classLabel = new Label(bookingClass.getCode() + " (" + bookingClass.getName() + ")");
    classLabel.getStyleClass().add("class-label");

    String availability = bookingClass.getAvailableSeats() > 0
        ? bookingClass.getAvailableSeats() + " Available"
        : "Not Available";
    Label availabilityLabel = new Label(availability);
    if (bookingClass.getAvailableSeats() > 0) {
      availabilityLabel.getStyleClass().add("availability-available");
    } else {
      availabilityLabel.getStyleClass().add("availability-unavailable");
    }

    double price = schedule.getPrice() * bookingClass.getPriceMultiplier();
    Label priceLabel = new Label("PKR " + String.format("%.0f", price));
    priceLabel.getStyleClass().add("price-label");

    Button bookButton = new Button("Book Now");
    bookButton.getStyleClass().add("book-now-button");
    bookButton.setPrefWidth(Double.MAX_VALUE);
    bookButton.setDisable(bookingClass.getAvailableSeats() <= 0);
    bookButton.setOnAction(e -> handleBook(schedule, bookingClass));

    classCard.getChildren().addAll(classLabel, availabilityLabel, priceLabel, bookButton);

    return classCard;
  }

  private String formatTime(LocalTime time) {
    if (time == null)
      return "--:--";
    return time.format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  private String getStationCode(String stationName) {
    // Generate station code from name (first 4 uppercase letters)
    if (stationName == null || stationName.length() < 4)
      return "----";
    return stationName.substring(0, Math.min(4, stationName.length())).toUpperCase();
  }

  private String calculateDuration(LocalTime departure, LocalTime arrival) {
    if (departure == null || arrival == null)
      return "-- h -- m";

    Duration duration;
    if (arrival.isBefore(departure) || arrival.equals(departure)) {
      // Next day arrival
      duration = Duration.between(departure, arrival.plusHours(24));
    } else {
      duration = Duration.between(departure, arrival);
    }

    long hours = duration.toHours();
    long minutes = duration.toMinutes() % 60;
    return hours + " h " + minutes + " m";
  }

  private String formatDaysOfWeek(List<Schedule.DayOfWeek> daysOfWeek) {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      return "N/A";
    }

    // Map to single letter abbreviations
    StringBuilder sb = new StringBuilder();
    for (Schedule.DayOfWeek day : daysOfWeek) {
      switch (day) {
        case MONDAY:
          sb.append("M ");
          break;
        case TUESDAY:
          sb.append("T ");
          break;
        case WEDNESDAY:
          sb.append("W ");
          break;
        case THURSDAY:
          sb.append("T ");
          break;
        case FRIDAY:
          sb.append("F ");
          break;
        case SATURDAY:
          sb.append("S ");
          break;
        case SUNDAY:
          sb.append("S ");
          break;
      }
    }
    return sb.toString().trim();
  }

  private void handleBook(Schedule schedule, BookingClass bookingClass) {
    UserSession.getInstance().setSelectedSchedule(schedule);
    UserSession.getInstance().setSelectedClass(bookingClass.getCode(), bookingClass.getPriceMultiplier());

    // Find first available seat in this class and preselect it
    List<Integer> occupiedSeats = bookingService.getOccupiedSeats(schedule.getId());
    Integer firstAvailableSeat = findFirstAvailableSeat(bookingClass, occupiedSeats);
    if (firstAvailableSeat != null) {
      UserSession.getInstance().setPreselectedSeat(firstAvailableSeat);
    }

    HomeController.getInstance().loadView("/com/example/trainreservationsystem/member/booking/booking-view.fxml");
  }

  private Integer findFirstAvailableSeat(BookingClass bookingClass, List<Integer> occupiedSeats) {
    // Convert List to HashSet for O(1) lookup instead of O(n) - better time
    // complexity
    Set<Integer> occupiedSet = new HashSet<>(occupiedSeats);

    // Linear search through seat range - O(n) where n is seat range size
    for (int seat = bookingClass.getSeatStart(); seat <= bookingClass.getSeatEnd(); seat++) {
      if (!occupiedSet.contains(seat)) {
        return seat;
      }
    }
    return null;
  }
}
