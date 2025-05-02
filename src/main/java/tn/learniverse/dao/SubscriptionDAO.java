package tn.learniverse.dao;

import tn.learniverse.model.Subscription;
import tn.learniverse.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO {
    
    public void create(Subscription subscription) {
        String query = "INSERT INTO subscription (id_user_id, id_offre_id, id_course_id, date_earned, status) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, subscription.getUserId());
            stmt.setInt(2, subscription.getOffreId());
            stmt.setInt(3, subscription.getCourseId());
            stmt.setTimestamp(4, Timestamp.valueOf(subscription.getDateEarned()));
            stmt.setString(5, subscription.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating subscription failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    subscription.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating subscription failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating subscription: " + e.getMessage(), e);
        }
    }
    
    public List<Subscription> readAll() {
        List<Subscription> subscriptions = new ArrayList<>();
        String query = "SELECT * FROM subscription";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Subscription subscription = new Subscription();
                subscription.setId(rs.getInt("id"));
                subscription.setUserId(rs.getInt("id_user_id"));
                subscription.setOffreId(rs.getInt("id_offre_id"));
                subscription.setCourseId(rs.getInt("id_course_id"));
                subscription.setDateEarned(rs.getTimestamp("date_earned").toLocalDateTime());
                subscription.setStatus(rs.getString("status"));
                
                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading subscriptions: " + e.getMessage(), e);
        }
        
        return subscriptions;
    }
    
    public Subscription readById(int id) {
        String query = "SELECT * FROM subscription WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Subscription subscription = new Subscription();
                    subscription.setId(rs.getInt("id"));
                    subscription.setUserId(rs.getInt("id_user_id"));
                    subscription.setOffreId(rs.getInt("id_offre_id"));
                    subscription.setCourseId(rs.getInt("id_course_id"));
                    subscription.setDateEarned(rs.getTimestamp("date_earned").toLocalDateTime());
                    subscription.setStatus(rs.getString("status"));
                    
                    return subscription;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading subscription: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public void update(Subscription subscription) {
        String query = "UPDATE subscription SET id_user_id = ?, id_offre_id = ?, id_course_id = ?, date_earned = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, subscription.getUserId());
            stmt.setInt(2, subscription.getOffreId());
            stmt.setInt(3, subscription.getCourseId());
            stmt.setTimestamp(4, Timestamp.valueOf(subscription.getDateEarned()));
            stmt.setString(5, subscription.getStatus());
            stmt.setInt(6, subscription.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating subscription: " + e.getMessage(), e);
        }
    }
    
    public void delete(int id) {
        String query = "DELETE FROM subscription WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting subscription: " + e.getMessage(), e);
        }
    }
    
    public List<Subscription> findByUserId(int userId) {
        List<Subscription> subscriptions = new ArrayList<>();
        String query = "SELECT * FROM subscription WHERE id_user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subscription subscription = new Subscription();
                    subscription.setId(rs.getInt("id"));
                    subscription.setUserId(rs.getInt("id_user_id"));
                    subscription.setOffreId(rs.getInt("id_offre_id"));
                    subscription.setCourseId(rs.getInt("id_course_id"));
                    subscription.setDateEarned(rs.getTimestamp("date_earned").toLocalDateTime());
                    subscription.setStatus(rs.getString("status"));
                    
                    subscriptions.add(subscription);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding subscriptions by user ID: " + e.getMessage(), e);
        }
        
        return subscriptions;
    }
} 