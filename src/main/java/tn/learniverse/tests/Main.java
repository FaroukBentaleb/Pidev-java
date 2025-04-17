package tn.learniverse.tests;

import tn.learniverse.entities.*;
import tn.learniverse.services.*;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        User usr = new User("adem", "abidi", "adem@gmail.com","Admin","12345678", "1980-05-01", 123456789, "Instructor", "Experienced Instructor", 5, "Instructor", "resume.pdf", "profile.jpg", "facebook.com/adem", "instagram.com/adem", "linkedin.com/in/adem", true, 10, 0);
        try{
            System.out.println(userService.getAllUsers());
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
