package tn.learniverse.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import  tn.learniverse.entities.Challenge;
import  tn.learniverse.entities.Competition;
import tn.learniverse.entities.Submission;
import tn.learniverse.tools.Session.User;
import tn.learniverse.tools.Session;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompetitionService {
    private static Connection connection;

    public CompetitionService(Connection connection) {
        this.connection = connection;
    }

    public static Map<Challenge, Submission> mySubmissions(int userId, int competitionId) {
        Map<Challenge, Submission> result = new HashMap<>();

        String query = "SELECT s.*, ch.* FROM submission s " +
                "JOIN challenge ch ON s.id_challenge_id = ch.id " +
                "WHERE s.id_user_id = ? AND s.competition_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, competitionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Map challenge
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("ch.id"));
                    challenge.setTitle(rs.getString("ch.title"));
                    challenge.setContent(rs.getString("ch.content"));
                    // add more fields as needed

                    // Map submission
                    Submission submission = new Submission();
                    submission.setId(rs.getInt("s.id"));
                    submission.setDate(rs.getDate("s.date").toLocalDate());
                    submission.setStudentTry(rs.getString("s.student_try"));
                    submission.setRating(rs.getInt("s.rating"));
                    String correctedCodeJson = rs.getString("corrected_code");
                    String aiFeedbackJson = rs.getString("ai_feedback");


// Parse JSON strings to Java arrays
                    ObjectMapper mapper = new ObjectMapper();
                    String[] correctedCode = mapper.readValue(correctedCodeJson, String[].class);
                    String[] aiFeedback = mapper.readValue(aiFeedbackJson, String[].class);

// Set into your Java object
                    submission.setCorrectedCode(correctedCode);
                    submission.setAiFeedback(aiFeedback);
                    submission.setTimeTaken(rs.getInt("s.time_taken"));
                    // add more fields as needed

                    result.put(challenge, submission);
                }
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // log or handle appropriately
        }

        return result;
    }

    public Map<Challenge, List<AbstractMap.SimpleEntry<User, Integer>>> getChallengeLeaderboards(int competitionId) {
        Map<Challenge, List<AbstractMap.SimpleEntry<User, Integer>>> challengeLeaderboards = new HashMap<>();

        // SQL query to get the challenges and the corresponding leaderboard for each challenge
        String sql = "SELECT c.id AS challenge_id, c.title AS challenge_title,c.content AS  challenge_content,c.solution As challenge_solution, " +
                "s.id_user_id, SUM(s.rating) AS total_score, u.nom AS user_name,u.prenom As lastname " +
                "FROM submission s " +
                "JOIN challenge c ON s.id_challenge_id = c.id " +
                "JOIN user u ON s.id_user_id = u.id " +
                "WHERE s.competition_id = ? " +
                "GROUP BY c.id, s.id_user_id " +
                "ORDER BY c.id, total_score DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, competitionId);
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                int challengeId = rs.getInt("challenge_id");
                String challengeTitle = rs.getString("challenge_title");
                String challengeContent = rs.getString("challenge_content");
                String challengeSolution = rs.getString("challenge_solution");
                int userId = rs.getInt("id_user_id");
                int totalScore = rs.getInt("total_score");
                String userName = rs.getString("user_name");
                String userlastName = rs.getString("lastname");

                // Create User and Challenge objects
                User user = new User(userId, userName,userlastName);
                Challenge challenge = new Challenge(challengeId, challengeTitle,challengeContent,challengeSolution); // Assuming Challenge class exists with constructor

                // Create the leaderboard entry
                AbstractMap.SimpleEntry<User, Integer> entry = new AbstractMap.SimpleEntry<>(user, totalScore);

                // Add the entry to the corresponding challenge's leaderboard
                challengeLeaderboards
                        .computeIfAbsent(challenge, k -> new ArrayList<>())
                        .add(entry);
            }
            for (Map.Entry<Challenge, List<AbstractMap.SimpleEntry<User, Integer>>> entry : challengeLeaderboards.entrySet()) {
                Challenge challenge = entry.getKey();
                List<AbstractMap.SimpleEntry<User, Integer>> leaderboard = entry.getValue();

                System.out.println("Challenge: " + challenge.getTitle());
                System.out.println("Leaderboard:");
                for (AbstractMap.SimpleEntry<User, Integer> leaderboardEntry : leaderboard) {
                    User user = leaderboardEntry.getKey();
                    Integer score = leaderboardEntry.getValue();
                    System.out.println(" - User: " + user.getNom() + " " + user.getPrenom() + ", Score: " + score);
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return challengeLeaderboards;
    }
    public List<Competition> getCompetitionsByUserId(int userId) {
        List<Competition> competitions = new ArrayList<>();
        String query = "SELECT c.* FROM competition c " +
                "JOIN competition_user cu ON c.id = cu.competition_id " +
                "WHERE cu.user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Competition competition = new Competition();
                    competition.setId(rs.getInt("id"));
                    competition.setNom(rs.getString("nom"));
                    competition.setEtat(rs.getString("etat"));
                    competition.setCategorie(rs.getString("categorie"));
                    competition.setDescription(rs.getString("description"));
                    competition.setDateComp(rs.getTimestamp("date_comp").toLocalDateTime());
                    competition.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
                    competition.setImageUrl(rs.getString("image_url"));
                    competition.setDuration(rs.getInt("duration"));
                    competition.setCurrentParticipant(rs.getInt("current_participant"));

                    // set other fields as needed
                    competitions.add(competition);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // or handle properly
        }

        return competitions;
    }

    public List<AbstractMap.SimpleEntry<User, Integer>> getLeaderboard(int competitionId) {
        List<AbstractMap.SimpleEntry<User, Integer>> leaderboard = new ArrayList<>();

        // SQL query to get the leaderboard for a specific competition
        String sql = "SELECT s.id_user_id, SUM(s.rating) AS total_score, u.nom,u.prenom " +
                "FROM submission s " +
                "JOIN user u ON s.id_user_id = u.id " +
                "WHERE s.competition_id = ? " +
                "GROUP BY s.id_user_id " +
                "ORDER BY total_score DESC";

        // Try-with-resources to ensure resources are closed
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            // Set the competition ID parameter
            stmt.setInt(1, competitionId);

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                int userId = rs.getInt("id_user_id");
                int totalScore = rs.getInt("total_score");
                String userName = rs.getString("nom");
                String userlastName = rs.getString("prenom");


                // Create User object
                User user = new User(userId, userName, userlastName);

                // Add to the leaderboard list
                leaderboard.add(new AbstractMap.SimpleEntry<>(user, totalScore));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return leaderboard;
    }
    public static void SetSubmmissionTrue(int id, int id1) {
        String updateSql = "UPDATE competition_user SET submission = true WHERE competition_id = ? AND user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setInt(1, id);   // Set competition_id
            statement.setInt(2, id1); // Set user_id

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Submission status updated to true successfully.");
            } else {
                System.out.println("No record found for competition_id: " + id + " and user_id: " + id1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Competition> getAllCompetitions() throws SQLException {
        List<Competition> competitions = new ArrayList<>();
        String sql = "SELECT * FROM competition";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Competition competition = mapResultSetToCompetition(resultSet);
                competitions.add(competition);
            }
        }
        return competitions;
    }

    public List<Competition> getAllUnfreesedCompetitions() throws SQLException {
        List<Competition> competitions = new ArrayList<>();
        String sql = "SELECT * FROM competition WHERE is_freesed = 0";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Competition competition = mapResultSetToCompetition(resultSet);
                competitions.add(competition);
            }
        }
        return competitions;
    }
    private Competition mapResultSetToCompetition(ResultSet resultSet) throws SQLException {
        Competition competition = new Competition();
        competition.setId(resultSet.getInt("id"));
        competition.setNom(resultSet.getString("nom"));
        competition.setDescription(resultSet.getString("description"));
        competition.setCategorie(resultSet.getString("categorie"));
        competition.setEtat(resultSet.getString("etat"));
        competition.setDateComp(resultSet.getTimestamp("date_comp").toLocalDateTime());
        competition.setDuration(resultSet.getInt("duration"));
        competition.setCurrentParticipant(resultSet.getInt("current_participant"));
        competition.setDateFin(resultSet.getTimestamp("date_fin").toLocalDateTime());
        competition.setImageUrl(resultSet.getString("image_url"));
        competition.setFreesed(resultSet.getBoolean("is_freesed"));
        competition.setNotification_sent2h(resultSet.getBoolean("notification_sent2h"));
        competition.setNotifiedEnd(resultSet.getBoolean("notifiedEnd"));
        competition.setNotification_sent_start(resultSet.getBoolean("notification_sent_start"));
        competition.setInstructorId(resultSet.getInt("id_instructor_id")); // Added mapping for instructorId


        // Check if the web_image_url column exists in the result set
        try {
            competition.setWebImageUrl(resultSet.getString("web_image_url"));
        } catch (SQLException e) {
            // Column might not exist yet, so we'll just use the regular image URL
            competition.setWebImageUrl(null);
        }

        competition.setChallenges(getChallengesForCompetition(competition.getId()));

        return competition;
    }
    public void deleteCompetition(int id) throws SQLException {
        String deleteCompetitionSql = "DELETE FROM competition WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteCompetitionSql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Competition with ID " + id + " deleted successfully (associated challenges should be deleted due to ON DELETE CASCADE).");
            } else {
                System.out.println("Competition with ID " + id + " not found, no deletion performed.");
            }

        } catch (SQLException e) {
            throw e;
        }
    }
    public static List<Challenge> getChallengesForCompetition(int competitionId) throws SQLException {
        List<Challenge> challenges = new ArrayList<>();
        String sql = "SELECT * FROM challenge WHERE id_competition_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, competitionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Challenge challenge = new Challenge();
                    challenge.setId(resultSet.getInt("id"));
                    challenge.setTitle(resultSet.getString("title"));
                    challenge.setDescription(resultSet.getString("description"));
                    challenge.setContent(resultSet.getString("content"));
                    challenge.setSolution(resultSet.getString("solution"));

                    challenges.add(challenge);
                }
            }
        }
        return challenges;
    }
    public static List<Submission> getSubmissionForCompetition(int competitionId) throws SQLException {
        List<Submission> Submissions = new ArrayList<>();
        String sql = "SELECT * FROM submission WHERE competition_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, competitionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Submission submission = new Submission();
                    submission.setId(resultSet.getInt("id"));
                    submission.setRating(resultSet.getInt("rating"));
                    Submissions.add(submission);
                }
            }
        }
        return Submissions;
    }
    public Competition createCompetition(Competition competition,  List<Challenge> challenges) throws SQLException {
        Connection conn = null;
        PreparedStatement competitionStatement = null;
        PreparedStatement challengeStatement = null;
        ResultSet generatedKeys = null;

        try {
            conn = connection; // Use the injected connection
            conn.setAutoCommit(false); // Start the transaction

            // Check if web_image_url column exists and create it if not
            createWebImageUrlColumnIfNotExists();

            // 1. Insert the Competition
            String competitionSql = "INSERT INTO competition (nom, description, categorie, etat, date_comp, duration, current_participant, date_fin, image_url, web_image_url, is_freesed, id_instructor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            competitionStatement = conn.prepareStatement(competitionSql, Statement.RETURN_GENERATED_KEYS);
            competitionStatement.setString(1, competition.getNom());
            competitionStatement.setString(2, competition.getDescription());
            competitionStatement.setString(3, competition.getCategorie());
            competitionStatement.setString(4, "Planned");
            competitionStatement.setTimestamp(5, Timestamp.valueOf(competition.getDateComp()));
            competitionStatement.setInt(6, competition.getDuration());
            competitionStatement.setInt(7, competition.getCurrentParticipant());
            competitionStatement.setTimestamp(8, Timestamp.valueOf(competition.getDateFin()));
            competitionStatement.setString(9, competition.getImageUrl());
            competitionStatement.setString(10, competition.getWebImageUrl());
            competitionStatement.setBoolean(11, competition.isFreesed());
            competitionStatement.setInt(12, 4);

            int affectedRows = competitionStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating competition failed, no rows affected.");
            }

            generatedKeys = competitionStatement.getGeneratedKeys();
            int competitionId;
            if (generatedKeys.next()) {
                competitionId = generatedKeys.getInt(1);
                competition.setId(competitionId); // Set the generated ID in the Competition object
            } else {
                throw new SQLException("Creating competition failed, no ID obtained.");
            }

            // 2. Insert the Challenges
            String challengeSql = "INSERT INTO challenge (title, description, content, solution, id_competition_id) VALUES (?, ?, ?, ?, ?)";
            challengeStatement = conn.prepareStatement(challengeSql);
            for (Challenge challenge : challenges) {
                challengeStatement.setString(1, challenge.getTitle());
                challengeStatement.setString(2, challenge.getDescription());
                challengeStatement.setString(3, challenge.getContent());
                challengeStatement.setString(4, challenge.getSolution());
                challengeStatement.setInt(5, competitionId);
                challengeStatement.executeUpdate();
            }

            conn.commit(); // Commit the transaction if everything was successful
            return competition;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback the transaction if any error occurred
                    System.err.println("Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            throw e; // Re-throw the original exception
        } finally {
            // Close resources
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    System.err.println("Error closing generated keys: " + e.getMessage());
                }
            }
            if (competitionStatement != null) {
                try {
                    competitionStatement.close();
                } catch (SQLException e) {
                    System.err.println("Error closing competition statement: " + e.getMessage());
                }
            }
            if (challengeStatement != null) {
                try {
                    challengeStatement.close();
                } catch (SQLException e) {
                    System.err.println("Error closing challenge statement: " + e.getMessage());
                }
            }
            // Do NOT close the connection here if it's managed externally (e.g., by a connection pool)
            // The calling code should handle closing the main connection.
        }
    }
    public void updateCompetitionWithChallenges(Competition competition, List<Challenge> newChallenges) throws SQLException {
        Connection conn = null;
        PreparedStatement competitionStatement = null;
        PreparedStatement challengeUpdateStatement = null;
        PreparedStatement challengeInsertStatement = null;
        PreparedStatement challengeDeleteStatement = null;

        try {
            conn = connection;
            conn.setAutoCommit(false); // Start transaction

            // Check if web_image_url column exists and create it if not
            createWebImageUrlColumnIfNotExists();

            // 1. Update the Competition
            String competitionSql = "UPDATE competition SET nom = ?, description = ?, categorie = ?, etat = ?, date_comp = ?, duration = ?, current_participant = ?, date_fin = ?, image_url = ?, web_image_url = ?, is_freesed = ?, id_instructor_id = ? WHERE id = ?";
            competitionStatement = conn.prepareStatement(competitionSql);
            competitionStatement.setString(1, competition.getNom());
            competitionStatement.setString(2, competition.getDescription());
            competitionStatement.setString(3, competition.getCategorie());
            competitionStatement.setString(4, competition.getEtat());
            competitionStatement.setTimestamp(5, Timestamp.valueOf(competition.getDateComp()));
            competitionStatement.setInt(6, competition.getDuration());
            competitionStatement.setInt(7, competition.getCurrentParticipant());
            competitionStatement.setTimestamp(8, Timestamp.valueOf(competition.getDateFin()));
            competitionStatement.setString(9, competition.getImageUrl());
            competitionStatement.setString(10, competition.getWebImageUrl());
            competitionStatement.setBoolean(11, competition.isFreesed());
            competitionStatement.setInt(12, 4);
            competitionStatement.setInt(13, competition.getId());
            int competitionRowsAffected = competitionStatement.executeUpdate();
            if (competitionRowsAffected == 0) {
                throw new SQLException("Updating competition failed, no rows affected for ID: " + competition.getId());
            }

            // 2. Get Existing Challenges for the Competition
            List<Challenge> existingChallenges = getChallengesForCompetition(competition.getId());
            Map<Integer, Challenge> existingChallengeMap = existingChallenges.stream()
                    .filter(c -> c.getId() > 0) // Only consider challenges with an ID (already in DB)
                    .collect(Collectors.toMap(Challenge::getId, Function.identity()));

            // 3. Process New Challenges
            for (Challenge newChallenge : newChallenges) {
                if (newChallenge.getId() > 0 && existingChallengeMap.containsKey(newChallenge.getId())) {
                    // Update Existing Challenge
                    String updateChallengeSql = "UPDATE challenge SET title = ?, description = ?, content = ?, solution = ?, id_competition_id = ? WHERE id = ?";
                    challengeUpdateStatement = conn.prepareStatement(updateChallengeSql);
                    challengeUpdateStatement.setString(1, newChallenge.getTitle());
                    challengeUpdateStatement.setString(2, newChallenge.getDescription());
                    challengeUpdateStatement.setString(3, newChallenge.getContent());
                    challengeUpdateStatement.setString(4, newChallenge.getSolution());
                    challengeUpdateStatement.setInt(5, competition.getId());
                    challengeUpdateStatement.setInt(6, newChallenge.getId());
                    challengeUpdateStatement.executeUpdate();
                } else if (newChallenge.getId() == 0 || !existingChallengeMap.containsKey(newChallenge.getId())) {
                    // Insert New Challenge
                    String insertChallengeSql = "INSERT INTO challenge (title, description, content, solution, id_competition_id) VALUES (?, ?, ?, ?, ?)";
                    challengeInsertStatement = conn.prepareStatement(insertChallengeSql, Statement.RETURN_GENERATED_KEYS);
                    challengeInsertStatement.setString(1, newChallenge.getTitle());
                    challengeInsertStatement.setString(2, newChallenge.getDescription());
                    challengeInsertStatement.setString(3, newChallenge.getContent());
                    challengeInsertStatement.setString(4, newChallenge.getSolution());
                    challengeInsertStatement.setInt(5, competition.getId());
                    challengeInsertStatement.executeUpdate();

                }
            }

            // 4. Delete Challenges That Are No Longer Present
            List<Integer> newChallengeIds = newChallenges.stream()
                    .filter(c -> c.getId() > 0)
                    .map(Challenge::getId)
                    .collect(Collectors.toList());

            for (Challenge existingChallenge : existingChallenges) {
                if (!newChallengeIds.contains(existingChallenge.getId())) {
                    String deleteChallengeSql = "DELETE FROM challenge WHERE id = ?";
                    challengeDeleteStatement = conn.prepareStatement(deleteChallengeSql);
                    challengeDeleteStatement.setInt(1, existingChallenge.getId());
                    challengeDeleteStatement.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("Competition with ID " + competition.getId() + " and its challenges updated successfully.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            // Close resources
            if (competitionStatement != null) try { competitionStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (challengeUpdateStatement != null) try { challengeUpdateStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (challengeInsertStatement != null) try { challengeInsertStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (challengeDeleteStatement != null) try { challengeDeleteStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Do NOT close the connection here
        }
    }
    public Competition getCompetitionById(int id) throws SQLException {
        String sql = "SELECT * FROM competition WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Competition competition = mapResultSetToCompetition(resultSet);
                    // Fetch challenges for this competition
                    competition.setChallenges(getChallengesForCompetition(competition.getId()));
                    return competition;
                }
            }
        }
        return null;
    }

    // Helper method to check and create the web_image_url column if it doesn't exist
    private void createWebImageUrlColumnIfNotExists() {
        try {
            // Check if column exists
            String checkColumnSql = "SHOW COLUMNS FROM competition LIKE 'web_image_url'";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkColumnSql)) {

                if (!rs.next()) {
                    // Column doesn't exist, create it
                    String addColumnSql = "ALTER TABLE competition ADD COLUMN web_image_url VARCHAR(255) AFTER image_url";
                    try (Statement alterStmt = connection.createStatement()) {
                        alterStmt.executeUpdate(addColumnSql);
                        System.out.println("Added web_image_url column to competition table");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking/creating web_image_url column: " + e.getMessage());
            // Continue execution even if this fails
        }
    }
    public static void AddParticipant(Competition c) {
        String updateSql = "UPDATE competition SET current_participant = current_participant + 1 WHERE id = ?";
        String insertSql = "INSERT INTO competition_user (competition_id, user_id) VALUES (?, ?)";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql);
             PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

            // Update participant count
            updateStatement.setInt(1, c.getId());
            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Participant count updated successfully.");
            } else {
                System.out.println("No competition found with ID: " + c.getId());
                return;
            }

            // Insert into competition_user table
            insertStatement.setInt(1, c.getId());
            insertStatement.setInt(2, Session.getCurrentUser().getId());
            insertStatement.executeUpdate();
            System.out.println("User added to competition_user table successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeParticipant(Competition c) {
        String updateSql = "UPDATE competition SET current_participant = current_participant - 1 WHERE id = ? AND current_participant > 0";
        String deleteSql = "DELETE FROM competition_user WHERE competition_id = ? AND user_id = ?";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql);
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {

            // Update participant count
            updateStatement.setInt(1, c.getId());
            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Participant count updated successfully.");
            } else {
                System.out.println("No competition found with ID: " + c.getId() + " or participant count is already zero.");
                return;
            }

            // Delete from competition_user table
            deleteStatement.setInt(1, c.getId());
            deleteStatement.setInt(2,Session.getCurrentUser().getId());
            deleteStatement.executeUpdate();
            System.out.println("User removed from competition_user table successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean isUserParticipant(int competitionId, int userId) {
        String query = "SELECT COUNT(*) FROM competition_user WHERE competition_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, competitionId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Check if count is greater than 0
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if an exception occurs or no record is found
    }
    public static boolean isUserSubmissited(int competitionId, int userId) {
        String query = "SELECT submission FROM competition_user WHERE competition_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, competitionId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("submission"); // Check the submission column
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if no record is found or an exception occurs
    }

    public void saveAll(List<Competition> competitions) {
        String sql = "UPDATE competition SET notification_sent2h = ?, notification_sent_start = ?, notifiedEnd = ?,etat= ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (Competition c : competitions) {
                stmt.setBoolean(1, c.isNotification_sent2h());
                stmt.setBoolean(2, c.isNotification_sent_start());
                stmt.setBoolean(3, c.isNotifiedEnd());
                stmt.setString(4, c.getEtat());
                stmt.setInt(5, c.getId());

                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> SendNotification(int competitionId) {
        Map<String, String> userMap = new HashMap<>();

        String sql = "SELECT u.nom, u.tel " +
                "FROM competition_user cu " +
                "JOIN user u ON cu.user_id = u.id " +
                "WHERE cu.competition_id = ?";

        try (
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, competitionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("nom");
                String phone = rs.getString("tel");
                if (phone != null && !phone.isEmpty()) {
                    userMap.put(name, phone);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(userMap);
        return userMap;
    }

    // Competition Statistics Methods

    public int getTotalCompetitions() throws SQLException {
        String query = "SELECT COUNT(*) FROM competition";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public int getActiveCompetitions() throws SQLException {
        String query = "SELECT COUNT(*) FROM competition WHERE etat = 'InProgress'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public int getCompletedCompetitions() throws SQLException {
        String query = "SELECT COUNT(*) FROM competition WHERE etat = 'Completed'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public double getAverageParticipants() throws SQLException {
        String query = "SELECT AVG(participant_count) FROM (SELECT COUNT(*) as participant_count FROM competition_user GROUP BY competition_id) as subquery";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    public Map<String, Integer> getCompetitionsByStatus() throws SQLException {
        Map<String, Integer> statusCounts = new HashMap<>();
        String query = "SELECT etat, COUNT(*) as count FROM competition WHERE etat IN ('Planned', 'InProgress', 'Completed') GROUP BY etat";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                statusCounts.put(rs.getString("etat"), rs.getInt("count"));
            }
            // Ensure all statuses are present
            statusCounts.putIfAbsent("Planned", 0);
            statusCounts.putIfAbsent("InProgress", 0);
            statusCounts.putIfAbsent("Completed", 0);
            return statusCounts;
        }
    }

    public List<Competition> getMostPopularCompetitions(int limit) throws SQLException {
        List<Competition> competitions = new ArrayList<>();
        String query = "SELECT c.* FROM competition c LEFT JOIN competition_user cu ON c.id = cu.competition_id " +
                "GROUP BY c.id ORDER BY COUNT(cu.user_id) DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Competition comp = mapResultSetToCompetition(rs);
                competitions.add(comp);
            }
            return competitions;
        }
    }

    public int getParticipantCount(int competitionId) throws SQLException {
        String query = "SELECT COUNT(*) FROM competition_user WHERE competition_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, competitionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public Map<String, Double> getAverageSubmissionRatingOverTime() throws SQLException {
        Map<String, Double> ratings = new LinkedHashMap<>();
        String query = "SELECT DATE_FORMAT(c.date_comp, '%Y-%m') as month, AVG(s.rating) as avg_rating " +
                "FROM competition c " +
                "LEFT JOIN challenge ch ON ch.id_competition_id = c.id " +
                "LEFT JOIN submission s ON s.id_challenge_id = ch.id " +
                "GROUP BY month ORDER BY month";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ratings.put(rs.getString("month"), rs.getDouble("avg_rating"));
            }
            return ratings;
        }
    }

    // Challenge Statistics Methods

    public int getTotalChallenges() throws SQLException {
        String query = "SELECT COUNT(*) FROM challenge WHERE id_competition_id IN " +
                "(SELECT id FROM competition WHERE etat IN ('InProgress', 'Completed'))";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public double getAverageCompletionRate() throws SQLException {
        String query = "SELECT ch.id, COUNT(s.id) as submission_count, c.current_participant " +
                "FROM challenge ch " +
                "JOIN competition c ON ch.id_competition_id = c.id " +
                "LEFT JOIN submission s ON s.id_challenge_id = ch.id " +
                "WHERE c.etat IN ('InProgress', 'Completed') " +
                "GROUP BY ch.id, c.current_participant";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            double totalCompletionRate = 0.0;
            int challengeCount = 0;
            while (rs.next()) {
                int submissionCount = rs.getInt("submission_count");
                int participantCount = rs.getInt("current_participant");
                if (participantCount > 0) {
                    totalCompletionRate += (double) submissionCount / participantCount;
                    challengeCount++;
                }
            }
            return challengeCount > 0 ? totalCompletionRate / challengeCount : 0.0;
        }
    }

    public List<Challenge> getMostDifficultChallenges(int limit) throws SQLException {
        List<Challenge> challenges = new ArrayList<>();
        String query = "SELECT ch.* FROM challenge ch " +
                "LEFT JOIN submission s ON s.id_challenge_id = ch.id " +
                "WHERE ch.id_competition_id IN (SELECT id FROM competition WHERE etat IN ('InProgress', 'Completed')) " +
                "GROUP BY ch.id ORDER BY AVG(s.rating) ASC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Challenge challenge = new Challenge();
                challenge.setId(rs.getInt("id"));
                challenge.setTitle(rs.getString("title"));
                challenge.setDescription(rs.getString("description"));
                challenge.setContent(rs.getString("content"));
                challenge.setSolution(rs.getString("solution"));
                challenges.add(challenge);
            }
            return challenges;
        }
    }

    public double getChallengeDifficultyScore(int challengeId) throws SQLException {
        String query = "SELECT AVG(rating) FROM submission WHERE id_challenge_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    public Map<String, Integer> getChallengeDifficultyDistribution() throws SQLException {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        String query = "SELECT rating_range, COUNT(*) as challenge_count " +
                "FROM ( " +
                "    SELECT ch.id, " +
                "           CASE " +
                "               WHEN AVG(s.rating) < 7 THEN '0-7' " +
                "               WHEN AVG(s.rating) < 14 THEN '7-14' " +
                "               ELSE '14-20' " +
                "           END as rating_range " +
                "    FROM challenge ch " +
                "    LEFT JOIN submission s ON s.id_challenge_id = ch.id " +
                "    WHERE ch.id_competition_id IN (SELECT id FROM competition WHERE etat IN ('InProgress', 'Completed')) " +
                "    GROUP BY ch.id " +
                ") subquery " +
                "GROUP BY rating_range";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distribution.put(rs.getString("rating_range"), rs.getInt("challenge_count"));
            }
            // Ensure all ranges are present
            distribution.putIfAbsent("0-7", 0);
            distribution.putIfAbsent("7-14", 0);
            distribution.putIfAbsent("14-20", 0);
            return distribution;
        }
    }
    // Submission Statistics Methods

    public int getTotalSubmissions() throws SQLException {
        String query = "SELECT COUNT(*) FROM submission";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public double getAverageSubmissionRating() throws SQLException {
        String query = "SELECT AVG(rating) FROM submission";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    public double getAverageTimeTaken() throws SQLException {
        String query = "SELECT AVG(time_taken) FROM submission";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    public double getSuccessRate() throws SQLException {
        String totalQuery = "SELECT COUNT(*) FROM submission";
        String successQuery = "SELECT COUNT(*) FROM submission WHERE rating > 10";
        try (PreparedStatement totalStmt = connection.prepareStatement(totalQuery);
             PreparedStatement successStmt = connection.prepareStatement(successQuery)) {
            ResultSet totalRs = totalStmt.executeQuery();
            ResultSet successRs = successStmt.executeQuery();
            int total = 0;
            int successful = 0;
            if (totalRs.next()) {
                total = totalRs.getInt(1);
            }
            if (successRs.next()) {
                successful = successRs.getInt(1);
            }
            return total > 0 ? (double) successful / total : 0.0;
        }
    }

    public Map<String, Integer> getSubmissionsOverTime() throws SQLException {
        Map<String, Integer> submissions = new LinkedHashMap<>();
        String query = "SELECT DATE_FORMAT(date, '%Y-%m') as month, COUNT(*) as count " +
                "FROM submission GROUP BY month ORDER BY month";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                submissions.put(rs.getString("month"), rs.getInt("count"));
            }
            return submissions;
        }
    }

    public int getCompetitionCount(String status, String searchQuery) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM competition WHERE 1=1");
        List<String> parameters = new ArrayList<>();

        // Add status filter if provided
        if (status != null && !status.isEmpty()) {
            query.append(" AND etat = ?");
            parameters.add(status);
        }

        // Add search query filter if provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query.append(" AND LOWER(nom) LIKE ?");
            parameters.add("%" + searchQuery.toLowerCase() + "%");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setString(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Competition> getCompetitions(String status, String searchQuery, int offset, int limit) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM competition WHERE 1=1");
        List<String> stringParameters = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            query.append(" AND etat = ?");
            stringParameters.add(status);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query.append(" AND LOWER(nom) LIKE ?");
            stringParameters.add("%" + searchQuery.toLowerCase() + "%");
        }
        query.append(" LIMIT ? OFFSET ?");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < stringParameters.size(); i++) {
                stmt.setString(i + 1, stringParameters.get(i));
            }
            stmt.setInt(stringParameters.size() + 1, limit);
            stmt.setInt(stringParameters.size() + 2, offset);

            List<Competition> competitions = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Competition comp = new Competition();
                    comp.setId(rs.getInt("id"));
                    comp.setNom(rs.getString("nom"));
                    comp.setDescription(rs.getString("description"));
                    comp.setCategorie(rs.getString("categorie"));
                    comp.setEtat(rs.getString("etat"));
                    comp.setDateComp(rs.getObject("date_comp", LocalDateTime.class));
                    comp.setDuration(rs.getInt("duration"));
                    comp.setCurrentParticipant(rs.getInt("current_participant"));
                    comp.setDateFin(rs.getObject("date_fin", LocalDateTime.class));
                    comp.setImageUrl(rs.getString("image_url"));
                    comp.setFreesed(rs.getBoolean("is_freesed"));
                    comp.setWebImageUrl(rs.getString("web_image_url"));
                    comp.setNotification_sent2h(rs.getBoolean("notification_sent2h"));
                    comp.setNotification_sent_start(rs.getBoolean("notification_sent_start"));
                    competitions.add(comp);
                }
            }
            return competitions;
        }
    }}