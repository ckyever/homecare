package com.example.sayyaf.homecare.requests;

public class Request {

    private String senderId;
    private String email;
    public Request(String senderId, String email) {
        this.senderId = senderId;
        this.email = email;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
