package tn.learniverse.entities;

public class Favorite {
    private int id;
    private int userId;
    private int courseId;

    // Constructeur vide
    public Favorite() {
    }

    // Constructeur avec tous les attributs
    public Favorite(int id, int userId, int courseId) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
    }

    // Constructeur sans ID (utile pour l'insertion)
    public Favorite(int userId, int courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    // Getters et Setters
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

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                '}';
    }
}