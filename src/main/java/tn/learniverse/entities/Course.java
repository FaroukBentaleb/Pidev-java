package tn.learniverse.entities;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Objects;

public class Course {

    private int id;
    private int id_user;
    private String image;
    private String video_url;
    private boolean is_frozen;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 50, message = "Le titre doit être entre 5 et 50 caractères")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @Min(value = 1, message = "La durée doit être supérieure à 0")
    private int duration;

    @Positive(message = "Le prix doit être un nombre positif")
    @DecimalMin(value = "0.0")
    private double price;

    @NotBlank(message = "Le niveau est requis")
    private String level;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;

    private List<Lesson> lessons;

    public Course() {
    }

    public Course( String title, String description, int duration, double price, String level, String category, int id_user) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.level = level;
        this.category = category;
        this.id_user = id_user;
    }

    public Course(String title, String description, int duration, double price, String level,String category
                  ,String image, String video_url, boolean is_frozen) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.level = level;
        this.category = category;
        this.image = image;
        this.video_url = video_url;
        this.is_frozen = is_frozen;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public boolean isIs_frozen() {
        return is_frozen;
    }

    public void setIs_frozen(boolean is_frozen) {
        this.is_frozen = is_frozen;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Méthode equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id == course.id && id_user == course.id_user && duration == course.duration &&
                Double.compare(course.price, price) == 0 && is_frozen == course.is_frozen &&
                Objects.equals(title, course.title) && Objects.equals(description, course.description) &&
                Objects.equals(level, course.level) && Objects.equals(image, course.image) &&
                Objects.equals(video_url, course.video_url) && Objects.equals(category, course.category);
    }

    // Méthode hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, id_user, title, description, duration, price, level,category, image, video_url, is_frozen);
    }

    @Override
    public String toString() {
        return "Course{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", price=" + price +
                ", level='" + level + '\'' +
                ", category='" + category + '\'' +
                ", image='" + image + '\'' +
                ", video_url='" + video_url + '\'' +
                ", is_frozen=" + is_frozen +
                '}';
    }

}
