package com.learniverse.dao;

import com.learniverse.model.Subscription;
import com.learniverse.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO {
    private Connection connection;

    public SubscriptionDAO() {
        connection = DatabaseConnection.getConnection();
    }

    public void create(Subscription subscription) {
        String query = "INSERT INTO subscription (id_course_id, date_earned, status, id_user_id, id_offre_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, subscription.getCourseId() > 0 ? subscription.getCourseId() : null);
            statement.setTimestamp(2, Timestamp.valueOf(subscription.getDateEarned()));
            statement.setString(3, subscription.getStatus());
            statement.setObject(4, subscription.getUserId() > 0 ? subscription.getUserId() : null);
            statement.setObject(5, subscription.getOffreId() > 0 ? subscription.getOffreId() : null);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    subscription.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Subscription> readAll() {
        List<Subscription> subscriptions = new ArrayList<>();
        String query = "SELECT * FROM subscription";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Subscription subscription = new Subscription();
                subscription.setId(resultSet.getInt("id"));
                subscription.setCourseId(resultSet.getInt("id_course_id"));
                subscription.setDateEarned(resultSet.getTimestamp("date_earned").toLocalDateTime());
                subscription.setStatus(resultSet.getString("status"));
                subscription.setUserId(resultSet.getInt("id_user_id"));
                subscription.setOffreId(resultSet.getInt("id_offre_id"));

                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subscriptions;
    }

    public Subscription readById(int id) {
        String query = "SELECT * FROM subscription WHERE id = ?";
        Subscription subscription = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                subscription = new Subscription();
                subscription.setId(resultSet.getInt("id"));
                subscription.setCourseId(resultSet.getInt("id_course_id"));
                subscription.setDateEarned(resultSet.getTimestamp("date_earned").toLocalDateTime());
                subscription.setStatus(resultSet.getString("status"));
                subscription.setUserId(resultSet.getInt("id_user_id"));
                subscription.setOffreId(resultSet.getInt("id_offre_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subscription;
    }

    public void update(Subscription subscription) {
        String query = "UPDATE subscription SET id_course_id = ?, date_earned = ?, status = ?, " +
                "id_user_id = ?, id_offre_id = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, subscription.getCourseId() > 0 ? subscription.getCourseId() : null);
            statement.setTimestamp(2, Timestamp.valueOf(subscription.getDateEarned()));
            statement.setString(3, subscription.getStatus());
            statement.setObject(4, subscription.getUserId() > 0 ? subscription.getUserId() : null);
            statement.setObject(5, subscription.getOffreId() > 0 ? subscription.getOffreId() : null);
            statement.setInt(6, subscription.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM subscription WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 