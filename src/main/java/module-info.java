module tn.learniverse {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.material;
    requires java.sql;
    requires MaterialFX;
    requires javafx.web;

    // Add access to JavaFX standard modules
    requires com.github.oshi;
    requires jbcrypt;
    requires jakarta.validation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires java.net.http;
    requires org.slf4j;
    requires twilio;
    requires com.google.api.client.auth;
    requires google.http.client.jackson2;
    requires com.google.api.client;
    requires google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires org.json;
    requires java.mail;
    requires javafx.swing;
    requires org.apache.commons.codec;
    requires com.google.zxing;
    requires jdk.httpserver;

    // Make sure tools package is properly exported and opened
    opens tn.learniverse.controllers.Competition to javafx.fxml;
    opens tn.learniverse to javafx.fxml;
    opens tn.learniverse.tools to javafx.fxml, javafx.base, javafx.controls, javafx.web;
    opens tn.learniverse.entities to javafx.base, javafx.fxml;

    exports tn.learniverse;
    exports tn.learniverse.controllers.Competition to javafx.fxml;
    exports tn.learniverse.tools to javafx.fxml, javafx.base, javafx.controls, javafx.web;
    exports tn.learniverse.tests to javafx.graphics;
    exports tn.learniverse.controllers.user to javafx.fxml;
    opens tn.learniverse.controllers to javafx.fxml;
    opens tn.learniverse.controllers.user to javafx.fxml;


}