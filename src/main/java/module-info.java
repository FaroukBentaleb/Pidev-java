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
    requires javafx.graphics;
    
    // Make sure tools package is properly exported and opened
    opens tn.learniverse.controllers.Competition to javafx.fxml;
    opens tn.learniverse to javafx.fxml;
    opens tn.learniverse.tools to javafx.fxml, javafx.base, javafx.controls, javafx.web;
    opens tn.learniverse.entities to javafx.base, javafx.fxml;
    
    exports tn.learniverse;
    exports tn.learniverse.controllers.Competition to javafx.fxml;
    exports tn.learniverse.tools to javafx.fxml, javafx.base, javafx.controls, javafx.web;
}