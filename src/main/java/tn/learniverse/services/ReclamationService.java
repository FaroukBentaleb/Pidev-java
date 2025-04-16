package tn.learniverse.services;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import tn.learniverse.entities.*;
import tn.learniverse.tools.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ReclamationService implements IReclamation<Reclamation> {
    Connection cnx;
    String sql;

    public ReclamationService() {
        cnx = DBConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reclamation r) throws SQLException {
        sql = "INSERT INTO reclamation (titre, date_reclamation, contenu, statut, id_user_id, statut_archivation, statut_archivation_back, date_modification, fichier) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setString(1, r.getTitre());
        ste.setTimestamp(2, new java.sql.Timestamp(r.getDateReclamation().getTime()));
        ste.setString(3, r.getContenu());
        ste.setString(4, r.getStatut());
        ste.setInt(6, r.getStatutArchivation());
        ste.setInt(7, r.getStatutArchivationBack());
        ste.setDate(8, new java.sql.Date(r.getDateModification().getTime()));
        ste.setString(9, r.getFichier());
        ste.setInt(5, r.getUser().getId());
        ste.executeUpdate();
        System.out.println("Réclamation ajoutée !");
    }

    @Override
    public void modifier(int id,String contenu) throws SQLException {
        sql = "UPDATE reclamation SET contenu = '"+contenu+"' WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setInt(1, id);
        st.executeUpdate();
        System.out.println("Réclamation modifiée !");
    }

    @Override
    public void supprimer(Reclamation r) {
        try {
            sql = "DELETE FROM reclamation WHERE id = ?";
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setInt(1, r.getId());
            st.executeUpdate();
            System.out.println("Réclamation supprimée !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Reclamation> recuperer(User user) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email " +
                "FROM reclamation r " +
                "JOIN user u ON r.id_user_id = u.id " +
                "WHERE r.statut_archivation = 0 AND r.id_user_id = ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, user.getId());

        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));

            Reclamation rec = new Reclamation(
                    rs.getString("titre"),
                    rs.getTimestamp("date_reclamation"),
                    rs.getString("contenu"),
                    rs.getString("statut"),
                    u,
                    rs.getInt("statut_archivation"),
                    rs.getInt("statut_archivation_back"),
                    rs.getTimestamp("date_modification"),
                    rs.getString("fichier")
            );
            rec.setId(rs.getInt("id"));
            reclamations.add(rec);
        }
        for (Reclamation reclamation : reclamations) {
            String sql1 = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
                    "FROM reponse r " +
                    "LEFT JOIN user u ON r.id_user_id = u.id " +
                    "WHERE r.id_reclamation_id = ?";


            PreparedStatement ste1 = cnx.prepareStatement(sql1);
            ste1.setInt(1, reclamation.getId());
            ResultSet rs1 = ste1.executeQuery();
            List<Reponse> reponses = new ArrayList<>();
            while (rs1.next()) {
                User userReponse = new User();
                userReponse.setId(rs1.getInt("user_id"));
                userReponse.setNom(rs1.getString("nom"));
                userReponse.setPrenom(rs1.getString("prenom"));
                userReponse.setEmail(rs1.getString("email"));
                userReponse.setRole(rs1.getString("role"));
                Reponse rep = new Reponse(
                        rs1.getString("contenu"),
                        rs1.getTimestamp("date_reponse"),
                        reclamation,
                        rs1.getTimestamp("date_modification"),
                        userReponse,
                        rs1.getInt("statut")
                );
                rep.setId(rs1.getInt("id"));
                reponses.add(rep);
            }
            reclamation.setReponses(reponses);
        }

        return reclamations;
    }


}


