package tn.learniverse.services;

import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.tools.DBConnection;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LessonService implements ILesson<Lesson> {
    static Connection connection;
    public LessonService() {
        connection = DBConnection.getInstance().getConnection();
    }

    @Override
    public void addLesson(Lesson lesson) throws SQLException {
        String sql = "INSERT INTO lesson (title, description, content, attachment, course_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, lesson.getTitle());
        ps.setString(2, lesson.getDescription());
        ps.setString(3, lesson.getContent());
        ps.setString(4, lesson.getAttachment());
        ps.setInt(5, lesson.getCourse().getId()); // jointure par objet
        ps.executeUpdate();
        System.out.println("Lesson added!");
    }

    @Override
    public void updateLesson(Lesson lesson) throws SQLException {
        String sql = "UPDATE lesson SET title = ?, description = ?, content = ?, attachment = ?, course_id = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, lesson.getTitle());
        ps.setString(2, lesson.getDescription());
        ps.setString(3, lesson.getContent());
        ps.setString(4, lesson.getAttachment());
        ps.setInt(5, lesson.getCourse().getId());
        ps.setInt(6, lesson.getId());
        ps.executeUpdate();
        System.out.println("Lesson updated!");
    }

    @Override
    public void deleteLesson(Lesson lesson) throws SQLException {
        String sql = "DELETE FROM lesson WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, lesson.getId());
        ps.executeUpdate();
        System.out.println("Lesson deleted!");
    }

    @Override
    public List<Lesson> getAllLessons() throws SQLException {
        String sql = "SELECT l.*, c.title as course_title, c.id as course_id FROM lesson l JOIN course c ON l.course_id = c.id";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Lesson> lessons = new ArrayList<>();
        while (rs.next()) {
            Course course = new Course();
            course.setId(rs.getInt("course_id"));
            course.setTitle(rs.getString("course_title"));

            Lesson lesson = new Lesson();
            lesson.setId(rs.getInt("id"));
            lesson.setTitle(rs.getString("title"));
            lesson.setDescription(rs.getString("description"));
            lesson.setContent(rs.getString("content"));
            lesson.setAttachment(rs.getString("attachment"));
            lesson.setCourse(course);

            lessons.add(lesson);
        }

        return lessons;
    }

    public List<Lesson> getLessonsByCourse(Course course) throws SQLException {
        String sql = "SELECT * FROM lesson WHERE course_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, course.getId());

        ResultSet rs = ps.executeQuery();
        List<Lesson> lessons = new ArrayList<>();

        while (rs.next()) {
            Lesson lesson = new Lesson();
            lesson.setId(rs.getInt("id"));
            lesson.setTitle(rs.getString("title"));
            lesson.setDescription(rs.getString("description"));
            lesson.setContent(rs.getString("content"));
            lesson.setAttachment(rs.getString("attachment"));
            lesson.setCourse(course); // on garde l'objet course passé

            lessons.add(lesson);
        }

        return lessons;
    }

}