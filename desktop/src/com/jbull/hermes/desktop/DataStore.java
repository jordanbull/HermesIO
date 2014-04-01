package com.jbull.hermes.desktop;


import com.jbull.hermes.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DataStore implements Serializable {
    HashMap<String, ContactData> contacts = new HashMap<String, ContactData>();
    HashMap<String, ConversationData> conversations = new HashMap<String, ConversationData>();

    public ContactData getContact(String phoneNumber) {
        return contacts.get(phoneNumber);
    }

    public Collection<ContactData> getAllContacts() {
        return contacts.values();
    }

    public Collection<ConversationData> getAllConversations() {
        return conversations.values();
    }

    public void addContact(Message.Contact contact, boolean overwrite) {
        addContact(contact.getPhoneNumber(), contact.getName(), contact.getImage().toByteArray(), overwrite);
    }

    public void addContact(String phoneNumber, String displayName, byte[] imageData, boolean overwrite) {
        if (!overwrite && contacts.containsKey(phoneNumber)) {
            return;
        }
        contacts.put(phoneNumber, new ContactData(phoneNumber, displayName, imageData));
    }

    public ConversationData getConversation(String phoneNumber) {
        return conversations.get(phoneNumber);
    }

    public void addMessageToConversation(String phoneNumber, String msgContent, long timeMillis) {
        ConversationData conversation;
        if (!conversations.containsKey(phoneNumber)) {
            conversation = new ConversationData(phoneNumber);
            conversations.put(phoneNumber, conversation);
        } else {
            conversation = conversations.get(phoneNumber);
        }
        conversation.addMessage(msgContent, timeMillis);
    }

    public boolean equals(Object obj) {
        DataStore d2 = (DataStore) obj;
        if (!contacts.equals(d2.contacts)) {
            return false;
        }
        if (!conversations.equals(d2.conversations)) {
            return false;
        }
        return true;
        //return contacts.equals(d2.contacts) && conversations.equals(d2.conversations);
    }



    protected static class ContactData implements Serializable {
        private String displayName;
        private byte[] imageData;
        private String phoneNumber;

        protected ContactData(String phoneNumber, String displayName, byte[] imageData) {
            this.phoneNumber = phoneNumber;
            this.displayName = displayName;
            this.imageData = imageData;
        }

        public String getDisplayName() {
            return displayName;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public boolean equals(Object obj) {
            ContactData contact2 = (ContactData) obj;
            return displayName.equals(contact2.displayName) && phoneNumber.equals(contact2.phoneNumber) && java.util.Arrays.equals(imageData, contact2.imageData);
        }
    }

    protected static class ConversationData implements Serializable {
        private String phoneNumber;
        private ArrayList<MessageData> messages = new ArrayList<MessageData>();

        protected ConversationData(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public void addMessage(String content, long timeMillis) {
            int i = messages.size()-1;
            for (; i >= 0; i--) {
                if(messages.get(i).getTimeMillis() < timeMillis) {
                    break;
                }
            }
            MessageData msg = new MessageData(content, timeMillis);
            messages.add(i+1, msg);
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public ArrayList<MessageData> getMessages() {
            return (ArrayList<MessageData>) messages.clone();
        }

        public boolean equals(Object obj) {
            ConversationData conv2 = (ConversationData) obj;
            return phoneNumber.equals(conv2.getPhoneNumber()) && messages.equals(conv2.getMessages());
        }
    }

    protected static class MessageData implements Serializable {
        private String content;
        private Long timeMillis;

        protected MessageData(String content, Long timeMillis) {
            this.content = content;
            this.timeMillis = timeMillis;
        }

        public String getContent() {
            return content;
        }

        public Long getTimeMillis() {
            return timeMillis;
        }

        public boolean equals(Object obj) {
            MessageData msg2 = (MessageData) obj;
            if (msg2.getTimeMillis().equals(getTimeMillis()) && msg2.getContent().equals(getContent())) {
                return true;
            }
            return false;
        }
    }

}
