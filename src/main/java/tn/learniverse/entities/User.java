
package tn.learniverse.entities;

import java.util.Objects;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String mdp;
    private String dateDeNaissance;
    private int tel;
    private String role;
    private String field;
    private String description;
    private int experience;
    private String job;
    private String resume;
    private String picture;
    private String facebookLink;
    private String instagramLink;
    private String linkedinLink;
    private boolean verified;
    private int logs;
    private String googleAuthenticatorSecret;
    private int ban;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateDeNaissance() {
        return dateDeNaissance;
    }

    public void setDateDeNaissance(String dateDeNaissance) {
        this.dateDeNaissance = dateDeNaissance;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public int getTel() {
        return tel;
    }

    public void setTel(int tel) {
        this.tel = tel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    public String getInstagramLink() {
        return instagramLink;
    }

    public void setInstagramLink(String instagramLink) {
        this.instagramLink = instagramLink;
    }

    public String getLinkedinLink() {
        return linkedinLink;
    }

    public void setLinkedinLink(String linkedinLink) {
        this.linkedinLink = linkedinLink;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getLogs() {
        return logs;
    }

    public void setLogs(int logs) {
        this.logs = logs;
    }

    public String getGoogleAuthenticatorSecret() {
        return googleAuthenticatorSecret;
    }

    public void setGoogleAuthenticatorSecret(String googleAuthenticatorSecret) {
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
    }

    public int getBan() {
        return ban;
    }

    public void setBan(int ban) {
        this.ban = ban;
    }

    public User() {
    }

    public User(String nom, String prenom, String email,String role, String mdp) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.mdp = mdp;
    }
    public User(String nom, String prenom, String email, String mdp, String dateDeNaissance, int tel, String field, String description, int experience, String job, String resume, String picture, String facebookLink, String instagramLink, String linkedinLink, boolean verified, int logs, int ban) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
    public User(String nom, String prenom, String email, int phone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.tel = phone;
    }
    public User(String nom, String prenom, String email,String role, String mdp, String dateDeNaissance, int tel, String field, String description, int experience, String job, String resume, String picture, String facebookLink, String instagramLink, String linkedinLink, boolean verified, int logs, int ban) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.mdp = mdp;
        this.dateDeNaissance = dateDeNaissance;
        this.tel = tel;
        this.field = field;
        this.description = description;
        this.experience = experience;
        this.job = job;
        this.resume = resume;
        this.picture = picture;
        this.facebookLink = facebookLink;
        this.instagramLink = instagramLink;
        this.linkedinLink = linkedinLink;
        this.verified = verified;
        this.logs = logs;
        this.ban = ban;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && tel == user.tel && experience == user.experience && verified == user.verified && logs == user.logs && ban == user.ban && Objects.equals(nom, user.nom) && Objects.equals(prenom, user.prenom) && Objects.equals(email, user.email) && Objects.equals(mdp, user.mdp) && Objects.equals(dateDeNaissance, user.dateDeNaissance) && Objects.equals(role, user.role) && Objects.equals(field, user.field) && Objects.equals(description, user.description) && Objects.equals(job, user.job) && Objects.equals(resume, user.resume) && Objects.equals(picture, user.picture) && Objects.equals(facebookLink, user.facebookLink) && Objects.equals(instagramLink, user.instagramLink) && Objects.equals(linkedinLink, user.linkedinLink) && Objects.equals(googleAuthenticatorSecret, user.googleAuthenticatorSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, email, mdp, dateDeNaissance, tel, role, field, description, experience, job, resume, picture, facebookLink, instagramLink, linkedinLink, verified, logs, googleAuthenticatorSecret, ban);
    }

    @Override
    public String toString() {
        return "User{" +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", mdp='" + mdp + '\'' +
                ", dateDeNaissance='" + dateDeNaissance + '\'' +
                ", tel=" + tel +
                ", role='" + role + '\'' +
                ", field='" + field + '\'' +
                ", description='" + description + '\'' +
                ", experience=" + experience +
                ", job='" + job + '\'' +
                ", resume='" + resume + '\'' +
                ", picture='" + picture + '\'' +
                ", facebookLink='" + facebookLink + '\'' +
                ", instagramLink='" + instagramLink + '\'' +
                ", linkedinLink='" + linkedinLink + '\'' +
                ", verified=" + verified +
                ", logs=" + logs +
                ", googleAuthenticatorSecret='" + googleAuthenticatorSecret + '\'' +
                ", ban=" + ban +
                '}';
    }
}
