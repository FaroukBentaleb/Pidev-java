package tn.learniverse.services;

import tn.learniverse.entities.Logs;
import tn.learniverse.tools.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogsService implements ILogs {
    private Connection connection;
    public LogsService() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    @Override
    public void addLog(Logs log) {
        String sql = "INSERT INTO logs (user_id, action, timestamp, device_type, device_model, os_info, location, session_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getAction());
            ps.setTimestamp(3, Timestamp.valueOf(log.getTimestamp()));
            ps.setString(4, log.getDeviceType());
            ps.setString(5, log.getDeviceModel());
            ps.setString(6, log.getOsInfo());
            ps.setString(7, log.getLocation());
            ps.setInt(8, log.isSessionActive());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Logs> getAllLogs() {
        List<Logs> logsList = new ArrayList<>();
        String sql = "SELECT * FROM logs";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("LogsList IDs: ");
            while (rs.next()) {
                Logs log = extractLog(rs);
                logsList.add(log);
                System.out.println(log.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return logsList;
    }

    @Override
    public List<Logs> getLogsByUserId(int userId) {
        List<Logs> logsList = new ArrayList<>();
        String sql = "SELECT * FROM logs WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Logs log = extractLog(rs);
                logsList.add(log);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return logsList;
    }
    @Override
    public boolean deleteLog(int logId) {
        String sql = "DELETE FROM logs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, logId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public boolean logExists(int logId) {
        String sql = "SELECT COUNT(*) FROM logs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, logId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    private Logs extractLog(ResultSet rs) throws SQLException {
        Logs log = new Logs();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(rs.getString("action"));
        log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        log.setDeviceType(rs.getString("device_type"));
        log.setDeviceModel(rs.getString("device_model"));
        log.setOsInfo(rs.getString("os_info"));
        log.setLocation(rs.getString("location"));
        log.setSessionActive(rs.getInt("session_active"));
        return log;
    }
}
