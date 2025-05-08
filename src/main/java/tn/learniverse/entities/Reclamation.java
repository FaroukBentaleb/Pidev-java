package tn.learniverse.entities;

import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

public class Reclamation {
    private int id;
    private String titre;
    private Timestamp date_reclamation;
    private String contenu;
    private String statut;
    private int statut_archivation;
    private int statut_archivation_back;
    private Timestamp date_modification;
    private String fichier;
    private User user;
    private List<Reponse> reponses;

    public Reclamation() {
    }

    public Reclamation(int id, String titre, Timestamp date_reclamation, String contenu, String statut, User user,
                       int statut_archivation, int statut_archivation_back, Timestamp date_modification, String fichier) {
        this.id = id;
        this.titre = titre;
        this.date_reclamation = date_reclamation;
        this.contenu = contenu;
        this.statut = statut;
        this.statut_archivation = statut_archivation;
        this.statut_archivation_back = statut_archivation_back;
        this.date_modification = date_modification;
        this.fichier = fichier;
        this.user = user;
    }

    public Reclamation(String titre, Timestamp date_reclamation, String contenu, String statut, User user,
                       int statut_archivation, int statut_archivation_back, Timestamp date_modification, String fichier) {
        this.titre = titre;
        this.date_reclamation = date_reclamation;
        this.contenu = contenu;
        this.statut = statut;
        this.statut_archivation = statut_archivation;
        this.statut_archivation_back = statut_archivation_back;
        this.date_modification = date_modification;
        this.fichier = fichier;
        this.user = user;
    }

    public Reclamation(String titre, Timestamp date_reclamation, String contenu, String statut,
                       Timestamp date_modification, String fichier) {
        this.titre = titre;
        this.date_reclamation = date_reclamation;
        this.contenu = contenu;
        this.statut = statut;
        this.date_modification = date_modification;
        this.fichier = fichier;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Date getDateReclamation() {
        return date_reclamation;
    }

    public void setDateReclamation(Timestamp date_reclamation) {
        this.date_reclamation = date_reclamation;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getStatutArchivation() {
        return statut_archivation;
    }

    public void setStatutArchivation(int statut_archivation) {
        this.statut_archivation = statut_archivation;
    }

    public int getStatutArchivationBack() {
        return statut_archivation_back;
    }

    public void setStatutArchivationBack(int statut_archivation_back) {
        this.statut_archivation_back = statut_archivation_back;
    }

    public Date getDateModification() {
        return date_modification;
    }

    public void setDateModification(Timestamp date_modification) {
        this.date_modification = date_modification;
    }

    public String getFichier() {
        return fichier;
    }

    public void setFichier(String fichier) {
        this.fichier = fichier;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Reponse> getReponses() {
        return reponses;
    }

    public void setReponses(List<Reponse> reponses) {
        this.reponses = reponses;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = sdf.format(date_reclamation);
        String date2 = sdf.format(date_modification);
        return "Reclamation{" +
                "titre='" + titre + '\'' +
                ", dateReclamation=" + date1 +
                ", contenu='" + contenu + '\'' +
                ", statut='" + statut + '\'' +
                ", dateModification=" + date2 +
                ", fichier='" + fichier + '\'' +
                ", user=" + (user != null ? user.getId() : "null") +
                '}';
    }


}
