package tn.learniverse.services;

import tn.learniverse.entities.*;

import java.sql.SQLException;
import java.util.List;

public interface IUser<T> {
    void CreateAccount(T u) throws SQLException;
    void ModifyAccount(T u) throws SQLException;
    void DeleteAccount(T u);
    List<T> getAllUsers() throws SQLException;
    T getUserByEmail(String email);
}
