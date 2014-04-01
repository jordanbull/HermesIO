package com.jbull.hermes.osx;


public class Contact {
    static int numContacts = 0;
    String strRep;
    String phoneNumber;
    int id;
    ConversationThread conversation = new ConversationThread();

    public Contact(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        strRep = name;
        id = ++numContacts;
    }

    public String toString() {
        return strRep;
    }

    public ConversationThread getConversation() {
        return conversation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
