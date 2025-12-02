module com.example.trainreservationsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;

    exports com.example.trainreservationsystem.applications;

    opens com.example.trainreservationsystem.applications to javafx.fxml;

    exports com.example.trainreservationsystem.controllers;
    exports com.example.trainreservationsystem.controllers.booking;
    exports com.example.trainreservationsystem.controllers.payment;
    exports com.example.trainreservationsystem.controllers.search;
    exports com.example.trainreservationsystem.services;
    exports com.example.trainreservationsystem.services.booking;
    exports com.example.trainreservationsystem.services.payment;
    exports com.example.trainreservationsystem.models;
    exports com.example.trainreservationsystem.utils.booking;
    exports com.example.trainreservationsystem.utils.payment;
    exports com.example.trainreservationsystem.utils.ui;
    exports com.example.trainreservationsystem.utils.database;
    exports com.example.trainreservationsystem.repositories;

    opens com.example.trainreservationsystem.controllers to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.booking to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.payment to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.search to javafx.fxml;
}
