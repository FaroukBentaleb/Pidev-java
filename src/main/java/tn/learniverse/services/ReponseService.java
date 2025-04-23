package tn.learniverse.services;

import tn.learniverse.entities.*;
import tn.learniverse.tools.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService implements IReponse<Reponse>{

    Connection cnx;
    String sql;

    public ReponseService() {
        cnx = DBConnection.getInstance().getConnection();
    }
    @Override
    public void ajouter(Reponse r, User user, Reclamation reclamation) throws SQLException {
        String updateOld = "UPDATE reponse SET statut = 1 WHERE id_reclamation_id = ?";
        PreparedStatement updateStmt = cnx.prepareStatement(updateOld);
        updateStmt.setInt(1, reclamation.getId());
        updateStmt.executeUpdate();
        java.sql.Timestamp defaultModificationDate = java.sql.Timestamp.valueOf("1970-01-01 00:00:00");

        String sql = "INSERT INTO reponse (contenu, date_reponse, id_reclamation_id, date_modification, id_user_id, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = cnx.prepareStatement(sql);
        stmt.setString(1, r.getContenu());
        stmt.setTimestamp(2, new java.sql.Timestamp(r.getDateReponse().getTime()));
        stmt.setInt(3, r.getReclamation().getId());
        stmt.setTimestamp(4, defaultModificationDate);
        stmt.setInt(5, r.getUser().getId());
        stmt.setInt(6, r.getStatut());

        stmt.executeUpdate();
    }


    @Override
    public void modifier(int id, String contenu, User user, Reclamation reclamation) throws SQLException {
        String sql = "UPDATE reponse SET contenu = ?, date_modification = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setString(1, contenu);
        st.setInt(2, id);
        st.executeUpdate();
        System.out.println("Réponse modifiée !");
    }

    @Override
    public void supprimer(Reponse r) {
        try {
            sql = "DELETE FROM reponse WHERE id = ?";
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setInt(1, r.getId());
            st.executeUpdate();
            System.out.println("Réponse supprimée !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void updateStatut(int id, int statut) throws SQLException {
        String sql = "UPDATE reponse SET statut = ? WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setInt(1, statut);
        st.setInt(2, id);
        st.executeUpdate();
    }
}
