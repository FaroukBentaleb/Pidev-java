package tn.learniverse.entities;

import tn.learniverse.tools.Session;

import java.time.LocalDateTime;
import java.util.List;

public class Competition {
    private int id;
    private String nom;
    private String description;
    private String categorie;
    private String etat;
    private LocalDateTime dateComp;
    private int duration;
    private int currentParticipant;
    private LocalDateTime dateFin;
    private String imageUrl;
    private boolean isFreesed;

    public boolean isNotification_sent2h() {
        return notification_sent2h;
    }

    public void setNotification_sent2h(boolean notification_sent2h) {
        this.notification_sent2h = notification_sent2h;
    }

    private boolean notification_sent2h;

    public boolean isNotification_sent_start() {
        return notification_sent_start;
    }

    public void setNotification_sent_start(boolean notification_sent_start) {
        this.notification_sent_start = notification_sent_start;
    }

    private boolean notification_sent_start;

    public boolean isNotifiedEnd() {
        return notifiedEnd;
    }

    public void setNotifiedEnd(boolean notifiedEnd) {
        this.notifiedEnd = notifiedEnd;
    }

    private boolean notifiedEnd ;
    private int instructorId;
    private List<Challenge> challenges;
    private String webImageUrl;
   private List<Submission> submissions;
   private List<Session.User> Participants;



    public Competition() {
    }

    // Getters and setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public LocalDateTime getDateComp() {
        return dateComp;
    }

    public void setDateComp(LocalDateTime dateComp) {
        this.dateComp = dateComp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCurrentParticipant() {
        return currentParticipant;
    }

    public void setCurrentParticipant(int currentParticipant) {
        this.currentParticipant = currentParticipant;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public boolean isFreesed() {
        return isFreesed;
    }

    public void setFreesed(boolean freesed) {
        isFreesed = freesed;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public List<Challenge> getChallenges() {

        return challenges;
    }

    public void setChallenges(List<Challenge> challenges) {

        this.challenges = challenges;
    }

    public String getWebImageUrl() {
        return webImageUrl;
    }

    public void setWebImageUrl(String webImageUrl) {
        this.webImageUrl = webImageUrl;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }


    public List<Session.User> getParticipants() {
        return Participants;
    }

    public void setParticipants(List<Session.User> Participants) {
        this.Participants = Participants;
    }

}