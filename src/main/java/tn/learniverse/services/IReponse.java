package tn.learniverse.services;

import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.Reponse;
import tn.learniverse.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface IReponse<T> {
    public void ajouter(Reponse r, User user, Reclamation reclamation) throws SQLException;
    public void modifier(int id, String contenu, User user, Reclamation reclamation) throws SQLException;
    void supprimer(T t);
    public void updateStatut(int id, int statut) throws SQLException;

}
