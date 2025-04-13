module com.esprit.java {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.web;
    exports com.esprit.java.controllers to javafx.fxml;
    opens com.esprit.java.controllers to javafx.fxml;
    opens com.esprit.java to javafx.fxml;
    exports com.esprit.java;
}