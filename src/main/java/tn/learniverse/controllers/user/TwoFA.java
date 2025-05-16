package tn.learniverse.controllers.user;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import tn.learniverse.entities.TimeBasedOneTimePasswordGenerator;
import tn.learniverse.entities.User;
import tn.learniverse.services.UserService;
import org.apache.commons.codec.binary.Base32;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TwoFA implements Initializable {

    public TextField Code;
    public Button confirm2FAButton;
    public ImageView qrCodeImageView;
    public Button logoutButton;
    public Button Settingsbtn;
    public Label role;
    public Label usernameLabel;
    public ImageView UserPicture;
    public ToggleButton Enable2FA;
    public Label CodeError;
    private String AuthKey;

    // This is the actual method for verifying 2FA code
    public void handleConfirm2FA(ActionEvent actionEvent) {
        try {
            String inputCode = Code.getText().trim();

            if (inputCode.isEmpty()) {
                CodeError.setText("❌ Please enter the 2FA code.");
                CodeError.setStyle("-fx-text-fill: red;");
                return;
            }

            User currentUser = Session.getCurrentUser();
            String secretBase32 = AuthKey;

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
                currentUser.setGoogleAuthenticatorSecret(AuthKey);
                // Save secret permanently
                new UserService().GoogleAuthStore(secretBase32);

                CodeError.setText("✅ Two Factor Authentication Enabled");
                CodeError.setStyle("-fx-text-fill: green;");

                Enable2FA.setText("Disable 2FA");
                Enable2FA.setSelected(true);

                qrCodeImageView.setVisible(false);
                qrCodeImageView.setManaged(false);
                Code.setVisible(false);
                Code.setManaged(false);
                confirm2FAButton.setVisible(false);
                confirm2FAButton.setManaged(false);
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


    private Image generateQRCodeImage(String text, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int grayValue = (bitMatrix.get(x, y) ? 0 : 255);
                    bufferedImage.setRGB(x, y, (grayValue << 16) | (grayValue << 8) | grayValue);
                }
            }

            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateSecret() {
        if (Session.getCurrentUser().getGoogleAuthenticatorSecret() == null) {
            SecureRandom secureRandom = new SecureRandom();
            byte[] secretBytes = new byte[16];
            secureRandom.nextBytes(secretBytes);

            Base32 base32 = new Base32();
            return base32.encodeAsString(secretBytes);
        }
        else{
            return Session.getCurrentUser().getGoogleAuthenticatorSecret();
        }

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/logout.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.logoutButton.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/settings.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Settingsbtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            if (Session.getCurrentUser() == null) {
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event, "/fxml/user/Login.fxml");
            } else {
                this.usernameLabel.setText(Session.getCurrentUser().getNom());
                this.role.setText(Session.getCurrentUser().getRole());
                String picturePath = Session.getCurrentUser().getPicture();
                Image image;

                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 50, 50, false, false);
                    if (image.isError()) {
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                    }
                } else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                }

                this.UserPicture.setImage(image);
                Circle clip = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        User currentUser = Session.getCurrentUser();
        String existingSecret = currentUser.getGoogleAuthenticatorSecret();

        if (existingSecret != null && !existingSecret.isEmpty()) {
            Enable2FA.setVisible(true);
            Enable2FA.setText("Disable 2FA");
            Enable2FA.setSelected(true);
            CodeError.setStyle("-fx-text-fill: green;");
            CodeError.setText("✅ Two Factor Authentication Enabled");

            // Hide input fields
            qrCodeImageView.setVisible(false);
            qrCodeImageView.setManaged(false);
            Code.setVisible(false);
            Code.setManaged(false);
            confirm2FAButton.setVisible(false);
            confirm2FAButton.setManaged(false);
        } else {
            qrCodeImageView.setVisible(false);
            qrCodeImageView.setManaged(false);
            Code.setVisible(false);
            Code.setManaged(false);
            confirm2FAButton.setVisible(false);
            confirm2FAButton.setManaged(false);
            Enable2FA.setVisible(true);
            Enable2FA.setText("Enable 2FA");
            Enable2FA.setSelected(false);
            CodeError.setText("");
        }
    }



    public void Logs(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/LogsList.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Settings.fxml");
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }
    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Settings.fxml");
    }

    public void Enable2FA(ActionEvent actionEvent) {
        UserService userService = new UserService();
        User currentUser = Session.getCurrentUser();

        if (!Enable2FA.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Disable 2FA");
            alert.setHeaderText("Are you sure you want to disable 2FA?");
            alert.setContentText("This will remove the 2-factor authentication from your account.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Clear from DB and session
                try {
                    userService.GoogleAuthStore(null);
                    currentUser.setGoogleAuthenticatorSecret(null);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Enable2FA.setText("Enable 2FA");
                CodeError.setText("");
                qrCodeImageView.setVisible(false);
                qrCodeImageView.setManaged(false);
                Code.setVisible(false);
                Code.setManaged(false);
                confirm2FAButton.setVisible(false);
                confirm2FAButton.setManaged(false);
            } else {
                Enable2FA.setSelected(true);
            }
        } else {
            // Enable clicked: generate new secret, show UI
            String newSecret = generateSecret();
            String qrText = String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    "Learniverse", currentUser.getEmail(), newSecret, "Learniverse"
            );
            Image qrImage = generateQRCodeImage(qrText, 200, 200);
            qrCodeImageView.setImage(qrImage);

            AuthKey = newSecret; // TEMPORARY until confirmed

            Enable2FA.setText("Disable 2FA");
            CodeError.setText("");

            qrCodeImageView.setVisible(true);
            qrCodeImageView.setManaged(true);
            Code.setVisible(true);
            Code.setManaged(true);
            confirm2FAButton.setVisible(true);
            confirm2FAButton.setManaged(true);
        }
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/competitions_list.fxml");

    }

    public void ToReclamaitons(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamations.fxml");
    }

    public void ToForum(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/AfficherPoste.fxml");
    }

    public void ToDiscover(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        if(Session.getCurrentUser().getRole().equals("Student")){
            Navigator.redirect(actionEvent,"/SubscriptionCourses.fxml");
        }
        else{
            Navigator.redirect(actionEvent,"/MyCourses.fxml");
        }
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/OffreDisplay.fxml");

    }


}
