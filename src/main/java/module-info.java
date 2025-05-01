module tn.learniverse {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires MaterialFX;
    requires javafx.web;
    requires java.net.http;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires twilio;
    requires org.slf4j;

    requires javafx.base;
    requires javafx.graphics;

    opens tn.learniverse to javafx.fxml;
    opens tn.learniverse.tools to javafx.fxml;
    opens tn.learniverse.controllers.Competition to javafx.fxml;

    opens tn.learniverse.entities to javafx.base, javafx.fxml;
    exports tn.learniverse;
    exports tn.learniverse.controllers.Competition to javafx.fxml;
    exports tn.learniverse.tools to javafx.fxml;
}