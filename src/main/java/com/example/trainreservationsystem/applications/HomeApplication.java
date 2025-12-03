package com.example.trainreservationsystem.applications;

import java.io.IOException;

import com.example.trainreservationsystem.utils.shared.database.Database;
import com.example.trainreservationsystem.utils.shared.database.DatabaseInitializer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//./mvnw clean javafx:run
public class HomeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database in background thread to avoid blocking UI
        Thread dbInitThread = new Thread(() -> {
            try {
                System.out.println("🔄 Initializing database in background...");
                boolean success = DatabaseInitializer.initialize();
                if (success) {
                    System.out.println("✅ Database initialization completed");
                } else {
                    System.err.println("❌ Database initialization failed");
                }
            } catch (Exception e) {
                System.err.println("❌ Database initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        dbInitThread.setDaemon(true);
        dbInitThread.start();

        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            com.example.trainreservationsystem.services.ScheduledTaskService.getInstance().shutdown();
            Database.closeConnection();
        }));

        // Start scheduled tasks
        com.example.trainreservationsystem.services.ScheduledTaskService.getInstance().start();

        // Load UI immediately (non-blocking)
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/landing-view.fxml"));

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }
}
