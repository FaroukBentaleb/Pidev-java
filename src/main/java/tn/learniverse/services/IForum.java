package tn.learniverse.services;

import java.sql.SQLException;
import java.util.List;

public interface IForum<T> {
    void ajouter(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<T> getAll() throws SQLException;
    T getById(int id);

}
