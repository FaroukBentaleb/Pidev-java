package tn.learniverse.tools;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.learniverse.services.PasswordResetService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import javafx.stage.Window;

/**
 * Simple HTTP server to handle password reset requests
 * This server listens for requests to the reset-password endpoint and opens the JavaFX app
 */
public class PasswordResetServer {
    private static HttpServer server;

    /**
     * Start the password reset server
     */
    public static void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/reset-password", new ResetPasswordHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Password reset server started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the password reset server
     */
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Handler for password reset requests
     */
    static class ResetPasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the query parameters
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);

            // Get the token
            String token = params.get("token");

            // Validate the token
            String email = PasswordResetService.validateToken(token);

            if (email != null) {
                // Token is valid, open the JavaFX app's reset password screen
                // In a real application, you would need to find a way to communicate
                // between this server and your JavaFX application

                // For now, we'll just send a redirect to a success page
                String response = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta http-equiv=\"refresh\" content=\"5;url=http://localhost:8080/\" />" +
                        "<title>Password Reset</title>" +
                        "<style>" +
                        "body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #f6f8fc 0%, #e9effd 100%); }" +
                        ".container { text-align: center; max-width: 600px; padding: 40px; background-color: white; border-radius: 20px; box-shadow: 0 10px 25px rgba(98,0,234,0.1); }" +
                        "h1 { color: #1a202c; }" +
                        "p { color: #4a5568; line-height: 1.6; }" +
                        ".icon { font-size: 48px; margin-bottom: 20px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class=\"container\">" +
                        "<div class=\"icon\">✅</div>" +
                        "<h1>Valid Reset Link</h1>" +
                        "<p>Opening Learniverse application to reset your password...</p>" +
                        "<p>If the application doesn't open automatically, please open it manually and enter the following token:</p>" +
                        "<p><strong>" + token + "</strong></p>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

                // In a real application, you would launch your JavaFX app here
                // or communicate with it if it's already running
                openResetPasswordScreen(token);

            } else {
                // Token is invalid, send an error page
                String response = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta http-equiv=\"refresh\" content=\"5;url=http://localhost:8080/\" />" +
                        "<title>Invalid Password Reset</title>" +
                        "<style>" +
                        "body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #f6f8fc 0%, #e9effd 100%); }" +
                        ".container { text-align: center; max-width: 600px; padding: 40px; background-color: white; border-radius: 20px; box-shadow: 0 10px 25px rgba(98,0,234,0.1); }" +
                        "h1 { color: #1a202c; }" +
                        "p { color: #4a5568; line-height: 1.6; }" +
                        ".icon { font-size: 48px; margin-bottom: 20px; color: #dc2626; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class=\"container\">" +
                        "<div class=\"icon\">❌</div>" +
                        "<h1>Invalid or Expired Link</h1>" +
                        "<p>The password reset link you clicked is invalid or has expired.</p>" +
                        "<p>Please request a new password reset link.</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return params;
        }

        private void openResetPasswordScreen(String token) {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/ResetPwd.fxml"));
                    Parent root = loader.load();

                    // Get the current (first visible) window
                    Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
                    stage.setScene(new Scene(root));
                    stage.setTitle("Reset Password");
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }
}