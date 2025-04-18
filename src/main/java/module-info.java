module com.learniverse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.web;
    
    // FontAwesome dependencies
    requires de.jensd.fx.glyphs.commons;
    requires de.jensd.fx.glyphs.fontawesome;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    opens com.learniverse to javafx.fxml;
    opens com.learniverse.util to javafx.fxml;
    opens com.learniverse.controller to javafx.fxml;
    opens com.learniverse.model to javafx.fxml;
    opens com.learniverse.dao to javafx.fxml;

    exports com.learniverse;
    exports com.learniverse.util;
    exports com.learniverse.controller;
    exports com.learniverse.model;
    exports com.learniverse.dao;
}