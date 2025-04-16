package tn.learniverse.entities;


public class Lesson {
    private int id;

    private String title;

    private String description;

    private String content;

    //c pas obligatoire
    private String attachment;

    private Course course;

    public Lesson() {
    }

    public Lesson(int id,String title, String description, String content, String attachment,Course course) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.attachment = attachment;
        this.course = course;
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
