package com.example.trainreservationsystem.applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HomeApplication.class.getResource("home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1340, 780);
        stage.setMinHeight(780);
        stage.setMinWidth(1340);
        stage.setTitle("Train Reservation System");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
