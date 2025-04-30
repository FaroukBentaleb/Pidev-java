package tn.learniverse.tools;

import tn.learniverse.entities.*;

public class Session {
    private static User currentUser;
    private static Logs currentLog;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
    public static void setCurrentLog(Logs log) {
        currentLog = log;
    }

    public static Logs getCurrentLog() {
        return currentLog;
    }
    public static void clear() {
        currentUser = null;
        currentLog = null;
    }

}
