package tn.learniverse.tools;

import tn.learniverse.entities.*;

public class Session {
    private static user currentUser;
    private static Logs currentLog;
    private static String email;
    private static String url;

    public static void setCurrentUser(user user) {
        currentUser = user;
    }

    public static user getCurrentUser() {
        return currentUser;
    }
    public static void setCurrentLog(Logs log) {
        currentLog = log;
    }

    public static Logs getCurrentLog() {
        return currentLog;
    }
    public static void setEmail(String mail) {
        email = mail;
    }

    public static String getEmail() {
        return email;
    }

    public static String getUrl() {
        return url;
    }
    public static void setUrl(String URL) {
        url = URL;
    }
    public static void clear() {
        currentUser = null;
        currentLog = null;
        email = null;
    }

}
