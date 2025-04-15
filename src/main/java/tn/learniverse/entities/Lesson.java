package tn.learniverse.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Lesson {
    private int id;
    @NotBlank(message = "Le titre de la leçon est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 40, max = 300, message = "Le titre doit contenir entre 5 et 100 caractères")
    private String description;

    @NotBlank(message = "Le contenu de la leçon est requis")
    private String content;

    //c pas obligatoire
    private String attachment;

    private Course course;

    public Lesson() {
    }

    public Lesson(String title, String description, String content, String attachment,Course course) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.attachment = attachment;
        this.course = course;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Lesson{" +
                " course=" + course.getTitle() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", attachment='" + attachment + '\'' +
                '}';
    }
}
