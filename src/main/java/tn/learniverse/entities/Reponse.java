package tn.learniverse.entities;

import java.util.Date;

public class Reponse {
    private int id;
    private String contenu;
    private Date dateReponse;
    private Date dateModification;
    private int statut;
    private Reclamation reclamation;
    private User user;

    public Reponse() {
    }

    public Reponse(String contenu, Date dateReponse, Reclamation reclamation, Date dateModification, User user, int statut) {
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamation = reclamation;
        this.dateModification = dateModification;
        this.user = user;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Date getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(Date dateReponse) {
        this.dateReponse = dateReponse;
    }

    public Date getDateModification() {
        return dateModification;
    }

    public void setDateModification(Date dateModification) {
        this.dateModification = dateModification;
    }

    public int getStatut() {
        return statut;
    }

    public void setStatut(int statut) {
        this.statut = statut;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateReponse=" + dateReponse +
                ", dateModification=" + dateModification +
                ", statut=" + statut +
                ", reclamation=" + (reclamation != null ? reclamation.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                '}';
    }
}
