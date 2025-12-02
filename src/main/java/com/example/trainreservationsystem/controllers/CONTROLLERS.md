# CONTROLLERS - User Interface Layer

## Overview

Controllers handle all user interactions and UI logic. They follow the **MVC (Model-View-Controller)** pattern where:

- **View**: FXML files (in resources folder) define the UI layout
- **Controller**: Java classes handle user input and update the view
- **Model**: Data classes (in models package) represent business entities

## Architecture Pattern

```
User Action → Controller → Service → Repository → Database
                ↓
            Update View
```

## Controller Files

### 1. HomeController.java

**Purpose**: Main navigation controller - manages the overall application layout and routing between different screens.

**Key Features**:

- Singleton pattern (one instance shared across app)
- Manages navigation between views (Search, History, Complaints, Dashboard)
- Handles notification badge updates
- Loads FXML views dynamically into a content area

**How It Works**:

1. `initialize()`: Sets up icons, notification listener, loads default view
2. Navigation methods (`showSearch()`, `showHistory()`, etc.): Load different FXML files
3. `loadView()`: Dynamically replaces content in the main StackPane

**To Implement Similar**:

```java
// 1. Create FXML file with StackPane for content area
// 2. Use FXMLLoader to load views
Parent view = FXMLLoader.load(getClass().getResource("/path/to/view.fxml"));
contentArea.getChildren().setAll(view);

// 3. Use singleton pattern for shared access
private static HomeController instance;
public static HomeController getInstance() { return instance; }
```

---

### 2. SearchController.java

**Purpose**: Handles train schedule search functionality.

**Key Features**:

- Searchable combo boxes for source/destination (prevents selecting same city)
- Date picker for travel date
- TableView to display search results
- Double-click or "Book" button to select a schedule

**How It Works**:

1. User selects source, destination, and date
2. Calls `TrainService.searchSchedules()` to get results
3. Displays results in TableView
4. On selection, stores schedule in `UserSession` and navigates to booking

**To Implement Similar**:

```java
// 1. Use SearchableComboBox for autocomplete dropdowns
@FXML private SearchableComboBox<String> sourceCombo;

// 2. Filter options dynamically (prevent same source/destination)
sourceCombo.valueProperty().addListener((obs, old, newVal) -> {
    // Filter destination to exclude selected source
    destinationCombo.setItems(filteredList);
});

// 3. Use TableView with custom cell factories for action buttons
actionCol.setCellFactory(column -> new TableCell<>() {
    private Button bookBtn = new Button("Book");
    // Handle button click
});
```

---

### 3. BookingController.java

**Purpose**: Manages seat selection and passenger information entry.

**Key Features**:

- Visual seat grid (60 seats, 6 per row with aisle)
- Real-time seat selection (available/occupied/selected states)
- Passenger information form (name, age)
- Dynamic price calculation

**How It Works**:

1. Loads schedule from `UserSession`
2. Creates seat grid dynamically with buttons
3. Tracks selected seats in a `Set<Integer>`
4. Validates form (name + at least one seat)
5. Creates booking via `BookingService` and stores in `UserSession`

**To Implement Similar**:

```java
// 1. Create seat grid dynamically
for (int row = 0; row < totalRows; row++) {
    for (int col = 0; col < seatsPerRow; col++) {
        Button seatBtn = new Button(String.valueOf(seatNumber));
        seatBtn.setOnAction(e -> toggleSeat(seatNumber, seatBtn));
        seatGrid.add(seatBtn, col, row);
    }
}

// 2. Track selected seats
private Set<Integer> selectedSeats = new HashSet<>();
private void toggleSeat(int seatNumber, Button button) {
    if (selectedSeats.contains(seatNumber)) {
        selectedSeats.remove(seatNumber);
        button.getStyleClass().remove("seat-selected");
    } else {
        selectedSeats.add(seatNumber);
        button.getStyleClass().add("seat-selected");
    }
}

// 3. Real-time validation
nameField.textProperty().addListener((obs, old, val) -> validateForm());
```

---

### 4. SimplePaymentController.java

**Purpose**: Handles payment processing with multiple payment methods.

**Key Features**:

- Accordion UI (only one payment method active at a time)
- Three payment methods: Card, Cash, JazzCash
- Real-time input formatting (card number, expiry date, CVV)
- Payment validation before processing

**How It Works**:

1. Loads pending booking from `UserSession`
2. Displays booking details (route, date, passengers, total)
3. User selects payment method and enters details
4. Validates inputs, processes payment (simulated), confirms booking
5. Sends notifications and redirects to history

