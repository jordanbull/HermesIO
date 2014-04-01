package com.jbull.hermes.osx;


import com.google.protobuf.ByteString;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;

public class Contact {
    static int numContacts = 0;
    String strRep;
    String phoneNumber;
    ByteString imageData;
    int id;
    ConversationThread conversation;
    Message.Contact contactMsg;

    public Contact(Message.Contact contact, CommunicationCenter commCenter) {
        this(contact.getPhoneNumber(), contact.getName(), commCenter);
        contactMsg = contact;
        imageData = contact.getImage();
    }

    public Contact(String phoneNumber, String name, CommunicationCenter commCenter) {
        this.phoneNumber = phoneNumber;
        strRep = name;
        id = ++numContacts;
        conversation = new ConversationThread(this, commCenter);
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

    public Message.Contact getContactMsg() {
        if (contactMsg == null) {
            contactMsg = MessageHelper.createContact(strRep, phoneNumber, imageData);
        }
        return contactMsg;
    }

}
