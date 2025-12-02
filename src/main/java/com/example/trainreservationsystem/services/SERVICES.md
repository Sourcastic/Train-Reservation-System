# SERVICES - Business Logic Layer

## Overview

Services contain the business logic of the application. They sit between controllers (UI) and repositories (data access), following the **Service Layer Pattern**.

## Architecture Pattern

```
Controller → Service → Repository → Database
     ↑         ↓
     └─────────┘ (returns data/models)
```

## Service Files

### 1. BookingService.java

**Purpose**: Handles all booking-related business logic.

**Key Methods**:

- `createBooking()`: Creates a new booking with passengers
- `getUserBookings()`: Gets all bookings for a user
- `cancelBooking()`: Cancels a booking (updates status)
- `confirmBooking()`: Confirms a booking after payment

**How It Works**:

1. Receives data from controller
2. Validates business rules (if needed)
3. Calls repository to save/retrieve data
4. Returns model objects to controller

**Example**:

```java
// Controller calls service
Booking booking = bookingService.createBooking(userId, schedule, passengers);

// Service creates booking and saves via repository
public Booking createBooking(int userId, Schedule schedule, List<Passenger> passengers) {
    Booking booking = new Booking();
    booking.setUserId(userId);
    booking.setScheduleId(schedule.getId());
    booking.setStatus("PENDING");
    booking.setPassengers(passengers);

    return bookingRepository.createBooking(booking);
}
```

**To Implement Similar**:

```java
public class BookingService {
    private final BookingRepository repository;

    // Constructor injection (dependency injection)
    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public Booking createBooking(...) {
        // Business logic here
        // Call repository to persist
        return repository.createBooking(booking);
    }
}
```

---

### 2. TrainService.java

**Purpose**: Handles train schedule search and retrieval.

**Key Methods**:

- `searchSchedules()`: Searches schedules by source, destination, date
- `getSchedule()`: Gets a specific schedule by ID

**How It Works**:

1. Receives search criteria from controller
2. Delegates to repository for data access
3. Returns list of matching schedules

**Example**:

```java
List<Schedule> schedules = trainService.searchSchedules("New York", "Boston", LocalDate.now());
```

**To Implement Similar**:

```java
public class TrainService {
    private final TrainRepository repository;

    public TrainService(TrainRepository repository) {
        this.repository = repository;
    }

    // Simple delegation - no complex business logic needed
    public List<Schedule> searchSchedules(String source, String dest, LocalDate date) {
        return repository.searchSchedules(source, dest, date);
    }
}
```

---

### 3. PaymentService.java

**Purpose**: Handles payment processing.

**Key Methods**:

- `getPaymentMethods()`: Gets saved payment methods for user
- `addPaymentMethod()`: Saves a new payment method
- `processPayment()`: Processes a payment and updates booking status

**How It Works**:

1. Creates payment record
2. Saves payment to database
3. Updates booking status to CONFIRMED

**Example**:

```java
paymentService.processPayment(bookingId, 150.00, paymentMethodId);
```

---

### 4. ComplaintService.java

**Purpose**: Handles complaint submission.

**Key Methods**:

- `submitComplaint()`: Creates a complaint with tracking ID

**How It Works**:

1. Creates complaint object
2. Generates unique tracking ID (UUID)
3. Saves via repository

**Example**:

```java
complaintService.submitComplaint(userId, "Delayed train", "Train was 2 hours late");
// Generates tracking ID like "ABC12345"
```

---

### 5. NotificationService.java

**Purpose**: Manages in-memory notifications (singleton pattern).

**Key Features**:

- Singleton: Only one instance exists
- Observer pattern: Listeners notified when notifications change
- In-memory storage: Notifications stored in a list

**Key Methods**:

- `add()`: Adds a notification message
- `getAll()`: Gets all notifications
- `remove()`: Removes a notification
- `clear()`: Clears all notifications
- `addListener()`: Registers a listener for updates

**How It Works**:

1. Stores notifications in a `List<String>`
2. When notification added, notifies all listeners
3. Listeners update UI (e.g., badge count)

**Example**:

