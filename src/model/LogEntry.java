package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LogEntry implements Serializable {
    private int id;
    private LocalDateTime timestamp;
    private String userLogin;
    private String action;
    private String level;

    public LogEntry() {};
    public LogEntry(int id, LocalDateTime timestamp, String userLogin, String action, String level) {
        this.id = id;
        this.timestamp = timestamp;
        this.userLogin = userLogin;
        this.action = action;
        this.level = level;
    }

    public LogEntry(String userLogin, String description, String info) {
        this.userLogin = userLogin;
        this.action = description;
        this.level = info;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserLogin() {
        return userLogin;
    }
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }


}
