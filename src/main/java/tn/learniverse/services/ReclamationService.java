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
    public void ajouter(Reclamation r, User user) throws SQLException {
        sql = "INSERT INTO reclamation (titre, date_reclamation, contenu, statut, id_user_id, statut_archivation, statut_archivation_back, date_modification, fichier) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ste = cnx.prepareStatement(sql);

        java.sql.Timestamp defaultModificationDate = java.sql.Timestamp.valueOf("1970-01-01 00:00:00");
        ste.setString(1, r.getTitre());
        ste.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
        ste.setString(3, r.getContenu());
        ste.setString(4, "Non Traité");
        ste.setInt(5, user.getId());
        ste.setInt(6, 0);
        ste.setInt(7, 0);
        ste.setTimestamp(8, defaultModificationDate);
        ste.setString(9,r.getFichier());
        ste.executeUpdate();
        System.out.println("Réclamation ajoutée !");
    }
    @Override
    public void modifier(int id, String contenu) throws SQLException {
        sql = "UPDATE reclamation SET contenu = ?, date_modification = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setString(1, contenu);
        st.setInt(2, id);
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

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email,u.role " +
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
            u.setRole(rs.getString("role"));

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
    @Override
    public List<Reclamation> recupererReclamationsArchivéesFront(User user) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email " +
                "FROM reclamation r " +
                "JOIN user u ON r.id_user_id = u.id " +
                "WHERE r.statut_archivation = 1 AND r.id_user_id = ?";

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
    @Override
    public void ArchiverFront(int id, int statut_archivation) throws SQLException {
        sql = "UPDATE reclamation SET statut_archivation = ? WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setInt(1, statut_archivation);
        st.setInt(2, id);
        st.executeUpdate();
    }
    public void ArchiverBack(int id, int statut_archivation_back) throws SQLException {
        sql = "UPDATE reclamation SET statut_archivation_back = ? WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setInt(1, statut_archivation_back);
        st.setInt(2, id);
        st.executeUpdate();
        System.out.println("Réclamation archivée avec succès !");
    }

    public void updateStatut(int id, String statut) throws SQLException {
        sql = "UPDATE reclamation SET statut = ? WHERE id = ?";
        PreparedStatement st = cnx.prepareStatement(sql);
        st.setString(1, statut);
        st.setInt(2, id);
        st.executeUpdate();
        System.out.println("Statut de la réclamation mis à jour avec succès !");
    }

    @Override
    public List<Reclamation> recupererReclamationsBack() throws SQLException {
        return recupererReclamationsBackPaginees(1, 5);
    }
    @Override
    public List<Reclamation> rechercher(String searchText, User user) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
                "FROM reclamation r " +
                "JOIN user u ON r.id_user_id = u.id " +
                "WHERE r.statut_archivation = 0 AND r.id_user_id = ? " +
                "AND (LOWER(r.titre) LIKE LOWER(?) OR LOWER(r.contenu) LIKE LOWER(?))";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, user.getId());
        ste.setString(2, "%" + searchText + "%");
        ste.setString(3, "%" + searchText + "%");

        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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
    @Override
    public List<Reclamation> rechercherBack(String searchText) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
                "FROM reclamation r " +
                "JOIN user u ON r.id_user_id = u.id " +
                "WHERE r.statut_archivation_back = 0 " +
                "AND (LOWER(r.titre) LIKE LOWER(?) OR LOWER(r.contenu) LIKE LOWER(?))";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setString(1, "%" + searchText + "%");
        ste.setString(2, "%" + searchText + "%");

        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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

        // Récupérer les réponses pour chaque réclamation
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

    @Override
    public List<Reclamation> recupererReclamationsBackPaginees(int page, int pageSize) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
              "FROM reclamation r " +
              "LEFT JOIN user u ON r.id_user_id = u.id " +
              "WHERE r.statut_archivation_back = 0 " +
              "LIMIT ? OFFSET ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, pageSize);
        ste.setInt(2, offset);
        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id_user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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

    public int getTotalPages(int pageSize) throws SQLException {
        sql = "SELECT COUNT(*) FROM reclamation WHERE statut_archivation_back = 0";
        PreparedStatement ste = cnx.prepareStatement(sql);
        ResultSet rs = ste.executeQuery();
        
        if (rs.next()) {
            int totalReclamations = rs.getInt(1);
            return (int) Math.ceil((double) totalReclamations / pageSize);
        }
        return 0;
    }

    public List<Reclamation> trierReclamationsParDatePaginees(int page, int pageSize) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
              "FROM reclamation r " +
              "LEFT JOIN user u ON r.id_user_id = u.id " +
              "WHERE r.statut_archivation_back = 0 " +
              "ORDER BY r.date_reclamation DESC " +
              "LIMIT ? OFFSET ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, pageSize);
        ste.setInt(2, offset);
        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id_user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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

    public List<Reclamation> filtrerReclamationsParDate(Date date) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
              "FROM reclamation r " +
              "LEFT JOIN user u ON r.id_user_id = u.id " +
              "WHERE r.statut_archivation_back = 0 AND DATE(r.date_reclamation) = ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setDate(1, new java.sql.Date(date.getTime()));
        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id_user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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

        // Récupérer les réponses pour chaque réclamation
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

    public List<Reclamation> filtrerReclamationsParStatut(String statut) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
              "FROM reclamation r " +
              "LEFT JOIN user u ON r.id_user_id = u.id " +
              "WHERE r.statut_archivation_back = 0 AND r.statut = ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setString(1, statut);
        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id_user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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

    public List<Reclamation> filtrerReclamationsParDateEtStatut(Date date, String statut) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();

        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
              "FROM reclamation r " +
              "LEFT JOIN user u ON r.id_user_id = u.id " +
              "WHERE r.statut_archivation_back = 0 AND DATE(r.date_reclamation) = ? AND r.statut = ?";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setDate(1, new java.sql.Date(date.getTime()));
        ste.setString(2, statut);
        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id_user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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

    public int getCountByStatus(String status) throws SQLException {
        sql = "SELECT COUNT(*) FROM reclamation WHERE statut = ?";
        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setString(1, status);
        ResultSet rs = ste.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public List<MonthlyStats> getMonthlyStats(int year) throws SQLException {
        List<MonthlyStats> stats = new ArrayList<>();
        sql = "SELECT MONTH(date_reclamation) as month, COUNT(*) as count " +
              "FROM reclamation " +
              "WHERE YEAR(date_reclamation) = ? " +
              "GROUP BY MONTH(date_reclamation) " +
              "ORDER BY month";
        
        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, year);
        ResultSet rs = ste.executeQuery();
        
        while (rs.next()) {
            stats.add(new MonthlyStats(
                rs.getInt("month"),
                rs.getInt("count")
            ));
        }
        return stats;
    }

    public List<MonthlyStats> getMonthlyStatsByStatus(int year, String status) throws SQLException {
        List<MonthlyStats> stats = new ArrayList<>();
        sql = "SELECT MONTH(date_reclamation) as month, COUNT(*) as count " +
              "FROM reclamation " +
              "WHERE YEAR(date_reclamation) = ? AND statut = ? " +
              "GROUP BY MONTH(date_reclamation) " +
              "ORDER BY month";
        
        PreparedStatement ste = cnx.prepareStatement(sql);
        ste.setInt(1, year);
        ste.setString(2, status);
        ResultSet rs = ste.executeQuery();
        
        while (rs.next()) {
            stats.add(new MonthlyStats(
                rs.getInt("month"),
                rs.getInt("count")
            ));
        }
        return stats;
    }

    public static class MonthlyStats {
        private int month;
        private int count;

        public MonthlyStats(int month, int count) {
            this.month = month;
            this.count = count;
        }

        public String getMonthName() {
            return switch (month) {
                case 1 -> "Janvier";
                case 2 -> "Février";
                case 3 -> "Mars";
                case 4 -> "Avril";
                case 5 -> "Mai";
                case 6 -> "Juin";
                case 7 -> "Juillet";
                case 8 -> "Août";
                case 9 -> "Septembre";
                case 10 -> "Octobre";
                case 11 -> "Novembre";
                case 12 -> "Décembre";
                default -> "Inconnu";
            };
        }

        public int getCount() {
            return count;
        }
    }

    public List<Reclamation> recupererReclamationsArchivéesBack() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        sql = "SELECT r.*, u.id AS user_id, u.nom, u.prenom, u.email, u.role " +
                "FROM reclamation r " +
                "LEFT JOIN user u ON r.id_user_id = u.id " +
                "WHERE r.statut_archivation_back = 1";

        PreparedStatement ste = cnx.prepareStatement(sql);
        ResultSet rs = ste.executeQuery();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id_user_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));

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


