package tn.learniverse.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.learniverse.entities.Competition;
import tn.learniverse.tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class schedular {

    private static final Logger logger = LoggerFactory.getLogger(schedular.class);
    // Replace with your Twilio credentials
    public static final String ACCOUNT_SID = "AC8f195fbcf57167841098e5aa6bad5912";
    public static final String AUTH_TOKEN = "f15f43d714c32816d80e2ed5652785fd";
    public static final String TWILIO_NUMBER = "+15178787542";

    public  CompetitionService competitionService;
    public schedular() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        this.competitionService = new CompetitionService(connection);
        startScheduler();
        System.out.println("Scheduler started");

    }


    private void startScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Competition> competitions = competitionService.getAllCompetitions();
                LocalDateTime now = LocalDateTime.now();

                for (Competition c : competitions) {

                    // 2 hours before start
                    if (!c.isNotification_sent2h() &&
                            now.isAfter(c.getDateComp().minusHours(2)) &&
                            now.isBefore(c.getDateComp().minusHours(2).plusMinutes(30))&&c.getEtat().equals("Planned")) {
                        System.out.println("Sending 2-hour notification for competition: " + c.getNom());
                        sendMessageToUsers(competitionService.SendNotification(c.getId()), "Your competition "+c.getNom()+ " starts in 2 hours!");
                        c.setNotification_sent2h(true);

                    }

                    // At start time
                    if (!c.isNotification_sent_start() &&
                            now.isAfter(c.getDateComp()) &&
                            now.isBefore(c.getDateComp().plusMinutes(2))&&c.getEtat().equals("Planned")) {
                        System.out.println("Sending start notification for competition: " + c.getNom());
                        sendMessageToUsers(competitionService.SendNotification(c.getId()), "Your competition "+c.getNom()+ "  has started!,give it a try");
                        c.setNotification_sent_start(true);
                        c.setEtat("InProgress");
                    }

                    // At end time
                    if (!c.isNotifiedEnd() &&
                            now.isAfter(c.getDateFin()) &&
                            now.isBefore(c.getDateFin().plusMinutes(2))&&c.getEtat().equals("InProgress")) {
                        System.out.println("Sending end notification for competition: " + c.getNom());
                        sendMessageToUsers(competitionService.SendNotification(c.getId()), "Your competition "+c.getNom()+ "  is now over.Go check your rank");
                        c.setNotifiedEnd(true);
                        c.setEtat("Completed");
                    }

                }

                competitionService.saveAll(competitions);
                System.out.println("Scheduler executed at: " + now);

            } catch (Exception e) {
                logger.error("Scheduler error", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void sendMessageToUsers(Map<String, String> userMap, String messageText) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        System.out.println("Sending message to users...");
        for (Map.Entry<String, String> entry : userMap.entrySet()) {
            String name = entry.getKey();
            String phone = entry.getValue();
            System.out.println("Sending message to " + name + " (" + phone + "): " + messageText);
            try {
                Message.creator(
                        new PhoneNumber("+216"+phone),          // To
                        new PhoneNumber(TWILIO_NUMBER),  // From
                        "Hello " + name + ", " + messageText
                ).create();
                logger.info("Message sent to " + name + " (" + phone + "): " + messageText);
            } catch (Exception e) {
                logger.error("Error sending message to " + name + " (" + phone + ")", e);
            }
        }
    }
}