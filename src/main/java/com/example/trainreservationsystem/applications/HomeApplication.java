package com.example.trainreservationsystem.applications;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database schema and seed data
        boolean dbInitialized = com.example.trainreservationsystem.utils.database.DatabaseInitializer.initialize();

        // Auto-login demo user
        com.example.trainreservationsystem.models.User user = null;
        if (dbInitialized) {
            // Try to get user from database
            user = new com.example.trainreservationsystem.repositories.UserRepository()
                    .getUserByUsername("demo");
        }

        // If database failed or user not found, create mock user
        if (user == null) {
            System.out.println("ℹ️  Using mock user for demo");
            user = new com.example.trainreservationsystem.models.User(1, "demo", "demo123", "demo@example.com");
        }

        com.example.trainreservationsystem.services.UserSession.getInstance().login(user);

        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/home-view.fxml"));

        Scene scene = new Scene(root, 1380, 780);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }
}
