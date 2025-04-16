package tn.learniverse.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Poste {
    private int id;
    private user user;
    private String titre;
    private String contenu;
    private String datePost;
    private boolean visible;
    private int nbCom;
    private String categorie;
    private String photo;

    private List<Commentaire> commentaires = new ArrayList<>();
    public Poste() {}

    public Poste(int id, user user, String titre, String contenu, String datePost,
                 boolean visible, int nbCom, String categorie, String photo) {
        this.id = id;
        this.user = user;
        this.titre = titre;
        this.contenu = contenu;
        this.datePost = datePost;
        this.visible = visible;
        this.nbCom = nbCom;
        this.categorie = categorie;
        this.photo = photo;
    }

    public Poste(String contenu, String titre, String categorie) {
        this.contenu = contenu;
        this.titre = titre;
        this.categorie = categorie;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public user getUser() {
        return user;
    }

    public void setUser(user user) {
        this.user = user;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getDatePost() {
        return datePost;
    }

    public void setDatePost(String datePost) {
        this.datePost = datePost;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getNbCom() {
        return nbCom;
    }

    public void setNbCom(int nbCom) {
        this.nbCom = nbCom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getPhoto() {
        return photo;
    }

    public  void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(List<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    public void addCommentaire(Commentaire c) {
        commentaires.add(c);
    }

    @Override
    public String toString() {
        return "Poste{" +
                "id=" + id +
                ", user_id=" + (user != null ? user.getId() : "null") +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", datePost='" + datePost + '\'' +
                ", visible=" + visible +
                ", nbCom=" + nbCom +
                ", categorie='" + categorie + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Poste)) return false;
        Poste poste = (Poste) o;
        return id == poste.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


