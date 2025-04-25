package tn.learniverse.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final String EMAIL = "bytequest.pro@gmail.com";
    private static final String PASSWORD = "wlmzixzfblxlzmsc";
    private static final String SENDER_NAME = "Learniverse";

    public static void sendEmail(String to, String subject, String content) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL, SENDER_NAME));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.addHeader("List-Unsubscribe", "<mailto:" + EMAIL + "?subject=unsubscribe>");
            message.addHeader("X-Priority", "1");
            message.addHeader("X-MSMail-Priority", "High");
            
            message.setSubject(subject);
            Multipart multipart = new MimeMultipart("alternative");
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(content.replaceAll("<[^>]*>", ""), "UTF-8");
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; line-height: 1.6;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff;">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h1 style="color: #2c3e50; margin: 0;">Learniverse</h1>
                            <p style="color: #7f8c8d; margin: 5px 0;">Plateforme d'apprentissage</p>
                        </div>
                        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px;">
                            %s
                        </div>
                        <div style="text-align: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee;">
                            <p style="color: #7f8c8d; font-size: 12px;">
                                Ceci est un message automatique de Learniverse.<br>
                                Merci de ne pas répondre à cet email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
            """, content);
            
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + to);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            throw new MessagingException("Erreur lors de l'envoi de l'email", e);
        }
    }
}