**To Implement Similar**:

```java
// 1. Use Accordion for mutually exclusive options
@FXML private Accordion paymentAccordion;
paymentAccordion.setExpandedPane(cardPane); // Default selection

// 2. Real-time input formatting
cardNumberField.textProperty().addListener((obs, old, val) -> {
    // Remove non-digits, format as groups of 4
    String formatted = formatCardNumber(val);
    if (!formatted.equals(val)) {
        cardNumberField.setText(formatted);
    }
});

// 3. Simulate payment processing with delay
PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
pause.setOnFinished(e -> {
    // Process payment
    bookingService.confirmBooking(bookingId);
});
pause.play();
```

---

### 5. HistoryController.java

**Purpose**: Displays user's booking history with actions (cancel, view ticket).

**Key Features**:

- TableView showing all user bookings
- Action buttons per row (Cancel, View Ticket)
- Conditional button display based on booking status
- Modal window for ticket view

**How It Works**:

1. Loads user bookings from `BookingService`
2. Displays in TableView with custom action column
3. Cancel: Confirms, updates status, refreshes table
4. View Ticket: Opens modal window with ticket details

**To Implement Similar**:

```java
// 1. Custom TableCell with buttons
actionsCol.setCellFactory(column -> new TableCell<>() {
    private Button cancelBtn = new Button("Cancel");
    private Button ticketBtn = new Button("View Ticket");

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            Booking booking = getTableView().getItems().get(getIndex());
            // Show buttons conditionally based on status
            if ("CONFIRMED".equals(booking.getStatus())) {
                setGraphic(new HBox(cancelBtn, ticketBtn));
            }
        }
    }
});

// 2. Open modal window
FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/ticket.fxml"));
Parent root = loader.load();
Stage stage = new Stage();
stage.initModality(Modality.APPLICATION_MODAL);
stage.setScene(new Scene(root));
stage.showAndWait();
```

---

### 6. ComplaintController.java

**Purpose**: Simple form for submitting complaints.

**Key Features**:

- Text fields for subject and description
- Validation (non-empty fields)
- Submits via `ComplaintService`

**How It Works**:

1. User enters subject and description
2. Validates inputs
3. Calls `ComplaintService.submitComplaint()`
4. Shows success message and clears form

**To Implement Similar**:

```java
@FXML
public void handleSubmit() {
    String subject = subjectField.getText().trim();
    String desc = descriptionArea.getText().trim();

    if (subject.isEmpty() || desc.isEmpty()) {
        showAlert("Error", "Fields cannot be empty");
        return;
    }

    complaintService.submitComplaint(userId, subject, desc);
    showAlert("Success", "Complaint submitted");
    // Clear form
    subjectField.clear();
    descriptionArea.clear();
}
```

---

### 7. Other Controllers

- **MemberHomeController**: Dashboard with quick actions
- **NotificationController**: Manages notification inbox
- **PaymentMethodController**: Manages saved payment methods
- **TicketViewController**: Displays e-ticket details

## Common Patterns

### 1. Alert Creation (Repeated Pattern)

Many controllers create alerts the same way. Consider extracting to a utility:

```java
private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setContentText(content);
    // Apply stylesheet
    alert.getDialogPane().getStylesheets().add(...);
    alert.showAndWait();
}
```

### 2. FXML Injection

All UI components are injected via `@FXML`:

```java
@FXML
private Button submitButton;  // Must match fx:id in FXML
```

### 3. Service Access

Controllers get services via `ServiceFactory`:

```java
private final BookingService bookingService = ServiceFactory.getBookingService();
```

### 4. Session Management

Use `UserSession` to pass data between screens:

```java
// Store
UserSession.getInstance().setSelectedSchedule(schedule);

// Retrieve
Schedule schedule = UserSession.getInstance().getSelectedSchedule();
```

## Best Practices

1. **Keep controllers thin**: Business logic goes in services, not controllers
2. **Validate early**: Check inputs before calling services
3. **Handle errors gracefully**: Show user-friendly error messages
4. **Use property bindings**: For real-time UI updates
5. **Separate concerns**: Each controller handles one screen/feature

## Making Changes Easily

- **Add new screen**: Create FXML file, create controller class, add navigation method in HomeController
- **Modify validation**: Update validation methods in controller
- **Change UI layout**: Edit FXML file (no code changes needed)
- **Add new field**: Add `@FXML` field, update FXML, handle in controller methods
