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
    requires AnimateFX;
    
    // Icon Libraries
    requires de.jensd.fx.glyphs.commons;
    requires de.jensd.fx.glyphs.fontawesome;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    // Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    // ZXing QR Code
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // Spring Framework
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.web;
    requires spring.messaging;
    requires spring.websocket;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    // iText PDF
    requires kernel;
    requires layout;
    requires io;

    // Apache POI
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // AWS SDK
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.http;
    requires software.amazon.awssdk.services.s3;

    // Email and Messaging
    requires jakarta.mail;
    requires thymeleaf;
    requires twilio;
    requires stripe.java;

    // Lombok
    requires static lombok;

    // Open packages to FXML
    opens tn.learniverse to javafx.fxml, com.jfoenix;
    opens tn.learniverse.util to javafx.fxml, com.jfoenix;
    opens tn.learniverse.controller to javafx.fxml, com.jfoenix;
    opens tn.learniverse.model to javafx.fxml, com.jfoenix;
    opens tn.learniverse.dao to javafx.fxml;
    opens tn.learniverse.service to javafx.fxml;
    opens tn.learniverse.config to javafx.fxml;

    // Exports
    exports tn.learniverse;
    exports tn.learniverse.util;
    exports tn.learniverse.controller;
    exports tn.learniverse.model;
    exports tn.learniverse.dao;
    exports tn.learniverse.service;
    exports tn.learniverse.config;
}