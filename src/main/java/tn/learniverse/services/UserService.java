package tn.learniverse.services;

import tn.learniverse.entities.user;
import tn.learniverse.tools.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUser <user>{
    Connection connection;
    public UserService() {
        connection = DBConnection.getInstance().getConnection();
    }
    @Override
    public void CreateAccount(user usr) throws SQLException {
        String query = "INSERT INTO user(nom,prenom,email,role,mdp)" + "VALUES (?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, usr.getNom());
        ps.setString(2, usr.getPrenom());
        ps.setString(3, usr.getEmail());
        ps.setString(4, usr.getRole());
        ps.setString(5, usr.getMdp());
        ps.executeUpdate();
        System.out.println("Account created successfully");

    }

    @Override
    public void ModifyAccount(user usr)  throws SQLException {
        String query = "UPDATE user u SET "
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
        ps.setString(1, usr.getNom());
        ps.setString(2, usr.getPrenom());
        ps.setString(3, usr.getEmail());
        ps.setString(4, usr.getDateDeNaissance());
        ps.setInt(5, usr.getTel());
        ps.setString(6, usr.getField());
        ps.setString(7, usr.getDescription());
        ps.setInt(8, usr.getExperience());
        ps.setString(9, usr.getJob());
        ps.setString(10, usr.getResume());
        ps.setString(11, usr.getPicture());
        ps.setString(12, usr.getFacebookLink());
        ps.setString(13, usr.getInstagramLink());
        ps.setString(14, usr.getLinkedinLink());
        ps.setBoolean(15, usr.isVerified());
        ps.setInt(16, usr.getLogs());
        ps.setInt(17, usr.getBan());
        ps.setString(18, usr.getEmail());

        ps.executeUpdate();
        System.out.println("Account Updated successfully");
    }

    @Override
    public void DeleteAccount(user u)  throws SQLException{
        String query = "DELETE FROM user u WHERE u.email = ? ";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, u.getEmail());
        ps.executeUpdate();
        System.out.println("Account deleted successfully");
    }

    @Override
    public List<user> getAllUsers() throws SQLException {
        List<user> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                user usr = new user();
                usr.setNom(rs.getString("nom"));
                usr.setPrenom(rs.getString("prenom"));
                usr.setEmail(rs.getString("email"));
                users.add(usr);
            }
        }
        return users;
    }

    @Override
    public user getUserById(int id) {
        return null;
    }
}
