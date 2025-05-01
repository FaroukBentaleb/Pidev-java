package tn.learniverse.entities;

import tn.learniverse.tools.Session.User;
import java.time.LocalDate;
import java.util.Arrays;

public class Submission {
    private int id;
    private User User;
    private Challenge Challenge;
    private LocalDate date;
    private String studentTry;
    private String[] aiFeedback;
    private String[] correctedCode;
    private Integer timeTaken;
    private Integer rating;
    private Competition comp;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getIdUser() { return User; }
    public void setIdUser(User idUser) { this.User = idUser; }

    public Challenge getIdChallenge() { return Challenge; }
    public void setIdChallenge(Challenge idChallenge) { this.Challenge = idChallenge; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStudentTry() { return studentTry; }
    public void setStudentTry(String studentTry) { this.studentTry = studentTry; }

    public String[] getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String[] aiFeedback) { this.aiFeedback = aiFeedback; }

    public String[] getCorrectedCode() { return correctedCode; }
    public void setCorrectedCode(String[] correctedCode) { this.correctedCode = correctedCode; }

    public Integer getTimeTaken() { return timeTaken; }
    public void setTimeTaken(Integer timeTaken) { this.timeTaken = timeTaken; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Competition getComp() { return comp; }
    public void setComp(Competition comp) {
        this.comp = comp;
    }
}