package tn.learniverse.services;

import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface IReclamation<T> {
    public void ajouter(Reclamation r, User user) throws SQLException;
    void modifier(int id,String Contenu) throws SQLException;
    void supprimer(T t);
    List<T> recuperer(User user) throws SQLException;
    public List<Reclamation> recupererReclamationsArchivéesFront(User user) throws SQLException;
    public void ArchiverFront(int id, int statut_archivation) throws SQLException;
    public List<Reclamation> recupererReclamationsBack() throws SQLException;
    public List<Reclamation> rechercher(String searchText, User user) throws SQLException;
    public List<Reclamation> rechercherBack(String searchText) throws SQLException;
    public List<Reclamation> recupererReclamationsBackPaginees(int page, int pageSize) throws SQLException;

}
