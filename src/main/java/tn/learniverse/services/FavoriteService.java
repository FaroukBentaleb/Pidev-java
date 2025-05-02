package tn.learniverse.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tn.learniverse.entities.Favorite;
import tn.learniverse.tools.DBConnection;

public class FavoriteService {
    private Connection connection;

    public FavoriteService() {
        connection = DBConnection.getInstance().getConnection();
    }

    /**
     * Add a course to user's favorites
     */
    public void addFavorite(int userId, int courseId) throws SQLException {
        String query = "INSERT INTO favorites (user_id, course_id) VALUES (?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, courseId);
            pst.executeUpdate();
        }
    }

    /**
     * Remove a course from user's favorites
     */
    public void removeFavorite(int userId, int courseId) throws SQLException {
        String query = "DELETE FROM favorites WHERE user_id = ? AND course_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, courseId);
            pst.executeUpdate();
        }
    }

    /**
     * Check if a course is in user's favorites
     */
    public boolean isFavorite(int userId, int courseId) throws SQLException {
        String query = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND course_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, courseId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Get all favorite courses for a user
     */
    public Set<Integer> getUserFavorites(int userId) throws SQLException {
        Set<Integer> favorites = new HashSet<>();
        String query = "SELECT course_id FROM favorites WHERE user_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    favorites.add(rs.getInt("course_id"));
                }
            }
        }

        return favorites;
    }

    /**
     * Get all favorites (for admin purposes)
     */
    public List<Favorite> getAllFavorites() throws SQLException {
        List<Favorite> favorites = new ArrayList<>();
        String query = "SELECT id, user_id, course_id FROM favorites";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Favorite favorite = new Favorite(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("course_id")
                );
                favorites.add(favorite);
            }
        }

        return favorites;
    }
}