```java
// Add notification
NotificationService.getInstance().add("Payment successful!");

// Listen for changes
NotificationService.getInstance().addListener(count -> {
    updateBadge(count);
});
```

**To Implement Similar**:

```java
public class NotificationService {
    private static NotificationService instance;
    private final List<String> messages = new ArrayList<>();
    private final List<NotificationListener> listeners = new ArrayList<>();

    // Singleton pattern
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void add(String message) {
        messages.add(0, message); // Latest first
        notifyListeners();
    }

    private void notifyListeners() {
        for (NotificationListener listener : listeners) {
            listener.onNotificationUpdate(messages.size());
        }
    }

    public interface NotificationListener {
        void onNotificationUpdate(int count);
    }
}
```

---

### 6. UserSession.java

**Purpose**: Manages current user session (singleton pattern).

**Key Features**:

- Stores current logged-in user
- Stores selected schedule (for booking flow)
- Stores pending booking (between booking and payment)

**Key Methods**:

- `login()`: Sets current user
- `logout()`: Clears current user
- `isLoggedIn()`: Checks if user is logged in
- `setSelectedSchedule()`: Stores selected schedule
- `setPendingBooking()`: Stores booking before payment

**How It Works**:

1. Singleton pattern ensures one session per application
2. Stores temporary data between screens
3. Controllers access via `UserSession.getInstance()`

**Example**:

```java
// Store data
UserSession.getInstance().setSelectedSchedule(schedule);
UserSession.getInstance().setPendingBooking(booking);

// Retrieve data
Schedule schedule = UserSession.getInstance().getSelectedSchedule();
Booking booking = UserSession.getInstance().getPendingBooking();
```

**To Implement Similar**:

```java
public class UserSession {
    private static UserSession instance;
    private User currentUser;
    private Schedule selectedSchedule;

    private UserSession() {} // Private constructor

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Getters and setters
    public void login(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
```

---

### 7. ServiceFactory.java

**Purpose**: Creates and provides service instances (Factory Pattern).

**Key Features**:

- Singleton services: Each service created once and reused
- Dependency injection: Services get their repositories
- Centralized service creation

**How It Works**:

1. Lazy initialization: Services created on first access
2. Stores services as static fields
3. Returns same instance on subsequent calls

**Example**:

```java
BookingService service = ServiceFactory.getBookingService();
// First call: Creates new instance
// Subsequent calls: Returns same instance
```

**To Implement Similar**:

```java
public class ServiceFactory {
    private static BookingService bookingService;

    public static BookingService getBookingService() {
        if (bookingService == null) {
            // Create with dependencies
            bookingService = new BookingService(
                new BookingRepository(),
                new TrainRepository()
            );
        }
        return bookingService;
    }
}
```

## Common Patterns

### 1. Service Layer Pattern

Services contain business logic, repositories handle data access:

```java
// Service has business logic
public Booking createBooking(...) {
    // Validate business rules
    if (schedule.getCapacity() < passengers.size()) {
        throw new Exception("Not enough seats");
    }

    // Create booking
    Booking booking = new Booking();
    // ... set fields

    // Save via repository
    return repository.createBooking(booking);
}
```

### 2. Dependency Injection

Services receive repositories via constructor:

```java
public class BookingService {
    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }
}
```

### 3. Singleton Pattern

For shared state (session, notifications):

```java
public class Service {
    private static Service instance;

    public static synchronized Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }
}
```

## Best Practices

1. **Keep services focused**: One service per domain (Booking, Payment, etc.)
2. **No UI code**: Services don't know about JavaFX or controllers
3. **Business logic only**: Data access goes in repositories
4. **Return models**: Services return model objects, not raw data
5. **Handle errors**: Services should throw meaningful exceptions

## Making Changes Easily

- **Add new business rule**: Add validation in service method
- **Add new service method**: Add method, call repository, return model
- **Change business logic**: Update service method (repository stays same)
- **Add new service**: Create class, add to ServiceFactory

## Service vs Repository

**Service**:

- Contains business logic
- Validates business rules
- Coordinates multiple repositories if needed
- Returns model objects

**Repository**:

- Only handles data access
- No business logic
- Maps database rows to models
- Simple CRUD operations
