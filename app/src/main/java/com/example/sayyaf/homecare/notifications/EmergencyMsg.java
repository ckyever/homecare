package com.example.sayyaf.homecare.notifications;

import java.util.Date;

public class EmergencyMsg {

    private String messageSenderId;
    private String messageSender;
    private long messageTime;

    public EmergencyMsg() {}

    public EmergencyMsg(String messageSenderId, String messageSender) {
        this.messageSenderId = messageSenderId;
        this.messageSender = messageSender;
        messageTime = new Date().getTime();
    }

    public String getMessageSender() { return messageSender; }

    public void setMessageSender(String messageSender) { this.messageSender = messageSender; }

    public String getMessageSenderId() { return messageSenderId; }

    public void setMessageSenderId(String messageSenderId) { this.messageSenderId = messageSenderId; }

    public long getMessageTime() { return messageTime; }

}
