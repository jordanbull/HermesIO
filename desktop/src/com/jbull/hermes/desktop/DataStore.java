package com.jbull.hermes.desktop;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private AtomicLong smsCounter = new AtomicLong(0);

    HashMap<String, Contact> contacts = new HashMap<String, Contact>();
    HashMap<String, Conversation> conversations = new HashMap<String, Conversation>();

    public Contact getContact(String phoneNumber) {
        return contacts.get(phoneNumber);
    }

    public Collection<Contact> getAllContacts() {
        return contacts.values();
    }

    public Collection<Conversation> getAllConversations() {
        return conversations.values();
    }

    public Contact addContact(String phoneNumber, String displayName, byte[] imageData, boolean overwrite) {
        Contact contact = new Contact(phoneNumber, displayName, imageData);
        if (!overwrite && contacts.containsKey(phoneNumber)) {
            return null;
        }
        contacts.put(phoneNumber, contact);
        Conversation conversation = new Conversation(phoneNumber);
        conversations.put(phoneNumber, conversation);
        return contact;
    }

    public Conversation getConversation(String phoneNumber) {
        return conversations.get(phoneNumber);
    }

    public Sms addMessageToConversation(String phoneNumber, String msgContent, boolean senderOfMessage, long timeMillis) {
        Conversation conversation = conversations.get(phoneNumber);
        Sms sms = new Sms(msgContent, timeMillis, senderOfMessage, getNewMsgNum());
        conversation.addMessage(sms);
        return sms;
    }

    public boolean equals(Object obj) {
        DataStore d2 = (DataStore) obj;
        return contacts.equals(d2.contacts) && conversations.equals(d2.conversations);
    }

    private long getNewMsgNum() {
        return smsCounter.getAndIncrement();
    }
}
