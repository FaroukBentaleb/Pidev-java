package tn.learniverse.entities;

import java.time.LocalDateTime;

public class Subscription {
    private int id;
    private int userId;
    private int offreId;
    private int courseId;
    private LocalDateTime dateEarned;
    private String status;
    private long durationInDays;

    // Default constructor
    public Subscription() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOffreId() {
        return offreId;
    }

    public void setOffreId(int offreId) {
        this.offreId = offreId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getDateEarned() {
        return dateEarned;
    }

    public void setDateEarned(LocalDateTime dateEarned) {
        this.dateEarned = dateEarned;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(long durationInDays) {
        this.durationInDays = durationInDays;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", userId=" + userId +
                ", offreId=" + offreId +
                ", courseId=" + courseId +
                ", dateEarned=" + dateEarned +
                ", status='" + status + '\'' +
                ", durationInDays=" + durationInDays +
                '}';
    }
} 