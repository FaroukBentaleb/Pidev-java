package tn.learniverse.services;

import tn.learniverse.entities.*;
import tn.learniverse.tools.DBConnection;
import tn.learniverse.tools.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUser <User>{
    Connection connection;
    public UserService() {
        connection = DBConnection.getInstance().getConnection();
    }
    @Override
    public void CreateAccount(User usr) throws SQLException {
        String query = "INSERT INTO User(nom,prenom,email,role,mdp)" + "VALUES (?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, usr.getNom());
        ps.setString(2, usr.getPrenom());
        ps.setString(3, usr.getEmail());
        ps.setString(4, usr.getRole());
        ps.setString(5, usr.getMdp());
        ps.executeUpdate();

    }

    @Override
    public void ModifyAccount(User usr)  throws SQLException {
        try {
            String query = "UPDATE User u SET "
                    + "nom = ?, "
                    + "prenom = ?, "
                    + "email = ?, "
                    + "date_de_naissance = ?, "
                    + "tel = ?, "
                    + "field = ?, "
                    + "description = ?, "
                    + "experience = ?, "
                    + "job = ?, "
                    + "resume = ?, "
                    + "picture = ?, "
                    + "facebook_link = ?, "
                    + "instagram_link = ?, "
                    + "linkedin_link = ?, "
                    + "verified = ?, "
                    + "logs = ?, "
                    + "ban = ? "
                    + "WHERE u.email = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            User currentUser = Session.getCurrentUser();
            ps.setString(1, usr.getNom());
            ps.setString(2, usr.getPrenom());
            ps.setString(3, usr.getEmail());
            ps.setString(4, usr.getDateDeNaissance() != null ? usr.getDateDeNaissance() : currentUser.getDateDeNaissance());
            ps.setInt(5, usr.getTel() != 0 ? usr.getTel() : currentUser.getTel());
            ps.setString(6, usr.getField() != null ? usr.getField() : currentUser.getField());
            ps.setString(7, usr.getDescription() != null ? usr.getDescription() : currentUser.getDescription());
            ps.setInt(8, usr.getExperience() != 0 ? usr.getExperience() : currentUser.getExperience());
            ps.setString(9, usr.getJob() != null ? usr.getJob() : currentUser.getJob());
            ps.setString(10, usr.getResume() != null ? usr.getResume() : currentUser.getResume());
            ps.setString(11, usr.getPicture() != null ? usr.getPicture() : currentUser.getPicture());
            ps.setString(12, usr.getFacebookLink() != null ? usr.getFacebookLink() : currentUser.getFacebookLink());
            ps.setString(13, usr.getInstagramLink() != null ? usr.getInstagramLink() : currentUser.getInstagramLink());
            ps.setString(14, usr.getLinkedinLink() != null ? usr.getLinkedinLink() : currentUser.getLinkedinLink());
            ps.setBoolean(15, usr.isVerified()); // Assuming false is a valid value
            ps.setInt(16, usr.getLogs() != 0 ? usr.getLogs() : currentUser.getLogs());
            ps.setInt(17, usr.getBan() != 0 ? usr.getBan() : currentUser.getBan());
            ps.setString(18, usr.getEmail());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Account Updated successfully");
    }

    @Override
    public void DeleteAccount(User u){
        try {
            String query = "DELETE FROM User u WHERE u.email = ? ";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, u.getEmail());
            ps.executeUpdate();
            System.out.println("Account deleted successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());;
        }
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM User";

        try (
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                User usr = new User();
                usr.setNom(rs.getString("nom"));
                usr.setPrenom(rs.getString("prenom"));
                usr.setEmail(rs.getString("email"));
                users.add(usr);
            }
        }
        return users;
    }

    @Override
    public User getUserByEmail(String email) {
        User usr = new User();
        try {
            String query = "SELECT * FROM User u WHERE u.email = ? ";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                usr = new User(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("mdp"),
                        rs.getString("date_de_naissance"),
                        rs.getInt("tel"),
                        rs.getString("field"),
                        rs.getString("description"),
                        rs.getInt("experience"),
                        rs.getString("job"),
                        rs.getString("resume"),
                        rs.getString("picture"),
                        rs.getString("facebook_link"),
                        rs.getString("instagram_link"),
                        rs.getString("linkedin_link"),
                        rs.getBoolean("verified"),
                        rs.getInt("logs"),
                        rs.getInt("ban")
                );
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return usr;
    }
}
