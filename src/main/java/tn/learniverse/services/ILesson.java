package tn.learniverse.services;

import java.sql.SQLException;
import java.util.List;

public interface ILesson<L> {
    void addLesson(L l) throws SQLException;
    void updateLesson(L l) throws SQLException;
    void deleteLesson(L l) throws SQLException;
    List<L> getAllLessons() throws SQLException;

}
