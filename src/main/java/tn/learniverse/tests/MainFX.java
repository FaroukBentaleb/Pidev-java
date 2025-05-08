package tn.learniverse.tests;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.software.os.OperatingSystem;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.PasswordResetServer;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

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
        PasswordResetServer.startServer();
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) {
            String token = args.get(0);
            System.out.println("Received token: " + token);
            Navigator.redirect(new ActionEvent(),"/fxml/user/ResetPwd.fxml");
        } else {
            System.out.println("No token provided, loading login screen...");
            // load login screen
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackCourses.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Image icon = new Image(new FileInputStream("C:/wamp64/www/images/logo/logo.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Learniverse");
        primaryStage.show();
    }
    @Override
    public void stop() throws Exception {
        PasswordResetServer.stopServer();
        super.stop();
    }
}
