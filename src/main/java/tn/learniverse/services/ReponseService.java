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
    @Override
    public List<Reponse> recuperer(Reclamation reclamation) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();

        String sql = "SELECT * FROM reponse WHERE id_reclamation_id = ?";
        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, reclamation.getId());

        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            Reponse rep = new Reponse(
                    rs.getString("contenu"),
                    rs.getTimestamp("date_reponse"),
                    reclamation,
                    rs.getTimestamp("date_modification"),
                    null, // pas de User
                    rs.getInt("statut")
            );
            rep.setId(rs.getInt("id"));
            reponses.add(rep);
        }

        return reponses;
    }


    @Override
    public List<Reponse> recupererParUser(User user) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
 
        String sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, rec.id AS reclamation_id, rec.titre " +
                "FROM reponse r " +
                "JOIN user u ON r.id_user_id = u.id " +
                "JOIN reclamation rec ON r.id_reclamation_id = rec.id " +
                "WHERE r.id_user_id = ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, user.getId());

        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));

            Reclamation rec = new Reclamation();
            rec.setId(rs.getInt("reclamation_id"));
            rec.setTitre(rs.getString("titre"));

            Reponse reponse = new Reponse(
                    rs.getString("contenu"),
                    rs.getTimestamp("date_reponse"),
                    rec,
                    rs.getTimestamp("date_modification"),
                    u,
                    rs.getInt("statut")
            );
            reponse.setId(rs.getInt("id"));

            reponses.add(reponse);
        }

        return reponses;
    }

}
