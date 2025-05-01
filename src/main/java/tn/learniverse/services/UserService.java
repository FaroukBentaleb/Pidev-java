package tn.learniverse.services;

import java.sql.Connection;

public class UserService {
    private static Connection connection;

    public UserService (Connection connection) {
        this.connection = connection;
    }

}
