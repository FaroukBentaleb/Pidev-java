package tn.learniverse.services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailService {

    public static void sendMail(String toEmail, String subject, String body) {
        final String fromEmail = "bytequest.pro@gmail.com";
        final String password = "wlmzixzfblxlzmsc";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email envoyé à : " + toEmail);
        } catch (MessagingException e) {
            System.out.println("Erreur d'envoi de mail : " + e.getMessage());
        }
    }
}
