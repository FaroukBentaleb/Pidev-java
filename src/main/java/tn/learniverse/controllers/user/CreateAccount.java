package tn.learniverse.controllers.user;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.software.os.OperatingSystem;
import tn.learniverse.entities.*;
import tn.learniverse.services.*;
import tn.learniverse.tools.Navigator;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import org.mindrot.jbcrypt.BCrypt;
import tn.learniverse.tools.Session;

public class CreateAccount implements Initializable {

    private final UserService userService = new UserService();
    public Button login_btn;
    public ImageView logoImageView;
    public TextField textField;
    public CheckBox showPasswordCheckBox;
    public TextField textFieldconf;

    @FXML
    private TextField signup_email;

    @FXML
    private TextField signup_family_name;

    @FXML
    private TextField signup_name;

    @FXML
    private ChoiceBox<String> signup_role;

    @FXML
    private PasswordField  signup_conf_pwd;

    @FXML
    private PasswordField  signup_pwd;

    @FXML
    private Label error_name;

    @FXML
    private Label error_family_name;

    @FXML
    private Label error_email;

    @FXML
    private Label error_pwd;

    @FXML
    private Label error_conf_pwd;

    @FXML
    void Signup_btn(ActionEvent event) {
        if (signup_pwd.getText().equals(signup_conf_pwd.getText())){
            boolean isValid = true;
            String name = signup_name.getText().trim();
            String familyName = signup_family_name.getText().trim();
            String email = signup_email.getText().trim();
            String role = signup_role.getValue();
            String pwd = signup_pwd.getText();
            String confPwd = signup_conf_pwd.getText();
            if (name.isEmpty() || familyName.isEmpty() || email.isEmpty() || role == null || pwd.isEmpty() || confPwd.isEmpty()) {
                ValidateSignup();
                return;
            }

            // Proceed only if all valid
            if (isValid) {
                try {
                    String hashedPassword = BCrypt.hashpw(pwd, BCrypt.gensalt());
                    System.out.println("pwd: " + pwd);
                    System.out.println("hashedPassword: " + hashedPassword);
                    userService.CreateAccount(new User(name,familyName,email,role,hashedPassword));
                    Navigator.showAlert(Alert.AlertType.INFORMATION, "Account Created Successfully", "You can proceed to logging in");
                    Navigator.redirect(event, "/fxml/user/Login.fxml");
                }
                catch (SQLException e){
                    Navigator.showAlert(Alert.AlertType.ERROR, "Error creating account", "Something went wrong");
                }
            }
        }
        else{
            Navigator.showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The passwords you entered do not match. Please try again.");

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoImageView.setImage(new Image("file:///C:/wamp64/www/images/logo/logo.png"));
        signup_role.getItems().addAll("Student", "Insctructor");
        signup_role.setValue("Student");
        textField.textProperty().bindBidirectional(signup_pwd.textProperty());
        textFieldconf.textProperty().bindBidirectional(signup_conf_pwd.textProperty());

        // Toggle visibility
        showPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                textField.setVisible(true);
                textField.setManaged(true);
                signup_pwd.setVisible(false);
                signup_pwd.setManaged(false);

                textFieldconf.setVisible(true);
                textFieldconf.setManaged(true);
                signup_conf_pwd.setVisible(false);
                signup_conf_pwd.setManaged(false);
            } else {
                signup_pwd.setVisible(true);
                signup_pwd.setManaged(true);
                textField.setVisible(false);
                textField.setManaged(false);

                signup_conf_pwd.setVisible(true);
                signup_conf_pwd.setManaged(true);
                textFieldconf.setVisible(false);
                textFieldconf.setManaged(false);
            }
        });
        setupRealTimeValidation();
    }

    public void Login_btn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
    }
    private void setupRealTimeValidation() {
        // Name
        signup_name.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                signup_name.setStyle("-fx-border-color: red;");
                error_name.setText("Name must be at least 2 characters");
            } else {
                signup_name.setStyle(null);
                error_name.setText("");
            }
        });

        // Family Name
        signup_family_name.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                signup_family_name.setStyle("-fx-border-color: red;");
                error_family_name.setText("Family name must be at least 2 characters");
            } else {
                signup_family_name.setStyle(null);
                error_family_name.setText("");
            }
        });

        // Email
        signup_email.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                signup_email.setStyle("-fx-border-color: red;");
                error_email.setText("Invalid email format");
            } else {
                signup_email.setStyle(null);
                error_email.setText("");
            }
        });

        // Password
        signup_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
                signup_pwd.setStyle("-fx-border-color: red;");
                textField.setStyle("-fx-border-color: red;");
                error_pwd.setText("8+ characters (upper & lower case, special char)");
            } else {
                signup_pwd.setStyle(null);
                textField.setStyle(null);
                error_pwd.setText("");
            }
        });

        // Confirm Password
        signup_conf_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(signup_pwd.getText())) {
                signup_conf_pwd.setStyle("-fx-border-color: red;");
                textFieldconf.setStyle("-fx-border-color: red;");
                error_conf_pwd.setText("Passwords do not match");
            } else {
                signup_conf_pwd.setStyle(null);
                textFieldconf.setStyle(null);
                error_conf_pwd.setText("");
            }
        });

        // Sync confirm password check when password field changes
        signup_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            String conf = signup_conf_pwd.getText();
            if (!conf.equals(newVal)) {
                signup_conf_pwd.setStyle("-fx-border-color: red;");
                error_conf_pwd.setText("Passwords do not match");
            } else {
                signup_conf_pwd.setStyle(null);
                error_conf_pwd.setText("");
            }
        });
    }
    private void ValidateSignup() {
        if(signup_name.getText().length()<2){
            signup_name.setStyle("-fx-border-color: red;");
            error_name.setText("Name must be at least 2 characters");
        }
        else{
            signup_name.setStyle(null);
            error_name.setText("");
        }
        if(signup_family_name.getText().length()<2){
            signup_family_name.setStyle("-fx-border-color: red;");
            error_family_name.setText("Family Name must be at least 2 characters");
        }
        else{
            signup_family_name.setStyle(null);
            error_family_name.setText("");
        }
        if(!signup_email.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")){
            signup_email.setStyle("-fx-border-color: red;");
            error_email.setText("Invalid email format");
        }
        else{
            signup_email.setStyle(null);
            error_family_name.setText("");
        }
        if(!signup_pwd.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")){
            signup_pwd.setStyle("-fx-border-color: red;");
            textField.setStyle("-fx-border-color: red;");
            error_pwd.setText("8+ characters (upper & lower case, special char)");
        }
        else{
            signup_pwd.setStyle(null);
            textField.setStyle(null);
            error_pwd.setText("");
        }
        if(!signup_conf_pwd.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")){
            signup_conf_pwd.setStyle("-fx-border-color: red;");
            textFieldconf.setStyle("-fx-border-color: red;");
            error_conf_pwd.setText("Passwords do not match");
        }
        else{
            signup_conf_pwd.setStyle(null);
            textFieldconf.setStyle(null);
            error_conf_pwd.setText("");
        }
    }

    public void Google_btn(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                Credential credential = Login.GoogleAuthUtil.authorize();
                if (credential != null) {
                    String email = fetchUser(credential).getString("email");

                    Platform.runLater(() -> {
                        try {
                            UserService userService = new UserService();
                            User usr = userService.getUserByEmail(email);

                            if (usr.getEmail() != null) {
                                if (usr.getBan() == 1) {
                                    Navigator.showAlert(Alert.AlertType.ERROR, "Account Banned",
                                            "Your account has been Banned by the admin.\nPlease contact support for more details!");
                                } else if (usr.getLogs() == 0) {
                                    Navigator.showAlert(Alert.AlertType.ERROR, "Account locked",
                                            "Your account has been locked due to many login attempts.\nPlease reset your password or contact support for more details!");
                                } else {
                                    Session.setCurrentUser(usr);
                                    saveLogs();
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Login Successful");
                                    alert.setHeaderText("Welcome, " + usr.getNom() + "!");
                                    alert.setContentText("Connected successfully via Google");
                                    alert.showAndWait();

                                    if (Session.getCurrentUser().getRole().equals("Admin")) {
                                        Navigator.redirect(actionEvent, "/fxml/Back.fxml");
                                    } else {
                                        Navigator.redirect(actionEvent, "/fxml/homePage.fxml");
                                    }
                                }
                            } else {

                                usr.setNom(fetchUser(credential).getString("given_name"));
                                usr.setPrenom(fetchUser(credential).getString("family_name"));
                                usr.setEmail(fetchUser(credential).getString("email"));
                                usr.setPicture(fetchUser(credential).getString("picture"));
                                Session.setCurrentUser(usr);
                                saveLogs();
                                System.out.println(fetchUser(credential));
                                System.out.println(usr);
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("New User");
                                alert.setHeaderText("Account not found");
                                alert.setContentText("No account found with email: " + email + "\nBut do not worry we will be helping you create one!");

                                ButtonType yesButton = new ButtonType("Proceed");

                                alert.getButtonTypes().setAll(yesButton);

                                alert.showAndWait();
                                Navigator.redirect(actionEvent, "/fxml/user/ConfirmPwd.fxml");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Login Error");
                            alert.setContentText("Error processing user data: " + e.getMessage());
                            alert.showAndWait();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setContentText("Google sign-in failed: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    public static JSONObject fetchUser(Credential credential) throws Exception {
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(
                    request -> request.setHeaders(request.getHeaders().set("Authorization", "Bearer " + credential.getAccessToken()))
            );

            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();

            String json = response.parseAsString();
            System.out.println("User info response: " + json);

            JSONObject userInfo = new JSONObject(json);
            if (userInfo.has("email")) {
                return userInfo;
            } else {
                throw new Exception("Email not found in the response: " + json);
            }
        } catch (Exception e) {
            System.err.println("Error fetching user email: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to fetch user info: " + e.getMessage());
        }
    }

    public static class GoogleAuthUtil {
        private static final String CREDENTIALS_FILE = "C:/wamp64/www/images/credentials/client_secret.json";
        private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/google_oauth_tokens");

        public static Credential authorize() throws Exception {
            try {
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                        JacksonFactory.getDefaultInstance(),
                        new FileReader(CREDENTIALS_FILE)
                );

                if (clientSecrets.getDetails().getClientId().startsWith("YOUR") ||
                        clientSecrets.getDetails().getClientSecret().startsWith("YOUR")) {
                    System.err.println("Client secrets not properly configured in: " + CREDENTIALS_FILE);
                    throw new Exception("Client secrets not properly configured. Check credentials file.");
                }

                if (!DATA_STORE_DIR.exists()) {
                    DATA_STORE_DIR.mkdirs();
                }

                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        clientSecrets,
                        List.of("email", "profile")
                )
                        .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                        .setAccessType("offline")
                        .setApprovalPrompt("force")
                        .build();

                LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                        .setPort(0)
                        .setHost("localhost")
                        .build();

                System.out.println("Starting Google authorization process...");

                flow.getCredentialDataStore().clear();

                return new AuthorizationCodeInstalledApp(flow, receiver,
                        new AuthorizationCodeInstalledApp.Browser() {
                            @Override
                            public void browse(String url) {
                                if (!url.contains("prompt=")) {
                                    url += (url.contains("?") ? "&" : "?") + "prompt=select_account";
                                }
                                System.out.println("Please open the following URL in your browser: " + url);
                                try {
                                    Desktop.getDesktop().browse(new URI(url));
                                } catch (Exception e) {
                                    System.out.println("Unable to open browser" + e.getMessage());
                                }
                            }
                        }).authorize("user");
            } catch (Exception e) {
                System.err.println("Authorization error: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }




    public void Github_btn(ActionEvent actionEvent) {
        String clientId = "Ov23limTtxB1KtyLigav";
        String redirectUri = "http://localhost:9001/github-callback";
        String state = generateRandomState();

        String authUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state +
                "&scope=user:email"+
                "&prompt=login";;

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(9001);

                Desktop.getDesktop().browse(new URI(authUrl));

                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                String code = null;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("GET")) {
                        String[] parts = line.split(" ")[1].split("\\?");
                        if (parts.length > 1) {
                            String query = parts[1];
                            String[] params = query.split("&");
                            for (String param : params) {
                                String[] pair = param.split("=");
                                if (pair.length == 2 && pair[0].equals("code")) {
                                    code = pair[1];
                                    break;
                                }
                            }
                        }
                    }

                    if (line.isEmpty()) {
                        break;
                    }
                }

                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>Authentication successful!</h1><p>You can close this window now.</p></body></html>");
                out.flush();
                out.close();
                socket.close();
                serverSocket.close();

                if (code != null) {
                    String accessToken = exchangeCodeForToken(code, clientId, "3792722bbcee600a10dc667668fbd7c47f6c4ed9", redirectUri);

                    String email = fetchGitHubUserEmail(accessToken).getString("email");
                    String fullName = fetchGitHubUser(accessToken).optString("name", "");
                    String[] parts = fullName.trim().split(" ", 2);
                    String prenom = parts.length > 0 ? parts[0] : "";
                    String nom = parts.length > 1 ? parts[1] : "";
                    String picture = fetchGitHubUser(accessToken).getString("avatar_url");

                    Platform.runLater(() -> {
                        processGitHubLogin(email, nom, prenom, picture, actionEvent);
                    });
                } else {
                    throw new Exception("No authorization code received from GitHub");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("GitHub Login Failed");
                    alert.setContentText("GitHub sign-in failed: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private String generateRandomState() {
        return java.util.UUID.randomUUID().toString();
    }

    private String exchangeCodeForToken(String code, String clientId, String clientSecret, String redirectUri) throws Exception {
        URL url = new URL("https://github.com/login/oauth/access_token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String params = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&redirect_uri=" + redirectUri;

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = params.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("access_token");
            }
        } else {
            throw new Exception("Failed to get GitHub access token, response code: " + responseCode);
        }
    }

    private JSONObject fetchGitHubUserEmail(String accessToken) throws Exception {
        URL url = new URL("https://api.github.com/user/emails");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "token " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                org.json.JSONArray emails = new org.json.JSONArray(response.toString());

                for (int i = 0; i < emails.length(); i++) {
                    JSONObject emailObj = emails.getJSONObject(i);
                    if (emailObj.optBoolean("primary", false)) {
                        return emailObj;
                    }
                }

                if (emails.length() > 0) {
                    return emails.getJSONObject(0);
                }

                throw new Exception("No email found in GitHub response");
            }
        } else {
            throw new Exception("Failed to get GitHub user emails, response code: " + responseCode);
        }
    }
    private JSONObject fetchGitHubUser(String accessToken) throws Exception {
        URL url = new URL("https://api.github.com/user");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "token " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                return new JSONObject(response.toString());
            }
        } else {
            throw new Exception("Failed to get GitHub user profile, response code: " + responseCode);
        }
    }


    private void processGitHubLogin(String email,String nom,String prenom,String picture, ActionEvent actionEvent) {
        try {
            UserService userService = new UserService();
            User usr = userService.getUserByEmail(email);

            if (usr.getEmail() != null) {
                System.out.println("github:" +usr);
                if (usr.getBan() == 1) {
                    Navigator.showAlert(Alert.AlertType.ERROR, "Account Banned",
                            "Your account has been Banned by the admin.\nPlease contact support for more details!");
                } else if (usr.getLogs() == 0) {
                    Navigator.showAlert(Alert.AlertType.ERROR, "Account locked",
                            "Your account has been locked due to many login attempts.\nPlease reset your password or contact support for more details!");
                } else {
                    Session.setCurrentUser(usr);
                    saveLogs();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Login Successful");
                    alert.setHeaderText("Welcome, " + usr.getNom() + "!");
                    alert.setContentText("Connected successfully via GitHub");
                    alert.showAndWait();

                    if (Session.getCurrentUser().getRole().equals("Admin")) {
                        Navigator.redirect(actionEvent, "/fxml/Back.fxml");
                    } else {
                        Navigator.redirect(actionEvent, "/fxml/homePage.fxml");
                    }
                }
            } else {
                usr.setNom(nom);
                usr.setPrenom(prenom);
                usr.setEmail(email);
                usr.setPicture(picture);
                Session.setCurrentUser(usr);
                saveLogs();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("New User");
                alert.setHeaderText("Account not found");
                alert.setContentText("No account found with email: " + email + "\nBut do not worry we will be helping you create one!");

                ButtonType yesButton = new ButtonType("Proceed");

                alert.getButtonTypes().setAll(yesButton);

                alert.showAndWait();
                Navigator.redirect(actionEvent, "/fxml/user/ConfirmPwd.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setContentText("Error processing user data: " + e.getMessage());
            alert.showAndWait();
        }
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
