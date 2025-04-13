package com.esprit.java.Services;

import com.esprit.java.Models.Challenge;
import com.esprit.java.Models.Competition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompetitionService {
    private Connection connection;

    public CompetitionService(Connection connection) {
        this.connection = connection;
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
    private List<Challenge> getChallengesForCompetition(int competitionId) throws SQLException {
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
    public Competition createCompetition(Competition competition,  List<Challenge> challenges) throws SQLException {
        Connection conn = null;
        PreparedStatement competitionStatement = null;
        PreparedStatement challengeStatement = null;
        ResultSet generatedKeys = null;

        try {
            conn = connection; // Use the injected connection
            conn.setAutoCommit(false); // Start the transaction

            // 1. Insert the Competition
            String competitionSql = "INSERT INTO competition (nom, description, categorie, etat, date_comp, duration, current_participant, date_fin, image_url, is_freesed, id_instructor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            competitionStatement = conn.prepareStatement(competitionSql, Statement.RETURN_GENERATED_KEYS);
            competitionStatement.setString(1, competition.getNom());
            competitionStatement.setString(2, competition.getDescription());
            competitionStatement.setString(3, competition.getCategorie());
            competitionStatement.setString(4, competition.getEtat());
            competitionStatement.setTimestamp(5, Timestamp.valueOf(competition.getDateComp()));
            competitionStatement.setInt(6, competition.getDuration());
            competitionStatement.setInt(7, competition.getCurrentParticipant());
            competitionStatement.setTimestamp(8, Timestamp.valueOf(competition.getDateFin()));
            competitionStatement.setString(9, competition.getImageUrl());
            competitionStatement.setBoolean(10, competition.isFreesed());
            competitionStatement.setInt(11, 4);

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

            // 1. Update the Competition
            String competitionSql = "UPDATE competition SET nom = ?, description = ?, categorie = ?, etat = ?, date_comp = ?, duration = ?, current_participant = ?, date_fin = ?, image_url = ?, is_freesed = ?, id_instructor_id = ? WHERE id = ?";
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
            competitionStatement.setBoolean(10, competition.isFreesed());
            competitionStatement.setInt(11, 4);
            competitionStatement.setInt(12, competition.getId());
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

    // CRUD methods (createCompetition, getCompetitionById, etc.) ...
    // Add Challenge methods (addChallengeToCompetition, getChallengesForCompetition, etc.)
}