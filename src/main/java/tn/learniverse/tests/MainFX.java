package tn.learniverse.tests;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;


import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.software.os.OperatingSystem;
public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DisplayReclamationBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file at /Reclamation/Back.fxml");
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Learniverse");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

//        SystemInfo systemInfo = new SystemInfo();
//        ComputerSystem cs = systemInfo.getHardware().getComputerSystem();
//        OperatingSystem os = systemInfo.getOperatingSystem();
//        String manufacturer = cs.getManufacturer();
//        String model = cs.getModel();
//        String serialNumber = cs.getSerialNumber();
//
//        String osName = os.toString();
//        String deviceType = "Desktop"; // JavaFX usually = desktop
//
//        System.out.println("Manufacturer: " + manufacturer);
//        System.out.println("Model: " + model);
//        System.out.println("Serial: " + serialNumber);
//        System.out.println("OS: " + osName);




        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Image icon = new Image(new FileInputStream("C:/wamp64/www/images/logo/logo.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Learniverse");
        primaryStage.show();
    }
}
