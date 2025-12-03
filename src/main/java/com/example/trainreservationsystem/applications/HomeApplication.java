package com.example.trainreservationsystem.applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        com.example.trainreservationsystem.utils.database.DatabaseInitializer.initialize();

        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Database.closeConnection() call removed as per instruction
        }));

        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/landing-view.fxml"));

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }
}
