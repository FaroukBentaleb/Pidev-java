package tn.learniverse.services;

import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface IReponse<T> {
    void ajouter(T t) throws SQLException;
    void modifier(int id, String contenu) throws SQLException;
    void supprimer(T t);
    List<T> recuperer(Reclamation reclamation) throws SQLException;
    List<T> recupererParUser(User user) throws SQLException;

}
