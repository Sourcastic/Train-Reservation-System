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
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/home-view.fxml")
        );

        Scene scene = new Scene(root, 1380, 780);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }
}
