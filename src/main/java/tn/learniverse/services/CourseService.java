package tn.learniverse.services;

import tn.learniverse.entities.Course;
import tn.learniverse.tools.DBConnection;
import tn.learniverse.tools.Session;

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
        ps.setInt(7, Session.getCurrentUser().getId());
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
            c.setId_user(rs.getInt("id_user"));
            courses.add(c);
        }

        // Close resources
        rs.close();
        ps.close();

        return courses;
    }
    public List<Course> getVisibleCoursesForInstructor(int InstructorId) throws SQLException {
        String sql = "SELECT * FROM course WHERE is_frozen = false AND id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, InstructorId);
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
    public List<Course> getVisibleCoursesForStudent(int StudentId) throws SQLException {
        String sql = "SELECT * FROM course WHERE (is_frozen = false) AND id NOT IN (SELECT id_course_id FROM subscription WHERE id_user_id = ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, StudentId);
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
        String query = "SELECT * FROM course WHERE id = ?";

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
        System.out.println("Getting recommended courses for user: " + userId);

        try {
            // 1. D'abord, essayer de récupérer les cours des catégories que l'utilisateur a aimées
            String query = "SELECT DISTINCT c.* FROM course c " +
                    "WHERE c.category IN (SELECT DISTINCT c2.category FROM course c2 " +
                    "JOIN favorites f ON c2.id = f.course_id " +
                    "WHERE f.user_id = ?) " +
                    "AND c.id NOT IN (SELECT course_id FROM favorites WHERE user_id = ?) " +
                    "AND c.is_frozen = false " +
                    "ORDER BY RAND() LIMIT 3";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Course course = createCourseFromResultSet(rs);
                    recommended.add(course);
                    System.out.println("Added recommended course from favorite categories: " + course.getTitle());
                }
            }

            // 2. Si pas assez de recommandations, ajouter des cours aléatoires
            if (recommended.size() < 3) {
                System.out.println("Not enough recommendations, adding random courses");
                String fallbackQuery = "SELECT * FROM course " +
                        "WHERE is_frozen = false " +
                        "AND id NOT IN (SELECT course_id FROM favorites WHERE user_id = ?) " +
                        "ORDER BY RAND() LIMIT ?";

                try (PreparedStatement stmt = connection.prepareStatement(fallbackQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, 3 - recommended.size());
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Course course = createCourseFromResultSet(rs);
                        if (!recommended.contains(course)) {
                            recommended.add(course);
                            System.out.println("Added random course: " + course.getTitle());
                        }
                    }
                }
            }

            // 3. Si toujours pas assez de cours, prendre n'importe quel cours visible
            if (recommended.size() < 3) {
                System.out.println("Still not enough courses, adding any visible course");
                String finalQuery = "SELECT * FROM course WHERE is_frozen = false ORDER BY RAND() LIMIT ?";
                
                try (PreparedStatement stmt = connection.prepareStatement(finalQuery)) {
                    stmt.setInt(1, 3 - recommended.size());
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Course course = createCourseFromResultSet(rs);
                        if (!recommended.contains(course)) {
                            recommended.add(course);
                            System.out.println("Added any visible course: " + course.getTitle());
                        }
                    }
                }
            }

            System.out.println("Total recommended courses: " + recommended.size());
            return recommended;

        } catch (SQLException e) {
            System.err.println("Error in getRecommendedCourses: " + e.getMessage());
            e.printStackTrace();
            
            // En cas d'erreur, retourner des cours aléatoires
            String emergencyQuery = "SELECT * FROM course WHERE is_frozen = false ORDER BY RAND() LIMIT 3";
            try (PreparedStatement stmt = connection.prepareStatement(emergencyQuery)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    recommended.add(createCourseFromResultSet(rs));
                }
            }
            
            return recommended;
        }
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

    public List<Course> searchCourses(String keyword) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE LOWER(title) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) OR LOWER(level) LIKE LOWER(?)";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);

            String queryParam = "%" + keyword + "%";
            stmt.setString(1, queryParam);
            stmt.setString(2, queryParam);
            stmt.setString(3, queryParam);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setTitle(rs.getString("title"));
                course.setCategory(rs.getString("category"));
                course.setLevel(rs.getString("level"));
                course.setDescription(rs.getString("description"));
                course.setIs_frozen(rs.getBoolean("is_frozen"));
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des cours : " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException ignore) {}
            // 🚫 Pas de conn.close() ici
        }

        return courses;
    }

    public static List<Course> getPurchasedCourses() throws SQLException {
        String sql = "SELECT c.* FROM course c INNER JOIN subscription s ON s.id_course_id = c.id WHERE c.is_frozen = false AND s.id_user_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, Session.getCurrentUser().getId());
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
            c.setId_user(rs.getInt("id_user"));
            courses.add(c);
        }

        // Close resources
        rs.close();
        ps.close();

        return courses;
    }



}