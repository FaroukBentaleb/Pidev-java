package tn.learniverse.services;
import tn.learniverse.entities.Badge;
import tn.learniverse.entities.Competition;
import tn.learniverse.tools.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadgeService {
    private final Connection connection;

    public BadgeService(Connection connection) {
        this.connection = connection;
    }

    public void addBadge(Badge badge) throws SQLException {
        String sql = "INSERT INTO badge (type, user_id, competition_id, awarded_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, badge.getType());
            stmt.setInt(2, badge.getUser().getId());
            stmt.setInt(3, badge.getCompetition().getId());
            stmt.setTimestamp(4, badge.getAwardedAt());

            stmt.executeUpdate();
        }
    }
    public List<Badge> getBadgesByUserId(int userId) throws SQLException {
        List<Badge> badges = new ArrayList<>();
        String sql = "SELECT b.*, c.id as comp_id, c.nom as comp_nom FROM badge b " +
                "JOIN competition c ON b.competition_id = c.id " +
                "WHERE b.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Badge badge = new Badge();
                badge.setId(rs.getInt("id"));
                badge.setType(rs.getString("type"));
                Session.User user = new Session.User();
                user.setId(rs.getInt("user_id"));
                badge.setUser(user);
                Competition comp = new Competition();
                comp.setId(rs.getInt("comp_id"));
                comp.setNom(rs.getString("comp_nom"));
                badge.setCompetition(comp);
                badge.setAwardedAt(rs.getTimestamp("awarded_at"));
                badges.add(badge);
            }
        }
        return badges;
    }

    public Badge getBadgeByUserAndCompetition(int userId, int competitionId) throws SQLException {
        String sql = "SELECT * FROM badge WHERE user_id = ? AND competition_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, competitionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Badge badge = new Badge();
                badge.setId(rs.getInt("id"));
                badge.setType(rs.getString("type"));
                Session.User user = new Session.User();
                user.setId(rs.getInt("user_id"));
                badge.setUser(user);
                Competition comp = new Competition();
                comp.setId(rs.getInt("competition_id"));
                badge.setCompetition(comp);
                badge.setAwardedAt(rs.getTimestamp("awarded_at"));
                return badge;
            }
        }
        return null;
    }

    public Map<String, Integer> getBadgeCountsByUserId(int userId) throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("GOLD", 0);
        counts.put("SILVER", 0);
        counts.put("BRONZE", 0);
        counts.put("PARTICIPATION", 0);

        String sql = "SELECT type, COUNT(*) as count FROM badge WHERE user_id = ? GROUP BY type";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                counts.put(rs.getString("type"), rs.getInt("count"));
            }
        }
        return counts;
    }
}

