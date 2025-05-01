package tn.learniverse.test;

import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Submission;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.services.GradingService;
import tn.learniverse.tools.Session;
import tn.learniverse.tools.Session.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestGradingService {
    public static void main(String[] args) {
        // Database connection setup
        String dbUrl = "jdbc:mysql://localhost:3306/learniverse"; // Replace with your database URL
        String dbUser = "root"; // Replace with your database username
        String dbPassword = ""; // Replace with your database password

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            CompetitionService competitionService = new CompetitionService(connection);
            GradingService gradingService = new GradingService(connection);

            // Get competition with ID 3
            int competitionId = 3;
            Competition competition = competitionService.getCompetitionById(competitionId);
            if (competition == null) {
                System.out.println("Competition with ID " + competitionId + " not found.");
                return;
            }

            // Display challenges
            System.out.println("Challenges for Competition ID " + competitionId + ":");
            Map<Integer, Challenge> challenges = gradingService.getChallengesForCompetition(competitionId);
            for (Map.Entry<Integer, Challenge> entry : challenges.entrySet()) {
                System.out.println("Challenge ID: " + entry.getKey());
                System.out.println("Title: " + entry.getValue().getTitle());
                System.out.println("Description: " + entry.getValue().getDescription());
                System.out.println("Content: " + entry.getValue().getContent());
                System.out.println();
            }

            // Collect answers for challenges
            Scanner scanner = new Scanner(System.in);
            Map<Integer, String> submissions = new HashMap<>();
            for (Map.Entry<Integer, Challenge> entry : challenges.entrySet()) {
                System.out.println("Enter your solution for Challenge ID " + entry.getKey() + ":");
                String solution = scanner.nextLine();
                submissions.put(entry.getKey(), solution);
            }

            // Set submission duration
            int submissionDuration = 5;

            // Create a dummy user (replace with actual user if available)
            User user = Session.getCurrentUser();

            // Call startCompetition
            Map<Challenge, Submission> map = gradingService.startCompetition(competitionId, user, submissions, submissionDuration);

            System.out.println("Competition started and submissions processed successfully.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
