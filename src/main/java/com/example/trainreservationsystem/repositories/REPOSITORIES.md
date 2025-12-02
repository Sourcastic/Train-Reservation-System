# REPOSITORIES - Data Access Layer

## Overview

Repositories handle all database operations. They abstract away SQL queries and provide a clean interface for services to access data. They follow the **Repository Pattern**.

## Architecture Pattern

```
Service → Repository → Database (SQL)
            ↓
        Returns Models
```

## Key Design: Mock Mode Support

All repositories support **mock mode** - if database connection fails, they use in-memory data. This makes development easier and the app works without a database.

## Repository Files

### 1. BookingRepository.java

**Purpose**: Handles all database operations for bookings.

**Key Methods**:

- `createBooking()`: Inserts new booking and passengers
- `getBookingsByUserId()`: Gets all bookings for a user
- `updateBookingStatus()`: Updates booking status (PENDING → CONFIRMED)
- `isSeatBooked()`: Checks if a seat is already booked

**How It Works**:

1. Checks if mock mode is enabled
2. If mock: Uses in-memory list
3. If real DB: Executes SQL queries
4. Maps database rows to `Booking` objects
5. Returns models to service

**Example - Mock Mode**:

```java
if (Database.isMockMode()) {
    booking.setId(mockIdCounter++);
    booking.setBookingDate(LocalDateTime.now());
    mockDb.add(booking);
    return booking;
}
```

**Example - Real Database**:

```java
String query = "INSERT INTO bookings (user_id, schedule_id, status) VALUES (?, ?, ?)";
try (PreparedStatement stmt = conn.prepareStatement(query)) {
    stmt.setInt(1, booking.getUserId());
    stmt.setInt(2, booking.getScheduleId());
    stmt.setString(3, booking.getStatus());
    stmt.executeUpdate();
}
```

**To Implement Similar**:

```java
public class BookingRepository {
    // Mock data storage
    private static List<Booking> mockDb = new ArrayList<>();
    private static int mockIdCounter = 1;

    public Booking createBooking(Booking booking) {
        // Check mock mode first
        if (Database.isMockMode()) {
            // Mock implementation
            booking.setId(mockIdCounter++);
            mockDb.add(booking);
            return booking;
        }

        // Real database implementation
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, booking.getUserId());
            // ... set other parameters

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                booking.setId(rs.getInt("id"));
            }
            return booking;

        } catch (Exception e) {
            // Fallback to mock on error
            if (e.getMessage().equals("MOCK_MODE")) {
                return createBooking(booking); // Retry with mock
            }
            // ... error handling
        }
    }
}
```

---

### 2. TrainRepository.java

**Purpose**: Handles schedule and route queries.

**Key Methods**:

- `searchSchedules()`: Searches schedules by source, destination, date
- `getScheduleById()`: Gets a specific schedule by ID

**How It Works**:

1. If mock: Returns hardcoded mock schedules
2. If real DB: Joins `schedules` and `routes` tables
3. Maps ResultSet to `Schedule` objects with `Route` objects

**Example - Mock Schedules**:

```java
private List<Schedule> getMockSchedules(String source, String dest, LocalDate date) {
    List<Schedule> schedules = new ArrayList<>();

    if (source.contains("new york") && dest.contains("boston")) {
        Route r = new Route(1, "Northeast Express", "New York", "Boston");
        schedules.add(new Schedule(1, r, date, LocalTime.of(8, 0), ...));
        schedules.add(new Schedule(2, r, date, LocalTime.of(14, 0), ...));
    }
    return schedules;
}
```

**Example - Real Database Query**:

```java
String query = "SELECT s.*, r.name as route_name, r.source, r.destination " +
               "FROM schedules s " +
               "JOIN routes r ON s.route_id = r.id " +
               "WHERE LOWER(r.source) = LOWER(?) AND LOWER(r.destination) = LOWER(?) " +
               "AND s.departure_date = ?";
```

**To Implement Similar**:

