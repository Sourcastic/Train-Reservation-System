# SEEDERS - Database Seeding

## Overview

Seeders populate the database with initial data for development and testing. They are organized in separate classes for better maintainability and follow the **Single Responsibility Principle**.

## Architecture Pattern

```
DatabaseInitializer → DatabaseSeeder → Individual Seeders → Database
```

## Seeder Files

### 1. DatabaseSeeder.java

**Purpose**: Main coordinator that runs all individual seeders in the correct order.

**Key Features**:

- Coordinates all seeders
- Ensures correct seeding order (respects foreign key dependencies)
- Skips seeding if in mock mode
- Provides summary of what was seeded

**How It Works**:

1. Checks if database is in mock mode (skips if true)
2. Gets database connection
3. Runs seeders in order: Users → Routes → Schedules
4. Reports summary of seeding results

**Usage**:

```java
// Called automatically by DatabaseInitializer
DatabaseSeeder.seed();
```

**To Implement Similar**:

```java
public class DatabaseSeeder {
    public static boolean seed() {
        try (Connection conn = Database.getConnection()) {
            // Run seeders in dependency order
            UserSeeder.seed(conn);
            RouteSeeder.seed(conn);
            ScheduleSeeder.seed(conn);
            return true;
        } catch (Exception e) {
            // Handle errors
            return false;
        }
    }
}
```

---

### 2. UserSeeder.java

**Purpose**: Seeds user accounts into the database.

**Key Features**:

- Only seeds if users table is empty (idempotent)
- Creates demo users with different roles
- Includes loyalty points for testing

**Data Seeded**:

- `demo` - Demo customer account (password: demo123)
- `admin` - Admin account (password: admin123)
- `john_doe` - Sample customer (password: password123)
- `jane_smith` - Sample customer (password: password123)

**How It Works**:

1. Checks if users table has any records
2. If empty: Inserts demo users
3. If not empty: Skips seeding (preserves existing data)

**Example**:

```java
// Seeds 4 users if table is empty
UserSeeder.seed(connection);
```

**To Implement Similar**:

```java
public class UserSeeder {
    public static boolean seed(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Check if table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Already has data
            }

            // Insert seed data
            stmt.executeUpdate("INSERT INTO users (...) VALUES (...)");
            return true;
        } catch (Exception e) {
            // Handle errors
            return false;
        }
    }
}
```

---

### 3. RouteSeeder.java

**Purpose**: Seeds train routes into the database.

**Key Features**:

- Only seeds if routes table is empty
- Creates bidirectional routes (e.g., New York ↔ Boston)
- Includes 10 different routes

**Data Seeded**:

- Northeast Express: New York → Boston
- Northeast Return: Boston → New York
- Midwest Line: Chicago → St. Louis
- Midwest Return: St. Louis → Chicago
- West Coast Express: Los Angeles → San Francisco
- West Coast Return: San Francisco → Los Angeles
- East Coast Line: Washington DC → New York
- East Coast Return: New York → Washington DC
- Southern Route: Atlanta → Miami
- Southern Return: Miami → Atlanta

**How It Works**:

1. Checks if routes table is empty
2. If empty: Inserts 10 routes
3. If not empty: Skips seeding

**To Add New Routes**:

```java
// In RouteSeeder.seed(), add to INSERT statement:
"('Route Name', 'Source City', 'Destination City'), " +
```

---

### 4. ScheduleSeeder.java

**Purpose**: Seeds train schedules into the database.

**Key Features**:

- Only seeds if schedules table is empty
- Creates schedules for the next 7 days
- Multiple schedules per route per day
- Different prices and capacities per route

**Data Seeded**:

- Schedules for next 7 days
- Multiple departure times per route
- Varying prices (based on route and time)
- Different capacities per route type

**How It Works**:

1. Checks if schedules table is empty
2. If empty: Generates schedules for next 7 days
3. Creates multiple schedules per route per day
4. Uses helper method to build SQL efficiently

**Schedule Details**:

