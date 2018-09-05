package com.example.sayyaf.homecare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {

    private String id;
    private String name;
    private String email;
    //private long phone;
    private boolean isCaregiver;
    //private List<User> contacts;
    private HashMap<String, String> contacts;
    public User(String name, String email, boolean isCaregiver) {
        this.name = name;
        this.email = email;
        this.isCaregiver = isCaregiver;
        //this.contacts = new ArrayList<>();
        this.contacts = new HashMap<>();
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

    public boolean isCaregiver() {
        return isCaregiver;
    }

    public void setCaregiver(boolean caregiver) {
        this.isCaregiver = caregiver;
    }

    /*
    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    } */

    public HashMap<String, String> getContacts() {
        return contacts;
    }

    public void setContacts(HashMap<String, String> contacts) {
        this.contacts = contacts;
    }

    public void addToUserList(User user) {
        contacts.put(user.getName(), user.getEmail());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