```java
// Map database row to model object
private Schedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
    Route route = new Route(
        rs.getInt("route_id"),
        rs.getString("route_name"),
        rs.getString("source"),
        rs.getString("destination")
    );

    return new Schedule(
        rs.getInt("id"),
        route,
        rs.getDate("departure_date").toLocalDate(),
        rs.getTime("departure_time").toLocalTime(),
        rs.getTime("arrival_time").toLocalTime(),
        rs.getDouble("price"),
        rs.getInt("capacity")
    );
}
```

---

### 3. UserRepository.java

**Purpose**: Handles user authentication and retrieval.

**Key Methods**:

- `getUserByUsername()`: Gets user by username (for login)

**How It Works**:

1. If mock: Returns hardcoded demo user
2. If real DB: Queries `users` table

**Example**:

```java
public User getUserByUsername(String username) {
    if (Database.isMockMode()) {
        if ("demo".equals(username)) {
            return new User(1, "demo", "demo123", "demo@example.com");
        }
        return null;
    }

    String query = "SELECT * FROM users WHERE username = ?";
    // ... execute query and map to User object
}
```

---

### 4. PaymentRepository.java

**Purpose**: Handles payment and payment method storage.

**Key Methods**:

- `savePayment()`: Saves payment transaction
- `getPaymentMethods()`: Gets saved payment methods for user
- `savePaymentMethod()`: Saves a new payment method

---

### 5. ComplaintRepository.java

**Purpose**: Handles complaint storage.

**Key Methods**:

- `saveComplaint()`: Saves complaint to database

## Common Patterns

### 1. Mock Mode Pattern

All repositories check mock mode first:

```java
public SomeModel getData() {
    if (Database.isMockMode()) {
        // Return mock data
        return mockData;
    }

    // Real database query
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        // ... execute query
    }
}
```

### 2. ResultSet Mapping

Convert database rows to model objects:

```java
private Model mapResultSetToModel(ResultSet rs) throws SQLException {
    return new Model(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getDate("date").toLocalDate()
    );
}
```

### 3. Prepared Statements

Always use PreparedStatement to prevent SQL injection:

```java
String query = "SELECT * FROM table WHERE id = ?";
PreparedStatement stmt = conn.prepareStatement(query);
stmt.setInt(1, id);  // Safe parameter binding
ResultSet rs = stmt.executeQuery();
```

### 4. Try-With-Resources

Automatically closes connections:

```java
try (Connection conn = Database.getConnection();
     PreparedStatement stmt = conn.prepareStatement(query)) {
    // Use connection
} // Automatically closed here
```

### 5. Transaction Handling

For operations that need to be atomic:

```java
conn.setAutoCommit(false); // Start transaction
try {
    // Multiple operations
    stmt1.executeUpdate();
    stmt2.executeUpdate();
    conn.commit(); // Save all changes
} catch (Exception e) {
    conn.rollback(); // Undo all changes on error
}
```

## Best Practices

1. **Always use PreparedStatement**: Prevents SQL injection
2. **Handle exceptions gracefully**: Fallback to mock mode if DB fails
3. **Close resources**: Use try-with-resources
4. **Map to models**: Return model objects, not raw ResultSets
5. **Support mock mode**: Makes development easier

## Making Changes Easily

- **Add new query**: Add method, write SQL, map ResultSet to model
- **Change table structure**: Update SQL queries and mapping methods
- **Add mock data**: Update mock methods with test data
- **Add new repository**: Create class, implement CRUD methods, add mock support

## Database Schema

Repositories work with these tables:

- `users`: User accounts
- `routes`: Train routes
- `schedules`: Train schedules
- `bookings`: Reservations
- `passengers`: Passenger details
- `payments`: Payment transactions
- `payment_methods`: Saved payment methods
- `complaints`: Customer complaints

See `DatabaseInitializer.java` for full schema.

## Error Handling

Repositories catch database errors and fallback to mock mode:

```java
catch (Exception e) {
    if (e.getMessage().equals("MOCK_MODE")) {
        return getData(); // Retry with mock
    }
    // Log error
    System.err.println("Error: " + e.getMessage());
    return getData(); // Fallback to mock
}
```

This ensures the app always works, even without a database connection.
