package tn.learniverse.services;

import java.sql.SQLException;
import java.util.List;

public interface IUser<T> {
    void CreateAccount(T u) throws SQLException;
    void ModifyAccount(T u) throws SQLException;
    void DeleteAccount(T u);
    List<T> getAllUsers() throws SQLException;
    T getUserByEmail(String email);
    void banUser(int userId) throws SQLException ;
    void activateUser(int userId) throws SQLException ;
    int getUserIdByEmail(String email);
    boolean ChangePwd(String email, String pwd)  throws SQLException;
    public void MnsLogs(String email,int logs)  throws SQLException;
}