- **Route 1** (NY→Boston): 2 schedules/day (8:00, 14:00)
- **Route 2** (Boston→NY): 2 schedules/day (9:00, 15:30)
- **Route 3** (Chicago→St. Louis): 2 schedules/day (9:30, 16:00)
- **Route 5** (LA→SF): 2 schedules/day (10:00, 18:00)
- **Route 7** (DC→NY): 2 schedules/day (7:30, 13:00)
- Other routes: 1 schedule/day

**To Modify Schedules**:

```java
// In ScheduleSeeder.seed(), modify the loop:
for (int day = 1; day <= 7; day++) {
    LocalDate scheduleDate = today.plusDays(day);

    // Add your schedule
    addSchedule(sql, routeId, scheduleDate,
                LocalTime.of(8, 0), LocalTime.of(12, 0),
                capacity, price);
}
```

---

## Common Patterns

### 1. Idempotent Seeding

All seeders check if data exists before inserting:

```java
ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM table_name");
if (rs.next() && rs.getInt(1) > 0) {
    return false; // Skip seeding
}
// Insert data
```

### 2. Dependency Order

Seeders run in order that respects foreign keys:

1. **Users** (no dependencies)
2. **Routes** (no dependencies)
3. **Schedules** (depends on Routes)

### 3. Error Handling

Each seeder handles its own errors:

```java
try {
    // Seeding logic
    return true;
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
    e.printStackTrace();
    return false;
}
```

### 4. Connection Management

Seeders receive connection from DatabaseSeeder:

```java
public static boolean seed(Connection conn) {
    // Use provided connection
    // Don't close it (DatabaseSeeder manages lifecycle)
}
```

## Best Practices

1. **Idempotent**: Seeders should be safe to run multiple times
2. **Order matters**: Seed in dependency order (users before schedules)
3. **Check before insert**: Always check if data exists first
4. **Handle errors**: Each seeder handles its own exceptions
5. **Clear messages**: Print helpful messages about what's being seeded

## Making Changes Easily

### Add New User

Edit `UserSeeder.java`:

```java
"('newuser', 'password', 'email@example.com', 'CUSTOMER', 0), " +
```

### Add New Route

Edit `RouteSeeder.java`:

```java
"('Route Name', 'Source', 'Destination'), " +
```

### Add New Schedule

Edit `ScheduleSeeder.java`:

```java
addSchedule(sql, routeId, scheduleDate,
            LocalTime.of(10, 0), LocalTime.of(15, 0),
            100, 50.00);
```

### Create New Seeder

1. Create new class: `NewDataSeeder.java`
2. Follow pattern from existing seeders
3. Add to `DatabaseSeeder.seed()` in correct order

## Seeding Order

The seeding order is important due to foreign key constraints:

```
1. Users (no dependencies)
   ↓
2. Routes (no dependencies)
   ↓
3. Schedules (depends on Routes)
   ↓
4. (Future: Bookings depend on Users and Schedules)
   ↓
5. (Future: Payments depend on Bookings)
```

## Running Seeders

Seeders run automatically when:

- Application starts
- `DatabaseInitializer.initialize()` is called
- Database is successfully connected

To manually seed:

```java
DatabaseSeeder.seed();
```

## Mock Mode

Seeders automatically skip if database is in mock mode:

```java
if (Database.isMockMode()) {
    System.out.println("Skipping seeding (mock mode)");
    return true;
}
```

This prevents errors when database connection is unavailable.

## Adding More Seed Data

To add seed data for other tables (e.g., Bookings, Payments):

1. **Create new seeder class**:

```java
public class BookingSeeder {
    public static boolean seed(Connection conn) {
        // Check if empty
        // Insert seed bookings
    }
}
```

2. **Add to DatabaseSeeder**:

```java
// In DatabaseSeeder.seed()
BookingSeeder.seed(conn); // After UserSeeder and ScheduleSeeder
```

3. **Follow the pattern**: Check → Insert → Return

## Summary

- **DatabaseSeeder**: Main coordinator
- **UserSeeder**: Seeds 4 demo users
- **RouteSeeder**: Seeds 10 routes
- **ScheduleSeeder**: Seeds schedules for next 7 days
- All seeders are idempotent (safe to run multiple times)
- Seeders respect foreign key dependencies
- Easy to extend with new seed data
