package tn.learniverse.tools;

import tn.learniverse.entities.User;

public class Session {
    private static User currentUser;

    public static void setCurrentUser(User u) {
        currentUser = u;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }
}
