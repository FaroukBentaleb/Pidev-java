package tn.learniverse.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    public final String URL="jdbc:mysql://localhost:3306/learniverse";
    public final String USER="root";
    public final String PWD="";
    private static Connection connection;
    public static DBConnection DB;

    private  DBConnection(){
        try {
            connection = DriverManager.getConnection(URL,USER,PWD);
            System.out.println("Connection established!!");
        }
        catch (SQLException e){
            System.out.println(e);
        }
    }
    public static DBConnection getInstance(){
        if(DB == null){
            DB = new DBConnection();
        }
        return DB;
    }

    public static Connection getConnection() {
        return connection;
    }
}
