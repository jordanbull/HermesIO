package com.jbull.hermes.osx;


import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import com.jbull.hermes.desktop.Contact;
import com.jbull.hermes.desktop.Conversation;

public class ContactView {
    Contact contact;
    ConversationView conversation;
    Message.Contact contactMsg;

    public ContactView(Message.Contact contact, CommunicationCenter commCenter) {
        this(new Contact(contact.getPhoneNumber(), contact.getName(), contact.getImage().toByteArray()), commCenter);
        contactMsg = contact;
    }

    public ContactView(Contact contact, CommunicationCenter commCenter) {
        this(contact, null, commCenter);
    }

    public ContactView(Contact contact, Conversation convo, CommunicationCenter commCenter) {
        this.contact = contact;
        conversation = new ConversationView(convo, this, commCenter);
    }

    public String toString() {
        return contact.getDisplayName();
    }

    public ConversationView getConversation() {
        return conversation;
    }

    public String getPhoneNumber() {
        return contact.getPhoneNumber();
    }

    public Message.Contact getContactMsg() {
        if (contactMsg == null) {
            contactMsg = MessageHelper.createContact(contact.getDisplayName(), contact.getPhoneNumber(), null); //does not include image
        }
        return contactMsg;
    }

}
