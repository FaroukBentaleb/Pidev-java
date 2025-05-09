package tn.learniverse.entities;

import java.util.Date;

import java.util.Objects;

public class Commentaire {
    private int id;
    private String contenu;
    private String dateComment;
    private Poste poste;
    private User user;
    private boolean visible;
    private String gifurl;

    public Commentaire() {}

    public Commentaire(int id, String contenu, String dateComment,
                       Poste poste, User user, boolean visible, String gifurl) {
        this.id = id;
        this.contenu = contenu;
        this.dateComment = dateComment;
        this.poste = poste;
        this.user = user;
        this.visible = visible;
        this.gifurl = gifurl;
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

    public String getDateComment() {
        return dateComment;
    }

    public void setDateComment(String dateComment) {
        this.dateComment = dateComment;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getGifurl() {
        return gifurl;
    }

    public void setGifurl(String gifurl) {
        this.gifurl = gifurl;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateComment='" + dateComment + '\'' +
                ", posteId=" + (poste != null ? poste.getId() : "null") +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", visible=" + visible +
                ", gifurl='" + gifurl + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commentaire)) return false;
        Commentaire that = (Commentaire) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
