module com.example.trainreservationsystem {
    requires javafx.controls;
    requires javafx.graphics;
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
//    requires com.example.trainreservationsystem;

    exports com.example.trainreservationsystem.applications;

    opens com.example.trainreservationsystem.applications to javafx.fxml;

    // Controllers
    exports com.example.trainreservationsystem.controllers.admin;
    exports com.example.trainreservationsystem.controllers.member;
    exports com.example.trainreservationsystem.controllers.member.booking;
    exports com.example.trainreservationsystem.controllers.member.payment;
    exports com.example.trainreservationsystem.controllers.member.search;
    exports com.example.trainreservationsystem.controllers.shared;
    exports com.example.trainreservationsystem.controllers.shared.auth;
    exports com.example.trainreservationsystem.controllers.staff;

    opens com.example.trainreservationsystem.controllers.admin to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.member to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.member.booking to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.member.payment to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.member.search to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.shared to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.shared.auth to javafx.fxml;
    opens com.example.trainreservationsystem.controllers.staff to javafx.fxml;

    // Services
    exports com.example.trainreservationsystem.services.admin;
    exports com.example.trainreservationsystem.services.member;
    exports com.example.trainreservationsystem.services.member.booking;
    exports com.example.trainreservationsystem.services.member.payment;
    exports com.example.trainreservationsystem.services.shared;
    exports com.example.trainreservationsystem.services.staff;

    // Models
    exports com.example.trainreservationsystem.models.admin;
    exports com.example.trainreservationsystem.models.member;
    exports com.example.trainreservationsystem.models.member.booking;
    exports com.example.trainreservationsystem.models.shared;

    // Utils
    exports com.example.trainreservationsystem.utils.member.booking;
    exports com.example.trainreservationsystem.utils.shared.database;
    exports com.example.trainreservationsystem.utils.shared.payment;
    exports com.example.trainreservationsystem.utils.shared.payment.adapters;
    exports com.example.trainreservationsystem.utils.shared.ui;

    // Repositories
    exports com.example.trainreservationsystem.repositories;
    exports com.example.trainreservationsystem.repositories.admin;
    exports com.example.trainreservationsystem.repositories.member;
    exports com.example.trainreservationsystem.repositories.shared;
    exports com.example.trainreservationsystem.repositories.staff;
}
