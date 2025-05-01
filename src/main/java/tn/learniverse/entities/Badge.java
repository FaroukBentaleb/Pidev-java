package tn.learniverse.entities;
import tn.learniverse.tools.Session.User;

public class Badge {
    private int id;
    private String type; // PARTICIPATION, GOLD, SILVER, BRONZE
    private User user;
    private Competition competition;
    private java.sql.Timestamp awardedAt;

    // Constructors
    public Badge() {}

    public Badge(int id, String type, User user, Competition competition, java.sql.Timestamp awardedAt) {
        this.id = id;
        this.type = type;
        this.user = user;
        this.competition = competition;
        this.awardedAt = awardedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public java.sql.Timestamp getAwardedAt() {
        return awardedAt;
    }

    public void setAwardedAt(java.sql.Timestamp awardedAt) {
        this.awardedAt = awardedAt;
    }
}

