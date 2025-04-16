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
    public void ajouter(Reponse r) throws SQLException {
        sql = "INSERT INTO reponse (contenu, date_reponse, date_modification, statut, id_user_id, id_reclamation_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setString(1, r.getContenu());
        ste.setTimestamp(2, new Timestamp(r.getDateReponse().getTime()));
        ste.setTimestamp(3, new Timestamp(r.getDateModification().getTime()));
        ste.setInt(4, r.getStatut());
        ste.setInt(5, r.getUser().getId());
        ste.setInt(6, r.getReclamation().getId());
        ste.executeUpdate();
        System.out.println("Réponse ajoutée !");
    }
    @Override
    public void modifier(int id, String contenu) throws SQLException {
        sql = "UPDATE reponse SET contenu = ?, date_modification = CURRENT_TIMESTAMP WHERE id = ?";
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

}
