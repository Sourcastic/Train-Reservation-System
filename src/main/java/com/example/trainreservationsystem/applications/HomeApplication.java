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
        // Commenting out DB init for now to run on dummy data only
        // com.example.trainreservationsystem.utils.DatabaseInitializer.initialize();

        // Mock login since we aren't hitting the DB
        com.example.trainreservationsystem.models.User user = new com.example.trainreservationsystem.models.User(1,
                "demo", "demo123", "demo@example.com");
        com.example.trainreservationsystem.services.UserSession.getInstance().login(user);

        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/respondtocomplaints-view.fxml"));

        Scene scene = new Scene(root, 1380, 780);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }
}
