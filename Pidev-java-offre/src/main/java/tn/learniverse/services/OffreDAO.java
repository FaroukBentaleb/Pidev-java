package tn.learniverse.services;

import tn.learniverse.entities.Offre;
import tn.learniverse.tools.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreDAO {
    private Connection connection;

    public OffreDAO() {
        connection = DatabaseConnection.getConnection();
    }

    public void create(Offre offre) {
        String query = "INSERT INTO offre (name, price_per_month, applicable_plans, custom_plan, discount, " +
                "description, conditions, promo_code, valid_from, valid_until, is_active, max_subscriptions, " +
                "target_audience, benefits, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, offre.getName());
            statement.setDouble(2, offre.getPricePerMonth());
            statement.setString(3, offre.getApplicablePlans());
            statement.setString(4, offre.getCustomPlan());
            statement.setObject(5, offre.getDiscount());
            statement.setString(6, offre.getDescription());
            statement.setString(7, offre.getConditions());
            statement.setString(8, offre.getPromoCode());
            statement.setDate(9, Date.valueOf(offre.getValidFrom()));
            statement.setObject(10, offre.getValidUntil() != null ? Date.valueOf(offre.getValidUntil()) : null);
            statement.setBoolean(11, offre.isActive());
            statement.setObject(12, offre.getMaxSubscriptions());
            statement.setString(13, offre.getTargetAudience());
            statement.setString(14, offre.getBenefits());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    offre.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating offer: " + e.getMessage());
        }
    }

    public List<Offre> findAll() {
        List<Offre> offres = new ArrayList<>();
        String query = "SELECT * FROM offre";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Offre offre = new Offre();
                offre.setId(resultSet.getInt("id"));
                offre.setName(resultSet.getString("name"));
                offre.setPricePerMonth(resultSet.getDouble("price_per_month"));
                offre.setApplicablePlans(resultSet.getString("applicable_plans"));
                offre.setCustomPlan(resultSet.getString("custom_plan"));
                offre.setDiscount(resultSet.getObject("discount", Double.class));
                offre.setDescription(resultSet.getString("description"));
                offre.setConditions(resultSet.getString("conditions"));
                offre.setPromoCode(resultSet.getString("promo_code"));
                
                // Handle dates properly
                Date validFromDate = resultSet.getDate("valid_from");
                if (validFromDate != null) {
                    offre.setValidFrom(validFromDate.toLocalDate());
                }
                
                Date validUntilDate = resultSet.getDate("valid_until");
                if (validUntilDate != null) {
                    offre.setValidUntil(validUntilDate.toLocalDate());
                }
                
                offre.setActive(resultSet.getBoolean("is_active"));
                offre.setMaxSubscriptions(resultSet.getObject("max_subscriptions", Integer.class));
                offre.setTargetAudience(resultSet.getString("target_audience"));
                offre.setBenefits(resultSet.getString("benefits"));
                
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                if (createdAt != null) {
                    offre.setCreatedAt(createdAt.toLocalDateTime());
                }

                offres.add(offre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving offers: " + e.getMessage());
        }

        return offres;
    }

    public Offre findById(int id) {
        String query = "SELECT * FROM offre WHERE id = ?";
        Offre offre = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                offre = new Offre();
                offre.setId(resultSet.getInt("id"));
                offre.setName(resultSet.getString("name"));
                offre.setPricePerMonth(resultSet.getDouble("price_per_month"));
                offre.setApplicablePlans(resultSet.getString("applicable_plans"));
                offre.setCustomPlan(resultSet.getString("custom_plan"));
                offre.setDiscount(resultSet.getObject("discount", Double.class));
                offre.setDescription(resultSet.getString("description"));
                offre.setConditions(resultSet.getString("conditions"));
                offre.setPromoCode(resultSet.getString("promo_code"));
                
                Date validFromDate = resultSet.getDate("valid_from");
                if (validFromDate != null) {
                    offre.setValidFrom(validFromDate.toLocalDate());
                }
                
                Date validUntilDate = resultSet.getDate("valid_until");
                if (validUntilDate != null) {
                    offre.setValidUntil(validUntilDate.toLocalDate());
                }
                
                offre.setActive(resultSet.getBoolean("is_active"));
                offre.setMaxSubscriptions(resultSet.getObject("max_subscriptions", Integer.class));
                offre.setTargetAudience(resultSet.getString("target_audience"));
                offre.setBenefits(resultSet.getString("benefits"));
                
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                if (createdAt != null) {
                    offre.setCreatedAt(createdAt.toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding offer by ID: " + e.getMessage());
        }

        return offre;
    }

    public void update(Offre offre) {
        String query = "UPDATE offre SET name = ?, price_per_month = ?, applicable_plans = ?, custom_plan = ?, " +
                "discount = ?, description = ?, conditions = ?, promo_code = ?, valid_from = ?, valid_until = ?, " +
                "is_active = ?, max_subscriptions = ?, target_audience = ?, benefits = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, offre.getName());
            statement.setDouble(2, offre.getPricePerMonth());
            statement.setString(3, offre.getApplicablePlans());
            statement.setString(4, offre.getCustomPlan());
            statement.setObject(5, offre.getDiscount());
            statement.setString(6, offre.getDescription());
            statement.setString(7, offre.getConditions());
            statement.setString(8, offre.getPromoCode());
            statement.setDate(9, Date.valueOf(offre.getValidFrom()));
            statement.setObject(10, offre.getValidUntil() != null ? Date.valueOf(offre.getValidUntil()) : null);
            statement.setBoolean(11, offre.isActive());
            statement.setObject(12, offre.getMaxSubscriptions());
            statement.setString(13, offre.getTargetAudience());
            statement.setString(14, offre.getBenefits());
            statement.setInt(15, offre.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Updating offer failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating offer: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM offre WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Deleting offer failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting offer: " + e.getMessage());
        }
    }
} 