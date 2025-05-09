package tn.learniverse.services;

import tn.learniverse.entities.Logs;

import java.sql.SQLException;
import java.util.List;

public interface ILogs {
    void addLog(Logs log);
    List<Logs> getAllLogs();
    List<Logs> getLogsByUserId(int userId);
    boolean deleteLog(int logId);
}
