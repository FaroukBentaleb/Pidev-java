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
            c.setIs_frozen(rs.getBoolean("is_frozen"));
            courses.add(c);
        }

        // Fermer les ressources
        rs.close();
        ps.close();

        return courses;
    }

    public void toggleCourseVisibility(Course course) throws SQLException {
        String sql = "UPDATE course SET is_frozen = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, !course.isIs_frozen());  // Inverse l'état actuel
            ps.setInt(2, course.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                // Mettre à jour l'objet local
                course.setIs_frozen(!course.isIs_frozen());
                System.out.println("Course visibility toggled for ID: " + course.getId() +
                        " - Is now " + (course.isIs_frozen() ? "frozen" : "unfrozen"));
            }
        }
    }

    public List<Course> getVisibleCourses() throws SQLException {
        String sql = "SELECT * FROM course WHERE is_frozen = false";
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
            c.setIs_frozen(rs.getBoolean("is_frozen"));
            courses.add(c);
        }

        // Close resources
        rs.close();
        ps.close();

        return courses;
    }
    public Course getCourseById(int id) throws SQLException {
        Connection connection = DBConnection.getConnection();
        Course course = null;
        String query = "SELECT * FROM courses WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    course = new Course();
                    course.setId(resultSet.getInt("id"));
                    course.setTitle(resultSet.getString("title"));
                    course.setDescription(resultSet.getString("description"));
                    course.setCategory(resultSet.getString("category"));
                    course.setLevel(resultSet.getString("level"));
                    course.setDuration(resultSet.getInt("duration"));
                    course.setPrice(resultSet.getDouble("price"));

                }
            }
        }

        return course;
    }
    public List<Course> getRecommendedCourses(int userId) throws SQLException {
        List<Course> recommended = new ArrayList<>();

        // 1. Récupérer les cours des catégories favorites de l'utilisateur mais qui ne sont PAS déjà favoris
        String query = "SELECT DISTINCT c.* FROM course c " +
                "JOIN favorites f ON c.category = (SELECT category FROM course WHERE id = f.course_id LIMIT 1) " +
                "WHERE f.user_id = ? AND c.id NOT IN (SELECT course_id FROM favorites WHERE user_id = ?) " +
                "AND c.is_frozen = false " +
                "ORDER BY RAND() LIMIT 3";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                recommended.add(createCourseFromResultSet(rs));
            }
        }

        // Si moins de 3 recommandations, compléter avec des cours aléatoires non favoris
        if (recommended.size() < 3) {
            String fallbackQuery = "SELECT * FROM course WHERE is_frozen = false " +
                    "AND id NOT IN (SELECT course_id FROM favorites WHERE user_id = ?) " +
                    "ORDER BY RAND() LIMIT ?";

            try (PreparedStatement stmt = connection.prepareStatement(fallbackQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, 3 - recommended.size());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    recommended.add(createCourseFromResultSet(rs));
                }
            }
        }

        return recommended;
    }

    public List<Course> getPopularCourses(int limit) throws SQLException {
        List<Course> popular = new ArrayList<>();
        String query = "SELECT c.*, COUNT(e.id) as enroll_count FROM course c " +
                "LEFT JOIN enrollments e ON c.id = e.course_id " +
                "WHERE c.is_frozen = false " +
                "GROUP BY c.id " +
                "ORDER BY enroll_count DESC " +
                "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                popular.add(createCourseFromResultSet(rs));
            }
        }
        return popular;
    }

    private Course createCourseFromResultSet(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setDuration(rs.getInt("duration"));
        c.setPrice(rs.getDouble("price"));
        c.setLevel(rs.getString("level"));
        c.setCategory(rs.getString("category"));
        c.setIs_frozen(rs.getBoolean("is_frozen"));
        return c;
    }
}