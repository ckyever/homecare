package com.example.sayyaf.homecare;

public class User {

    private String name;
    private String email;
    //private long phone;
    private boolean isCaregiver;

    public User(String name, String email, boolean isCaregiver) {
        this.name = name;
        this.email = email;
        this.isCaregiver = isCaregiver;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*
    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    } */

}
