package tn.learniverse.services;

import tn.learniverse.entities.Course;

import java.sql.SQLException;
import java.util.List;

public interface ICourse <C>{
    void addCourse (C c) throws SQLException;
    void updateCourse (C c) throws SQLException;
    void deleteCourse (C c) throws SQLException;
    List<C> getAllCourses() throws SQLException;

    }
