package com.example.sayyaf.homecare.communication;

import java.util.Date;

public class ChatMessage {
    private String messageText;
    private String messageSender;
    private long messageTime;

    //private String imageSouce;

    public ChatMessage() {}

    public ChatMessage(String messageText, String messageSender) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        //this.imageSouce = "no Image";
        messageTime = new Date().getTime();
    }

    public ChatMessage(String messageText, String messageSender, String imageSouce) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        //this.imageSouce = imageSouce;
        messageTime = new Date().getTime();
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    //public String getImageSouce() { return imageSouce; }

    //public void setImageSouce(String imageSouce) { this.imageSouce = imageSouce; }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public long getMessageTime() { return messageTime; }

}
