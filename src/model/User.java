package model;

public class User {
    private int id;
    private String login;
    private String role;
    private int isBlocked;

    public User(int id, String login, String role, int isBlocked) {
        this.id = id;
        this.login = login;
        this.role = role;
        this.isBlocked = isBlocked;
    }

    public User(int id, String login, String role) {
        this.id = id;
        this.login = login;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }
    public int isBlocked() {
        return isBlocked;
    }
}
