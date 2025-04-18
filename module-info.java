module com.learniverse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.web;
    
    // UI Libraries
    requires com.jfoenix;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires animatefx;
    
    // Icon Libraries
    requires de.jensd.fx.glyphs.commons;
    requires de.jensd.fx.glyphs.fontawesome;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    // Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    // Open packages to FXML
    opens com.learniverse to javafx.fxml, com.jfoenix;
    opens com.learniverse.util to javafx.fxml, com.jfoenix;
    opens com.learniverse.controller to javafx.fxml, com.jfoenix;
    opens com.learniverse.model to javafx.fxml, com.jfoenix;
    opens com.learniverse.dao to javafx.fxml;
    opens com.learniverse.service to javafx.fxml;

    // Exports
    exports com.learniverse;
    exports com.learniverse.util;
    exports com.learniverse.controller;
    exports com.learniverse.model;
    exports com.learniverse.dao;
    exports com.learniverse.service;
} 