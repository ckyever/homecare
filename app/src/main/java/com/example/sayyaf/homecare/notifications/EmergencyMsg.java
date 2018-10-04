package com.example.sayyaf.homecare.notifications;

import java.util.Date;

public class EmergencyMsg {

    private String emergencyMsgSenderId;
    private String emergencyMsgSender;
    private long emergencyMsgTime;

    // private String imageSouce;

    public EmergencyMsg() {}

    public EmergencyMsg(String emergencyMsgSenderId, String emergencyMsgSender) {
        this.emergencyMsgSenderId = emergencyMsgSenderId;
        this.emergencyMsgSender = emergencyMsgSender;
        emergencyMsgTime = new Date().getTime();
    }

    public String getEmergencyMsgSenderId() {
        return emergencyMsgSenderId;
    }

    public void setEmergencyMsgSenderId(String emergencyMsgSenderId) {
        this.emergencyMsgSenderId = emergencyMsgSenderId;
    }

    public String getEmergencyMsgSender() {
        return emergencyMsgSender;
    }

    public void setEmergencyMsgSender(String messageSender) { this.emergencyMsgSender = emergencyMsgSender; }

    public long getEmergencyMsgTime() { return emergencyMsgTime; }

}
