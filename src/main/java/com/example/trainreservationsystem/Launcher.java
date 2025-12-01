package com.example.trainreservationsystem;

import com.example.trainreservationsystem.applications.HomeApplication;
import com.example.trainreservationsystem.utils.StartupDataloader;
import javafx.application.Application;
import com.example.trainreservationsystem.utils.DatabaseInitializer;

public class Launcher {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        StartupDataloader.loadData();
        Application.launch(HomeApplication.class, args);
    }
}
