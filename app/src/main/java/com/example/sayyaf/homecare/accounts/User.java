package com.example.sayyaf.homecare.accounts;

import java.util.HashMap;

public class User {

    private String id;
    private String name;
    private String email;
    private boolean isCaregiver;
    private String profileImage;
    //private List<User> friends;
    private HashMap<String, String> friends;
    private HashMap<String, String> chatDatabase;
    private HashMap<String, String> requests;
    private HashMap<String, String> requestsSent;

    public User(String name, String email, boolean isCaregiver) {
        this.name = name;
        this.email = email;
        this.isCaregiver = isCaregiver;
        this.profileImage = "no image";
        //this.friends = new ArrayList<>();
        this.friends = new HashMap<>();
        this.chatDatabase = new HashMap<>();
        this.requests = new HashMap<>();
        this.requestsSent = new HashMap<>();
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

    public String getUserType(){
        if(isCaregiver) return "caregiver";
        return "assisted person";
    }

    public String getProfileImage(){ return profileImage; }

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

    public HashMap<String, String> getChatDatabase() { return chatDatabase; }

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

    public void setChatDatabase(HashMap<String, String> chatDatabase) {
        this.chatDatabase = chatDatabase;
    }

    public HashMap<String, String> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, String> requests) {
        this.requests = requests;
    }

    public HashMap<String, String> getRequestsSent() {
        return requestsSent;
    }

    public void setRequestsSent(HashMap<String, String> requestsSent) {
        this.requestsSent = requestsSent;
    }

    public void setProfileImage(String profileImage){ this.profileImage = profileImage; }
}
