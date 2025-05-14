package tn.learniverse.services;

import tn.learniverse.entities.Poste;
import tn.learniverse.entities.User;
import tn.learniverse.entities.Commentaire;
import tn.learniverse.tools.DBConnection;
import tn.learniverse.tools.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosteService implements IForum<Poste> {

    private Connection cnx;

    public PosteService() {
        cnx = DBConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Poste p) {
        try {
            // Fixer l'utilisateur manuellement (id = 1)
            User u = new User();
            if( Session.getCurrentUser().getEmail()!=null ) {
                u = Session.getCurrentUser();
            }
            else{
                u.setId(1);
            }
            p.setUser(u);
            // Générer la date du jour si non spécifiée
            if (p.getDatePost() == null || p.getDatePost().isEmpty()) {
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                p.setDatePost(currentDate);
            }
            // Forcer visible à true si ce n'est pas déjà fait
            p.setVisible(true);
            // Initialiser nbCom à 0 si non défini
            if (p.getNbCom() == 0) {
                p.setNbCom(0);
            }

            String sql = "INSERT INTO poste (id_user_id, titre, contenu, date_post, visible, nb_com, categorie, photo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, p.getUser().getId());
            ps.setString(2, p.getTitre());
            ps.setString(3, p.getContenu());
            ps.setDate(4, Date.valueOf(p.getDatePost()));
            ps.setBoolean(5, p.isVisible());
            ps.setInt(6, p.getNbCom());
            ps.setString(7, p.getCategorie());
            ps.setString(8, p.getPhoto());

            ps.executeUpdate();
            System.out.println("✅ Poste ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du poste : " + e.getMessage());
        }
    }



    @Override
    public void modifier(Poste p) {
        String sql = "UPDATE poste SET titre=?, contenu=?, categorie=?, photo=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getContenu());
            pst.setString(3, p.getCategorie());
            pst.setString(4, p.getPhoto());
            pst.setInt(5, p.getId());
            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("La modification a échoué, aucun poste trouvé avec cet ID");
            }
            System.out.println("✅ Poste modifié !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification poste : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimer(int id) {
        try {
            // Étape 1 : Supprimer les commentaires liés à ce poste
            String deleteCommentaires = "DELETE FROM commentaire WHERE id_poste_id = ?";
            PreparedStatement ps1 = cnx.prepareStatement(deleteCommentaires);
            ps1.setInt(1, id);
            ps1.executeUpdate();

            // Étape 2 : Supprimer le poste lui-même
            String deletePoste = "DELETE FROM poste WHERE id = ?";
            PreparedStatement ps2 = cnx.prepareStatement(deletePoste);
            ps2.setInt(1, id);
            ps2.executeUpdate();

            System.out.println("✅ Poste (et ses commentaires) supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression poste : " + e.getMessage());
        }
    }





    @Override
    public Poste getById(int id) {
        String sql = "SELECT * FROM poste WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Poste p = new Poste();
                p.setId(rs.getInt("id"));
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                p.setDatePost(rs.getDate("date_post").toString());
                p.setVisible(rs.getBoolean("visible"));
                p.setNbCom(rs.getInt("nb_com"));
                p.setCategorie(rs.getString("categorie"));
                p.setPhoto(rs.getString("photo"));
                // Tu peux aussi charger l'objet User si tu veux
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Poste> getAll() {
        List<Poste> postes = new ArrayList<>();
        try {
            // Utilisation d'une seule requête avec jointure pour récupérer les données du poste et de l'utilisateur
            String sql = "SELECT p.*, u.nom AS user_nom, u.prenom AS user_prenom FROM poste p JOIN user u ON p.id_user_id = u.id ORDER BY id DESC";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Poste p = new Poste();

                p.setId(rs.getInt("id"));

                // Récupérer les informations du poste
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                p.setDatePost(rs.getString("date_post"));
                p.setVisible(rs.getBoolean("visible"));
                p.setNbCom(rs.getInt("nb_com"));
                p.setCategorie(rs.getString("categorie"));
                p.setPhoto(rs.getString("photo"));

                // Récupérer les informations de l'utilisateur associé au poste
                User u = new User();
                u.setId(rs.getInt("id_user_id"));
                u.setNom(rs.getString("user_nom"));
                u.setPrenom(rs.getString("user_prenom"));

                // Associer l'utilisateur au poste
                p.setUser(u);

                // Ajouter le poste à la liste
                postes.add(p);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAll postes : " + e.getMessage());
            e.printStackTrace();
        }
        return postes;
    }




}
