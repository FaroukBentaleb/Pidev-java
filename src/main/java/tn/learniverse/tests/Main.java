package tn.learniverse.tests;

import tn.learniverse.entities.Lesson;
import tn.learniverse.entities.User;
import tn.learniverse.services.CourseService;
import tn.learniverse.services.LessonService;
import tn.learniverse.services.UserService;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        UserService userService = new UserService();
        User usr = new User("adem", "abidi", "adem@gmail.com","12345678", "1980-05-01", 123456789, "Instructor", "Experienced Instructor", 5, "Instructor", "resume.pdf", "profile.jpg", "facebook.com/adem", "instagram.com/adem", "linkedin.com/in/adem", true, 10, 0);

        try{
            System.out.println(userService.getAllUsers());
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }


        CourseService cs = new CourseService();
       // Course course = new Course("Java","Les principes de Java",23,20,"Intermediate","Tech & IT",null,null,false);
        LessonService ls = new LessonService();

        try {
            //cs.addCourse(course);
            // Course updatedCourse = new Course();
          /*  updatedCourse.setId(149);
            updatedCourse.setTitle("Introduction to AI");
            updatedCourse.setDescription("A beginner-friendly course on Artificial Intelligence.");
            updatedCourse.setDuration(40);
            updatedCourse.setPrice(99.99);
            updatedCourse.setLevel("Beginner");
            updatedCourse.setCategory("Technology");
            cs.updateCourse(updatedCourse);*/

            /* Course c = new Course();
            c.setId(149);
            cs.deleteCourse(c);*/

            System.out.println(cs.getAllCourses());

            List<Lesson> lessons = ls.getAllLessons();
            for (Lesson l : lessons) {
                System.out.println(l);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
