# UTILS - Utility Classes

## Overview

Utility classes provide helper functionality used across the application. They contain static methods and configuration logic.

## Utility Files

### 1. Database.java

**Purpose**: Manages database connections and mock mode.

**Key Features**:

- Creates database connections using JDBC
- Supports PostgreSQL database URLs (from environment variables)
- Automatic fallback to mock mode if connection fails
- Handles both `jdbc:postgresql://` and `postgresql://` URL formats

**How It Works**:

1. Reads `DATABASE_URL` from `.env` file (using dotenv library)
2. Parses connection string (host, port, database, credentials)
3. Creates JDBC connection with SSL support
4. If connection fails: Enables mock mode and throws special exception
5. Repositories catch exception and use mock data

**Key Methods**:

- `getConnection()`: Returns database connection (or throws MOCK_MODE exception)
- `isMockMode()`: Checks if mock mode is enabled
- `enableMockMode()`: Enables mock mode

**Example Usage**:

```java
try (Connection conn = Database.getConnection()) {
    // Use database
} catch (Exception e) {
    if (e.getMessage().equals("MOCK_MODE")) {
        // Use mock data instead
    }
}
```

**Configuration**:
Create `.env` file in project root:

```
DATABASE_URL=postgresql://user:password@host:port/database?sslmode=require
```

**To Implement Similar**:

```java
public class Database {
    private static boolean useMockData = false;
    private static final Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing()
        .load();

    public static Connection getConnection() throws Exception {
        if (useMockData) {
            throw new Exception("MOCK_MODE");
        }

        String databaseUrl = dotenv.get("DATABASE_URL");
        if (databaseUrl == null) {
            enableMockMode();
            throw new Exception("MOCK_MODE");
        }

        // Parse URL and create connection
        // ... connection logic

        return DriverManager.getConnection(jdbcUrl, props);
    }

    public static boolean isMockMode() {
        return useMockData;
    }

    public static void enableMockMode() {
        useMockData = true;
    }
}
```

---

### 2. DatabaseInitializer.java

**Purpose**: Creates database tables and seeds initial data.

**Key Features**:

- Creates all required tables if they don't exist
- Seeds initial data (users, routes, schedules) if tables are empty
- Safe to run multiple times (uses `CREATE TABLE IF NOT EXISTS`)

**How It Works**:

1. Connects to database
2. Executes `CREATE TABLE IF NOT EXISTS` for each table
3. Checks if tables are empty
4. If empty: Inserts seed data (demo user, sample routes, schedules)
5. Returns `true` if successful, `false` if mock mode

**Tables Created**:

- `users`: User accounts
- `routes`: Train routes
- `schedules`: Train schedules
- `bookings`: Reservations
- `passengers`: Passenger details
- `payment_methods`: Saved payment methods
- `payments`: Payment transactions
- `complaints`: Customer complaints
- `notifications`: System notifications
- `tickets`: E-tickets

**Seed Data**:

- Demo user: username="demo", password="demo123"
- Sample routes: New York-Boston, Chicago-St. Louis, etc.
- Sample schedules: Multiple departure times per route

**Example Usage**:

```java
// Called at application startup
boolean dbInitialized = DatabaseInitializer.initialize();
if (dbInitialized) {
    System.out.println("Database ready!");
} else {
    System.out.println("Using mock data");
}
```

**To Implement Similar**:

```java
public class DatabaseInitializer {
    public static boolean initialize() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL" +
                ")");

            // Seed data if empty
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(
                    "INSERT INTO users (username, password) VALUES ('demo', 'demo123')"
                );
            }

            return true;
        } catch (Exception e) {
            if (e.getMessage().equals("MOCK_MODE")) {
                return false;
            }
            // ... error handling
            return false;
        }
    }
}
```

## Common Patterns

### 1. Static Utility Methods

Utility classes use static methods:

```java
public class Database {
    public static Connection getConnection() { ... }
    public static boolean isMockMode() { ... }
}
```

### 2. Environment Variable Reading

Use dotenv library to read `.env` file:

```java
private static final Dotenv dotenv = Dotenv.configure()
    .ignoreIfMissing()
    .load();

String value = dotenv.get("VARIABLE_NAME");
```

### 3. Graceful Fallback

If database unavailable, fallback to mock mode:

```java
try {
    return getConnection();
} catch (Exception e) {
    enableMockMode();
    throw new Exception("MOCK_MODE");
}
```

### 4. Safe Initialization

Use `IF NOT EXISTS` to avoid errors on re-run:

```java
stmt.execute("CREATE TABLE IF NOT EXISTS table_name (...)");
```

## Best Practices

1. **Static methods**: Utilities don't need instances
2. **Error handling**: Always handle connection failures gracefully
3. **Configuration**: Use environment variables for sensitive data
4. **Idempotent**: Initialization should be safe to run multiple times
5. **Mock support**: Always support mock mode for development

## Making Changes Easily

- **Add new table**: Add `CREATE TABLE` statement in `DatabaseInitializer`
- **Change connection**: Update `Database.getConnection()` method
- **Add seed data**: Add insert statements in `seedData()` method
- **Add new utility**: Create new class with static methods

## Dependencies

- **dotenv-java**: For reading `.env` file
- **PostgreSQL JDBC Driver**: For database connections

## Environment Variables

Create `.env` file in project root:

```
DATABASE_URL=postgresql://user:password@host:port/database?sslmode=require
```

If `DATABASE_URL` is missing or connection fails, app automatically uses mock mode.
