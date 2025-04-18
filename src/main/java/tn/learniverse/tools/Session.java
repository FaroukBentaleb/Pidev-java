package tn.learniverse.tools;
public class Session {
    private static Session instance;
    private static User currentUser;

    private Session() {
        // Private constructor for singleton
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            // Create a default test user
            currentUser = new User("Test User", "Instructor", 4);
        }
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Nested User class
    public static class User {
        private String name;
        private String role;
        private int id;

        public User(String name, String role, int id) {
            this.name = name;
            this.role = role;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
} 