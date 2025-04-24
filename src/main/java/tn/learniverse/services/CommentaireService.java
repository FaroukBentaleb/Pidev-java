package tn.learniverse.services;

import tn.learniverse.entities.Commentaire;
import tn.learniverse.entities.*;
import tn.learniverse.entities.Poste;

import tn.learniverse.tools.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IForum<Commentaire> {

    private Connection cnx;

    public CommentaireService() {
        cnx = DBConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Commentaire c) {
        try {
            // Fixer l'utilisateur manuellement
            User u = new User();
            u.setId(1); // utilisateur par défaut
            c.setUser(u);

            if (c.getDateComment() == null || c.getDateComment().isEmpty()) {
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                c.setDateComment(currentDate);
            }

            c.setVisible(true);


            String sql = "INSERT INTO commentaire (contenu, date_comment, id_poste_id, id_user_id, visible, gifurl) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getContenu());
            ps.setDate(2, Date.valueOf(c.getDateComment()));
            ps.setInt(3, c.getPoste().getId());
            ps.setInt(4, c.getUser().getId());
            ps.setBoolean(5, c.isVisible());
            ps.setString(6, c.getGifurl());

            ps.executeUpdate();
            System.out.println("✅ Commentaire ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du commentaire : " + e.getMessage());
        }
    }


    @Override
    public void modifier(Commentaire c) {
        String sql = "UPDATE commentaire SET contenu=?, date_comment=?, visible=?, gifurl=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, c.getContenu());
            pst.setDate(2, Date.valueOf(c.getDateComment()));
            pst.setBoolean(3, c.isVisible());
            pst.setString(4, c.getGifurl());
            pst.setInt(5, c.getId());
            pst.executeUpdate();
            System.out.println("✅ Commentaire modifié !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            System.out.println("Exécution de la suppression pour commentaire ID: " + id);
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✅ Commentaire ID " + id + " supprimé avec succès. Lignes affectées: " + rowsDeleted);
            } else {
                System.out.println("⚠️ Aucun commentaire trouvé avec l'ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL lors de la suppression: ");
            System.err.println("Code d'erreur SQL: " + e.getErrorCode());
            System.err.println("Message SQL: " + e.getMessage());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
        }
    }

    @Override
    public Commentaire getById(int id) {
        String sql = "SELECT * FROM commentaire WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDateComment(rs.getDate("date_comment").toString());
                c.setVisible(rs.getBoolean("visible"));
                c.setGifurl(rs.getString("gifurl"));
                // Charger les objets Poste et User si nécessaire
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Commentaire> getAll() {
        List<Commentaire> commentaires = new ArrayList<>();
        try {
            String sql = "SELECT * FROM commentaire";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDateComment(rs.getString("date_comment"));
                c.setVisible(rs.getBoolean("visible"));
                c.setGifurl(rs.getString("gifurl"));

                // Créer et associer l'utilisateur
                User u = new User();
                u.setId(rs.getInt("id_user_id"));
                c.setUser(u);

                // Créer et associer le poste
                Poste p = new Poste();
                p.setId(rs.getInt("id_poste_id"));
                c.setPoste(p);

                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAll commentaires : " + e.getMessage());
            e.printStackTrace();
        }
        return commentaires;
    }

    public List<Commentaire> getByPosteId(int posteId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT c.*, u.prenom, u.nom FROM commentaire c " +
                "JOIN user u ON c.id_user_id = u.id " +
                "WHERE c.id_poste_id = ? " +
                "ORDER BY c.date_comment DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, posteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDateComment(rs.getString("date_comment"));
                c.setGifurl(rs.getString("gifurl"));

                // Créer l'objet Poste avec juste l'ID
                Poste p = new Poste();
                p.setId(posteId);
                c.setPoste(p);

                // Créer l'objet User
                User u = new User();
                u.setId(rs.getInt("id_user_id"));
                u.setPrenom(rs.getString("prenom"));
                u.setNom(rs.getString("nom"));
                c.setUser(u);

                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commentaires: " + e.getMessage());
        }

        return commentaires;
    }

}
