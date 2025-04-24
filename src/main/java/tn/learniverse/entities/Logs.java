package tn.learniverse.entities;

import java.time.LocalDateTime;

public class Logs {
    private int id;
    private int userId;
    private String action;
    private LocalDateTime timestamp;
    private String deviceType;
    private String deviceModel;
    private String osInfo;
    private String location;
    private boolean sessionActive;

    public Logs() {}

    public Logs(int userId, String action, LocalDateTime timestamp, String deviceType, String deviceModel,
                String osInfo, String location, boolean sessionActive) {
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.deviceType = deviceType;
        this.deviceModel = deviceModel;
        this.osInfo = osInfo;
        this.location = location;
        this.sessionActive = sessionActive;
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public String getOsInfo() { return osInfo; }
    public void setOsInfo(String osInfo) { this.osInfo = osInfo; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isSessionActive() { return sessionActive; }
    public void setSessionActive(boolean sessionActive) { this.sessionActive = sessionActive; }
}
