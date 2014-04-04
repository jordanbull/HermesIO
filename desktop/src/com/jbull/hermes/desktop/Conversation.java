package com.jbull.hermes.desktop;

import java.io.Serializable;
import java.util.ArrayList;

public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String phoneNumber;
    private ArrayList<Sms> messages = new ArrayList<Sms>();
    private long mostRecentMsgNum;

    public Conversation(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void addMessage(String content, long timeMillis, boolean senderOfMessage) {
        addMessage(new Sms(content, timeMillis, senderOfMessage));
    }

    public void addMessage(Sms msg) {
        int i = messages.size()-1;
        for (; i >= 0; i--) {
            if(messages.get(i).getTimeMillis() < msg.getTimeMillis()) {
                break;
            }
        }
        messages.add(i+1, msg);
        mostRecentMsgNum = Math.max(mostRecentMsgNum, msg.getMsgNum());
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public ArrayList<Sms> getMessages() {
        return (ArrayList<Sms>) messages.clone();
    }

    public boolean equals(Object obj) {
        Conversation conv2 = (Conversation) obj;
        return phoneNumber.equals(conv2.getPhoneNumber()) && messages.equals(conv2.getMessages());
    }

    public long mostRecentMsgNum() {
        return mostRecentMsgNum;
    }
}