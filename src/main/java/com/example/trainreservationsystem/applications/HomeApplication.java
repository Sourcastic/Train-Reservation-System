package com.example.trainreservationsystem.applications;

import java.io.IOException;

import com.example.trainreservationsystem.utils.shared.database.Database;
import com.example.trainreservationsystem.utils.shared.database.DatabaseInitializer;
import com.example.trainreservationsystem.utils.shared.ui.StylesheetHelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//./mvnw clean javafx:run
public class HomeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database first (blocking) to ensure it's ready before starting
        // scheduled tasks
        try {
            System.out.println("🔄 Initializing database...");
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

        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            com.example.trainreservationsystem.services.shared.ScheduledTaskService.getInstance().shutdown();
            Database.closeConnection();
        }));

        // Start scheduled tasks after database is initialized
        com.example.trainreservationsystem.services.shared.ScheduledTaskService.getInstance().start();

        // Load UI immediately (non-blocking)
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/shared/landing-view.fxml"));

        Scene scene = new Scene(root, 1280, 800);
        // Apply global stylesheet to the scene
        StylesheetHelper.applyStylesheet(scene);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }
}
