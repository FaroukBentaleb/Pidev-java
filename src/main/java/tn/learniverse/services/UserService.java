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
        String query = "INSERT INTO User(nom,prenom,email,role,mdp,verified,logs,ban)" + "VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, usr.getNom());
        ps.setString(2, usr.getPrenom());
        ps.setString(3, usr.getEmail());
        ps.setString(4, usr.getRole());
        ps.setString(5, usr.getMdp());
        ps.setBoolean(6, false);
        ps.setInt(7, 5);
        ps.setInt(8, 0);
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
                usr.setId(Integer.parseInt(rs.getString("id")));
                usr.setNom(rs.getString("nom"));
                usr.setPrenom(rs.getString("prenom"));
                usr.setEmail(rs.getString("email"));
                usr.setRole(rs.getString("role"));
                usr.setBan(Integer.parseInt(rs.getString("ban")));
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
                        rs.getInt("id"),
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
                usr.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            usr = null;
        }
        return usr;
    }
    @Override
    public void banUser(int userId) throws SQLException {
        String query = "UPDATE User SET ban = 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();

        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public void activateUser(int userId) throws SQLException {
        String query = "UPDATE User SET ban = 0 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public int getUserIdByEmail(String email) {
        int userId = 0;
        try {
            String query = "SELECT id FROM User u WHERE u.email = ? ";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                userId = Integer.parseInt(rs.getString("id"));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return userId;
    }
    @Override
    public boolean ChangePwd(String email, String pwd)  throws SQLException {
        try {
            String query = "UPDATE User u SET "
                    + "u.mdp = ? ,"
                    + "u.logs = ? "
                    + "WHERE u.email = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, pwd);
            ps.setInt(2, 5);
            ps.setString(3, email);
            ps.executeUpdate();
            System.out.println("Password Changed successfully");
            return true;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }
    @Override
    public void MnsLogs(String email,int logs)  throws SQLException {
        try {
            int log = 0;
            if(logs>0){
                log = logs-1;
            }
            String query = "UPDATE User u SET "
                    + "u.logs = ? "
                    + "WHERE u.email = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, log);
            ps.setString(2, email);
            ps.executeUpdate();
            System.out.println("Logs -1");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void GoogleAuthStore(String google_auth) throws SQLException {
        User currentUser = Session.getCurrentUser();
        String updateQuery = "UPDATE user SET google_authenticator_secret = ? WHERE email = ?";
        PreparedStatement statement = connection.prepareStatement(updateQuery);
        statement.setString(1, google_auth);
        System.out.println("Auth: " + google_auth + "For: " + currentUser.getEmail());
        statement.setString(2, currentUser.getEmail());
        statement.executeUpdate();
        Session.getCurrentUser().setGoogleAuthenticatorSecret(google_auth);
    }
    public List<User> getAllStudents() throws SQLException {
        List<User> students = new ArrayList<>();
        String query = "SELECT * FROM user WHERE role = 'student'";

        try (
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                User usr = new User();
                usr.setNom(rs.getString("nom"));
                usr.setPrenom(rs.getString("prenom"));
                usr.setEmail(rs.getString("email"));
                students.add(usr);
            }
        }
        return students;
    }

}
