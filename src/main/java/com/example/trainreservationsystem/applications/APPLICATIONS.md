# APPLICATIONS - Entry Point

## Overview

The applications package contains the main entry point of the JavaFX application. This is where the application starts.

## Application File

### HomeApplication.java

**Purpose**: Main entry point for the JavaFX application.

**Key Features**:

- Extends `Application` (JavaFX base class)
- Initializes database on startup
- Auto-logs in demo user
- Loads main FXML view
- Sets up the primary stage (window)

**How It Works**:

1. `start()` method called by JavaFX when app launches
2. Calls `DatabaseInitializer.initialize()` to set up database
3. Attempts to get demo user from database
4. If database fails or user not found: Creates mock user
5. Logs in user via `UserSession`
6. Loads `home-view.fxml` (main application window)
7. Displays the window

**Code Flow**:

```java
@Override
public void start(Stage stage) {
    // 1. Initialize database
    boolean dbInitialized = DatabaseInitializer.initialize();

    // 2. Get or create user
    User user = null;
    if (dbInitialized) {
        user = new UserRepository().getUserByUsername("demo");
    }
    if (user == null) {
        user = new User(1, "demo", "demo123", "demo@example.com");
    }

    // 3. Login user
    UserSession.getInstance().login(user);

    // 4. Load main view
    Parent root = FXMLLoader.load(getClass().getResource("/path/to/home-view.fxml"));

    // 5. Show window
    Scene scene = new Scene(root, 1380, 780);
    stage.setScene(scene);
    stage.setTitle("Train Reservation System");
    stage.show();
}
```

**To Implement Similar**:

```java
public class MyApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Initialize any required services
        DatabaseInitializer.initialize();

        // 2. Set up initial state
        UserSession.getInstance().login(getDefaultUser());

        // 3. Load main FXML file
        Parent root = FXMLLoader.load(
            getClass().getResource("/com/example/app/main-view.fxml")
        );

        // 4. Create scene and show
        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("My Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // JavaFX entry point
    }
}
```

## JavaFX Application Lifecycle

1. **main() method**: Calls `Application.launch()`
2. **start() method**: Called by JavaFX framework
3. **Application runs**: User interacts with UI
4. **stop() method**: Called when application closes (optional)

## Key Concepts

### 1. Stage (Window)

The primary window of the application:

```java
Stage stage; // The main window
stage.setTitle("My App");
stage.setScene(scene);
stage.show();
```

### 2. Scene (Content)

The content displayed in the stage:

```java
Scene scene = new Scene(root, width, height);
// root is the Parent node loaded from FXML
```

### 3. FXML Loading

Loads the UI layout from FXML file:

```java
Parent root = FXMLLoader.load(
    getClass().getResource("/com/example/app/view.fxml")
);
```

## Best Practices

1. **Initialize early**: Set up database and services in `start()`
2. **Handle errors**: Gracefully handle database failures
3. **Default user**: Provide demo user for easy testing
4. **Window size**: Set reasonable default window size
5. **Title**: Set descriptive window title

## Making Changes Easily

- **Change window size**: Update `Scene` constructor parameters
- **Change default view**: Update FXML path in `FXMLLoader.load()`
- **Add startup logic**: Add code before `stage.show()`
- **Change window title**: Update `stage.setTitle()`

## Running the Application

The application is typically run via:

```bash
# Maven
mvn javafx:run

# Or directly
java -cp ... com.example.trainreservationsystem.applications.HomeApplication
```

The `main()` method is usually in the Application class or a separate launcher class.
