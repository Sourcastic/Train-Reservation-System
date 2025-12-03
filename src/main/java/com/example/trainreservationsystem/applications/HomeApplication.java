package com.example.trainreservationsystem.applications;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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

        startAutomaticReminderJob();

        Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/trainreservationsystem/updatetrainstatus-view.fxml"));

        Scene scene = new Scene(root, 1380, 780);
        stage.setScene(scene);
        stage.setTitle("Train Reservation System");
        stage.show();
    }


//    method to send the automatic notifications as upcoming journey reminders
    private void startAutomaticReminderJob() {

        Timer timer = new Timer(true); // Daemon thread (stops when app closes)

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // This runs automatically every 12 hours
                com.example.trainreservationsystem.services.ServiceFactory
                        .getTrainService()
                        .sendTomorrowReminders();

                System.out.println("Reminder job executed: checking for tomorrow bookings...");
            }
        }, 0, 12 * 60 * 60 * 1000);  // Runs every 12 hours
    }
}
