# MODELS - Data Layer

## Overview

Models are simple Java classes (POJOs - Plain Old Java Objects) that represent business entities. They contain:

- **Fields**: Data properties
- **Constructors**: For creating instances
- **Getters/Setters**: For accessing and modifying data

## Architecture Pattern

```
Database Table ↔ Model Class ↔ Service/Repository
```

## Model Files

### 1. User.java

**Purpose**: Represents a user account in the system.

**Fields**:

- `id`: Unique identifier
- `username`: Login username
- `password`: User password (should be hashed in production)
- `email`: User email address

**Usage**:

```java
User user = new User(1, "demo", "demo123", "demo@example.com");
userSession.login(user);
```

**To Implement Similar**:

```java
public class User {
    private int id;
    private String username;
    // ... other fields

    // Default constructor (required for some frameworks)
    public User() {}

    // Constructor with parameters
    public User(int id, String username, ...) {
        this.id = id;
        this.username = username;
        // ...
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    // ... repeat for each field
}
```

---

### 2. Booking.java

**Purpose**: Represents a train booking reservation.

**Fields**:

- `id`: Booking ID
- `userId`: Who made the booking
- `scheduleId`: Which train schedule
- `status`: PENDING, CONFIRMED, CANCELLED
- `bookingDate`: When booking was created
- `passengers`: List of passengers (relationship)
- `schedule`: Schedule object (for easy access)
- `totalAmount`: Total price

**Key Design**:

- Stores both `scheduleId` (for database) and `schedule` object (for UI)
- This allows easy access to route info without extra queries

**Usage**:

```java
Booking booking = new Booking();
booking.setUserId(1);
booking.setScheduleId(5);
booking.setStatus("PENDING");
booking.setPassengers(passengerList);
```

**To Implement Similar**:

```java
// Store both ID and object for convenience
private int scheduleId;        // For database storage
private Schedule schedule;      // For easy access in UI

// Relationships as lists
private List<Passenger> passengers;
```

---

### 3. Schedule.java

**Purpose**: Represents a train schedule (when a train runs on a route).

**Fields**:

- `id`: Schedule ID
- `route`: Route object (source, destination)
- `departureDate`: Date of departure
- `departureTime`: Time of departure
- `arrivalTime`: Expected arrival time
- `price`: Ticket price per seat
- `capacity`: Total seats available

**Usage**:

```java
Route route = new Route(1, "Express", "New York", "Boston");
Schedule schedule = new Schedule(1, route,
    LocalDate.now().plusDays(1),
    LocalTime.of(8, 0),
    LocalTime.of(12, 0),
    50.0, 100);
```

**To Implement Similar**:

```java
// Use Java 8 time classes for dates
import java.time.LocalDate;
import java.time.LocalTime;

private LocalDate departureDate;
private LocalTime departureTime;
```

---

### 4. Route.java

**Purpose**: Represents a train route (source to destination).

**Fields**:

- `id`: Route ID
- `name`: Route name (e.g., "Northeast Express")
- `source`: Starting station
- `destination`: Ending station

**Usage**:

```java
Route route = new Route(1, "Northeast Express", "New York", "Boston");
```

---

### 5. Passenger.java

**Purpose**: Represents a passenger in a booking.

**Fields**:

- `id`: Passenger ID
- `name`: Passenger name
- `age`: Passenger age
- `bringPet`: Whether bringing a pet
- `hasWheelchair`: Whether wheelchair accessible seat needed
- `seatNumber`: Assigned seat number

**Usage**:

```java
Passenger passenger = new Passenger("John Doe", 25, false, false);
passenger.setSeatNumber(12);
```

---

### 6. Payment.java

**Purpose**: Represents a payment transaction.

**Fields**:

- `id`: Payment ID
- `bookingId`: Which booking this payment is for
- `amount`: Payment amount
- `paymentMethodId`: Which payment method was used
- `status`: SUCCESS, FAILED, PENDING
- `paymentDate`: When payment was made

**Usage**:

```java
Payment payment = new Payment();
payment.setBookingId(bookingId);
payment.setAmount(150.00);
payment.setStatus("SUCCESS");
```

---

### 7. PaymentMethod.java

**Purpose**: Represents a saved payment method.

**Fields**:

- `id`: Payment method ID
- `userId`: Who owns this payment method
- `methodType`: CARD, CASH, JAZZCASH, etc.
- `details`: Encrypted/stored payment details

---

### 8. Complaint.java

**Purpose**: Represents a customer complaint.

**Fields**:

- `id`: Complaint ID
- `userId`: Who submitted the complaint
- `subject`: Complaint subject line
- `description`: Full complaint text
- `trackingId`: Unique tracking number for customer reference
- `createdAt`: When complaint was submitted

**Usage**:

```java
Complaint complaint = new Complaint();
complaint.setUserId(userId);
complaint.setSubject("Delayed train");
complaint.setDescription("Train was 2 hours late");
complaint.setTrackingId("ABC12345");
```

## Common Patterns

### 1. Standard POJO Structure

All models follow this pattern:

```java
public class ModelName {
    // Private fields
    private int id;
    private String name;

    // Default constructor (required)
    public ModelName() {}

    // Constructor with parameters
    public ModelName(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    // ... repeat for each field
}
```

### 2. Relationships

Models can reference other models:

```java
// One-to-one relationship
private Schedule schedule;

// One-to-many relationship
private List<Passenger> passengers;
```

### 3. Status Fields

Many models use status strings:

```java
// Booking statuses
"PENDING", "CONFIRMED", "CANCELLED"

// Payment statuses
"SUCCESS", "FAILED", "PENDING"
```

## Best Practices

1. **Keep models simple**: Only data, no business logic
2. **Use appropriate types**: `LocalDate` for dates, `LocalTime` for times
3. **Include both ID and object**: Store ID for database, object for convenience
4. **Default constructor**: Always include for framework compatibility
5. **Immutable when possible**: Consider making fields `final` if they shouldn't change

## Making Changes Easily

- **Add new field**: Add private field, add getter/setter
- **Add new model**: Create new class following POJO pattern
- **Change relationship**: Update field type (e.g., `List<Passenger>`)
- **Rename field**: Update field name, getter, setter, and all usages

## Database Mapping

Models map directly to database tables:

- `User` → `users` table
- `Booking` → `bookings` table
- `Schedule` → `schedules` table
- etc.

Repositories handle the conversion between database rows and model objects.
