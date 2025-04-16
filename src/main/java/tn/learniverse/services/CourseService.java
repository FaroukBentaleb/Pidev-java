package tn.learniverse.services;

import tn.learniverse.entities.Course;
import tn.learniverse.tools.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseService implements ICourse<Course>{
    static Connection connection;
    public CourseService() {
        connection = DBConnection.getInstance().getConnection();
    }

    @Override
    public void addCourse(Course course) throws SQLException {
        String sql ="insert into course(title,description,duration,price,level,category,id_user)" +
                "values(?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, course.getTitle());
        ps.setString(2, course.getDescription());
        ps.setInt(3, course.getDuration());
        ps.setDouble(4, course.getPrice());
        ps.setString(5, course.getLevel());
        ps.setString(6, course.getCategory());
        ps.setInt(7, 7);
        ps.executeUpdate();
        System.out.println("Course added!!");
    }

    @Override
    public void updateCourse(Course course) throws SQLException {
        if (course.getId() <= 0) {
            throw new SQLException("Invalid course ID: " + course.getId());
        }

        String sql = "UPDATE course SET " +
                "title = ?, " +
                "description = ?, " +
                "duration = ?, " +
                "price = ?, " +
                "level = ?, " +
                "category = ? " +
                "WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getDuration());
            ps.setDouble(4, course.getPrice());
            ps.setString(5, course.getLevel());
            ps.setString(6, course.getCategory());
            ps.setInt(7, course.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Course update failed, no rows affected. Course ID: " + course.getId());
            } else {
                System.out.println("Course updated successfully! ID: " + course.getId() + ", Rows affected: " + rowsAffected);
            }
        } catch (SQLException e) {
            System.err.println("Error updating course with ID " + course.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCourse(Course c) throws SQLException {
        String query = "DELETE FROM course WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, c.getId());
        ps.executeUpdate();
        System.out.println("Course deleted successfully");
    }


    public List<Course> getAllCourses() throws SQLException {
        String sql = "SELECT * FROM course";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Course> courses = new ArrayList<>();

        while (rs.next()) {
            Course c = new Course();
            c.setId(rs.getInt("id"));
            c.setTitle(rs.getString("title"));
            c.setDescription(rs.getString("description"));
            c.setDuration(rs.getInt("duration"));
            c.setPrice(rs.getDouble("price"));
            c.setLevel(rs.getString("level"));
            c.setCategory(rs.getString("category"));
            courses.add(c);
        }

        // Fermer les ressources
        rs.close();
        ps.close();

        return courses;
    }

}
