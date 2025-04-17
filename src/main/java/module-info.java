module com.esprit.java {

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.web;
    requires MaterialFX;
    requires jdk.jfr;
    exports com.esprit.java.controllers to javafx.fxml;
    opens com.esprit.java.controllers to javafx.fxml;
    opens com.esprit.java to javafx.fxml;
    exports com.esprit.java;
    
    // Open Models package to allow JavaFX to access bean properties
    opens com.esprit.java.Models to javafx.base, javafx.fxml;
    exports com.esprit.java.Models to javafx.base;
}