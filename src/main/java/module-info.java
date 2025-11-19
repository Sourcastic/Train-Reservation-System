module com.example.trainreservationsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.trainreservationsystem to javafx.fxml;
    exports com.example.trainreservationsystem;
    exports com.example.trainreservationsystem.applications;
    opens com.example.trainreservationsystem.applications to javafx.fxml;
    exports com.example.trainreservationsystem.controllers;
    opens com.example.trainreservationsystem.controllers to javafx.fxml;
}