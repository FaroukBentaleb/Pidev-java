package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.codec.binary.Base32;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.software.os.OperatingSystem;
import tn.learniverse.entities.Logs;
import tn.learniverse.entities.TimeBasedOneTimePasswordGenerator;
import tn.learniverse.entities.User;
import tn.learniverse.services.LogsService;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class VeriyCode implements Initializable {
    public ImageView logoImageView;
    public TextField Code;
    public Label CodeError;

    public void ProceedBtn(ActionEvent actionEvent) {
        try {
            String inputCode = Code.getText().trim();

            if (inputCode.isEmpty()) {
                CodeError.setText("❌ Please enter the 2FA code.");
                CodeError.setStyle("-fx-text-fill: red;");
                return;
            }

            User currentUser = Session.getCurrentUser();
            String secretBase32 = currentUser.getGoogleAuthenticatorSecret();

            if (secretBase32 == null || secretBase32.isEmpty()) {
                CodeError.setText("❌ No secret key found.");
                CodeError.setStyle("-fx-text-fill: red;");
                return;
            }

            byte[] keyBytes = new Base32().decode(secretBase32);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
            int generatedCode = totp.generateOneTimePassword(secretKey, Instant.now());

            if (String.valueOf(generatedCode).equals(inputCode)) {
                // Save secret permanently
                new UserService().GoogleAuthStore(secretBase32);

                CodeError.setText("✅ Two Factor Authentication Enabled");
                CodeError.setStyle("-fx-text-fill: green;");
                Code.setStyle("");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✅ Login approved");
                alert.setHeaderText("Welcome!");
                alert.setContentText("Connected successfully");
                alert.showAndWait();
                saveLogs();
                Navigator.redirect(actionEvent, "/fxml/homePage.fxml");

            } else {
                CodeError.setText("❌ Invalid code");
                CodeError.setStyle("-fx-text-fill: red;");
                Code.setStyle("-fx-border-color: red;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            CodeError.setText("❌ Error validating 2FA code.");
            CodeError.setStyle("-fx-text-fill: red;");
        }
    }

    public void Backbtn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoImageView.setImage(new Image("file:///C:/wamp64/www/images/logo/logo.png"));
    }
    public static String getLocation() {
        try {
            URL url = new URL("http://ip-api.com/line/?fields=city,country");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String city = in.readLine();
            String country = in.readLine();
            in.close();
            return city + ", " + country;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown Location";
        }
    }
    private void saveLogs(){
        LogsService logs = new LogsService();
        SystemInfo systemInfo = new SystemInfo();
        ComputerSystem cs = systemInfo.getHardware().getComputerSystem();
        OperatingSystem os = systemInfo.getOperatingSystem();
        String osInfo = os.toString();
        String getlocation = getLocation();
        UserService userService = new UserService();
        Logs log = new Logs(
                userService.getUserIdByEmail(Session.getCurrentUser().getEmail()),
                "LOGIN",
                LocalDateTime.now(),
                cs.getSerialNumber(),
                cs.getModel(),
                osInfo,
                getlocation,
                0
        );
        logs.addLog(log);
        Session.setCurrentLog(logs.getLogsByUserId(userService.getUserIdByEmail(Session.getCurrentUser().getEmail())).get(0));
    }
}
