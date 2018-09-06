package com.example.sayyaf.homecare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {

    private String id;
    private String name;
    private String email;
    private boolean isCaregiver;
    //private List<User> friends;
    private HashMap<String, String> friends;

    public User(String name, String email, boolean isCaregiver) {
        this.name = name;
        this.email = email;
        this.isCaregiver = isCaregiver;
        //this.friends = new ArrayList<>();
        this.friends = new HashMap<>();
    }

    public User() {

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

    public HashMap<String, String> getFriends() {
        return friends;
    }

    public void setFriends(HashMap<String, String> friends) {
        this.friends = friends;
    }

    public void addToUserList(User user) {
        friends.put(user.getName(), user.getEmail());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
