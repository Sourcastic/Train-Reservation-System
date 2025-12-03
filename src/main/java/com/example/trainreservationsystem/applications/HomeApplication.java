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
        // Load UI immediately (non-blocking) - show UI first for better UX
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/shared/landing-view.fxml"));

        Scene scene = new Scene(root, 1280, 800);
        // Apply global stylesheet to the scene
        StylesheetHelper.applyStylesheet(scene);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();

        // Initialize database in background thread to avoid blocking UI
        Thread dbInitThread = new Thread(() -> {
            try {
                System.out.println("🔄 Initializing database in background...");
                boolean success = DatabaseInitializer.initialize();
                if (success) {
                    System.out.println("✅ Database initialization completed");
                    // Start scheduled tasks after database is initialized
                    javafx.application.Platform.runLater(() -> {
                        com.example.trainreservationsystem.services.shared.ScheduledTaskService.getInstance().start();
                    });
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
            com.example.trainreservationsystem.services.shared.ScheduledTaskService.getInstance().shutdown();
            Database.closeConnection();
        }));
    }
}
