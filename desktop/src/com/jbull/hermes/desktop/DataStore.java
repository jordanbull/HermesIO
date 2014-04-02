package com.jbull.hermes.desktop;


import com.jbull.hermes.Message;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public Contact addContact(Message.Contact contact, boolean overwrite) {
        return addContact(contact.getPhoneNumber(), contact.getName(), contact.getImage().toByteArray(), overwrite);
    }

    public Contact addContact(String phoneNumber, String displayName, byte[] imageData, boolean overwrite) {
        Contact contact = new Contact(phoneNumber, displayName, imageData);
        if (!overwrite && contacts.containsKey(phoneNumber)) {
            return null;
        }
        contacts.put(phoneNumber, contact);
        return contact;
    }

    public Conversation getConversation(String phoneNumber) {
        return conversations.get(phoneNumber);
    }

    public Sms addMessageToConversation(String phoneNumber, String msgContent, boolean senderOfMessage, long timeMillis) {
        Conversation conversation;
        if (!conversations.containsKey(phoneNumber)) {
            conversation = new Conversation(phoneNumber);
            conversations.put(phoneNumber, conversation);
        } else {
            conversation = conversations.get(phoneNumber);
        }
        Sms sms = new Sms(msgContent, timeMillis, senderOfMessage);
        conversation.addMessage(sms);
        return sms;
    }

    public boolean equals(Object obj) {
        DataStore d2 = (DataStore) obj;
        return contacts.equals(d2.contacts) && conversations.equals(d2.conversations);
    }









}
