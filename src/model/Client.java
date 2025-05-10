package model;

import java.io.Serializable;

public class Client implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String full_name;
    private String phone;
    private String email;
    private String address;
    public Client(int id, String full_name, String phone, String email, String address) {
        this.id = id;
        this.full_name = full_name;
        this.phone = phone;
        this.email = email;
        this.address = address;

    }
    public int getId() {
        return id;
    }
    public Client( String full_name, String phone, String email, String address) {
        this.full_name = full_name;
        this.phone = phone;
        this.email = email;
        this.address = address;

    }

    public String getFull_name() {
        return full_name;
    }
    public String getPhone() {
        return phone;
    }
    public String getEmail() {
        return email;
    }
    public String getAddress() {
        return address;
    }

    public void setId(int id) {
    }
}
