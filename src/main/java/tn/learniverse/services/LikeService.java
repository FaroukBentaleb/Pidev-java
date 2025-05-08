package tn.learniverse.services;

import tn.learniverse.entities.Like;
import tn.learniverse.entities.Poste;
import tn.learniverse.entities.user;
import tn.learniverse.tools.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeService implements IForum<Like> {

    private Connection cnx;

    public LikeService() {
        cnx = DBConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Like like) throws SQLException {
        String query = "INSERT INTO `like` (type, id_user_id, id_poste_id) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, like.getType());
        ps.setInt(2, like.getUser().getId());
        ps.setInt(3, like.getPoste().getId());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Like like) throws SQLException {
        String query = "UPDATE `like` SET type = ?, id_user_id = ?, id_poste_id = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, like.getType());
        ps.setInt(2, like.getUser().getId());
        ps.setInt(3, like.getPoste().getId());
        ps.setInt(4, like.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM `like` WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Like> getAll() throws SQLException {
        List<Like> likes = new ArrayList<>();
        String query = "SELECT * FROM `like`";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Like like = new Like();
            like.setId(rs.getInt("id"));
            like.setType(rs.getString("type"));

            user u = new user();
            u.setId(rs.getInt("id_user_id"));
            like.setUser(u);

            Poste p = new Poste();
            p.setId(rs.getInt("id_poste_id"));
            like.setPoste(p);

            likes.add(like);
        }
        return likes;
    }

    @Override
    public Like getById(int id) {
        try {
            String query = "SELECT * FROM `like` WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Like like = new Like();
                like.setId(rs.getInt("id"));
                like.setType(rs.getString("type"));

                user u = new user();
                u.setId(rs.getInt("id_user_id"));
                like.setUser(u);

                Poste p = new Poste();
                p.setId(rs.getInt("id_poste_id"));
                like.setPoste(p);

                return like;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Méthodes personnalisées : compter likes par type etc.

    public Map<String, Integer> compterLikesParType(Poste poste) throws SQLException {
        Map<String, Integer> compteur = new HashMap<>();

        String query = "SELECT type, COUNT(*) AS count FROM `like` WHERE id_poste_id = ? GROUP BY type";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, poste.getId());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            compteur.put(rs.getString("type"), rs.getInt("count"));
        }
        return compteur;
    }

    public boolean existeDeja(Poste poste, user user, String type) throws SQLException {
        String query = "SELECT COUNT(*) FROM `like` WHERE id_poste_id = ? AND id_user_id = ? AND type = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, poste.getId());
        ps.setInt(2, user.getId());
        ps.setString(3, type);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    public List<Like> getLikesParPoste(Poste poste) throws SQLException {
        List<Like> likes = new ArrayList<>();
        String query = "SELECT * FROM `like` WHERE id_poste_id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, poste.getId());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Like like = new Like();
            like.setId(rs.getInt("id"));
            like.setType(rs.getString("type"));

            user u = new user();
            u.setId(rs.getInt("id_user_id"));
            like.setUser(u);

            Poste p = new Poste();
            p.setId(rs.getInt("id_poste_id"));
            like.setPoste(p);

            likes.add(like);
        }
        return likes;
    }

    public Like getLike(int idPoste, int idUser, String type) throws SQLException {
        String req = "SELECT * FROM `like` WHERE id_poste_id = ? AND id_user_id = ? AND type = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idPoste);
        ps.setInt(2, idUser);
        ps.setString(3, type);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Like like = new Like();
            like.setId(rs.getInt("id"));

            user u = new user();
            u.setId(rs.getInt("id_user_id"));
            like.setUser(u);

            Poste p = new Poste();
            p.setId(rs.getInt("id_poste_id"));
            like.setPoste(p);

            like.setType(rs.getString("type"));
            return like;
        } else {
            return null; // Aucun like trouvé
        }
    }


}
