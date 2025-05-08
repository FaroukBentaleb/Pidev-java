package tn.learniverse.entities;

import java.util.Objects;

public class Like {
    private int id;
    private String type; // Exemple : "smile", "love", "angry"
    private Poste poste;
    private User user;

    // Constructeur vide
    public Like() {
    }

    // Constructeur plein
    public Like(int id, String type, Poste poste, User user) {
        this.id = id;
        this.type = type;
        this.poste = poste;
        this.user = user;
    }

    // Constructeur sans id (utile pour l'ajout avant de générer l'id en base)
    public Like(String type, Poste poste, User user) {
        this.type = type;
        this.poste = poste;
        this.user = user;
    }

    // Getters et Setters
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

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // toString
    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", poste=" + poste +
                ", user=" + user +
                '}';
    }

    // equals et hashCode : important pour éviter les doublons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Like)) return false;
        Like like = (Like) o;
        return Objects.equals(poste, like.poste) &&
                Objects.equals(user, like.user) &&
                Objects.equals(type, like.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(poste, user, type);
    }
}
