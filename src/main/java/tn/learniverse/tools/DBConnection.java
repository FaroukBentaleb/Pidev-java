package tn.learniverse.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    public final String URL="jdbc:mysql://localhost:3306/learniverse";
    public final String USER="root";
    public final String PWD="";
    private Connection connection;
    public static DBConnection DB;

    private DBConnection() {
        try {
            // Charger explicitement le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Tentative de connexion
            connection = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connexion à la base de données établie avec succès!");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Driver MySQL non trouvé!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données!");
            System.err.println("URL: " + URL);
            System.err.println("Utilisateur: " + USER);
            System.err.println("Message d'erreur: " + e.getMessage());
            System.err.println("Code d'erreur SQL: " + e.getSQLState());
            e.printStackTrace();
        }
    }

    public static DBConnection getInstance() {
        if (DB == null) {
            DB = new DBConnection();
        }
        // Vérifier si la connexion est toujours valide
        if (DB != null && DB.getConnection() != null) {
            try {
                if (!DB.getConnection().isValid(5)) { // timeout de 5 secondes
                    System.out.println("La connexion n'est plus valide, tentative de reconnexion...");
                    DB = new DBConnection();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la vérification de la connexion!");
                e.printStackTrace();
            }
        }
        return DB;
    }

    public Connection getConnection() {
        return connection;
    }
}
