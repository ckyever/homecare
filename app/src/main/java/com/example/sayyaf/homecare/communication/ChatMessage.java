package com.example.sayyaf.homecare.communication;

import java.util.Date;

public class ChatMessage {
    private String messageText;
    private String messageSender;
    private long messageTime;

    private String imageSource;

    public ChatMessage() {}

    public ChatMessage(String messageText, String messageSender) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        this.imageSource = "no Image";
        messageTime = new Date().getTime();
    }

    public ChatMessage(String messageText, String messageSender, String imageSource) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        this.imageSource = imageSource;
        messageTime = new Date().getTime();
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getImageSource() { return imageSource; }

    public void setImageSource(String imageSource) { this.imageSource = imageSource; }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public long getMessageTime() { return messageTime; }

}
