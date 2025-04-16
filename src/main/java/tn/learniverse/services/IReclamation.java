package tn.learniverse.services;

import tn.learniverse.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface IReclamation<T> {
    void ajouter(T t) throws SQLException;
    void modifier(int id,String Contenu) throws SQLException;
    void supprimer(T t);
    List<T> recuperer(User user) throws SQLException;

}
