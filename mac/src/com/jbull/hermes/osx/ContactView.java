package com.jbull.hermes.osx;


import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import com.jbull.hermes.desktop.Contact;
import com.jbull.hermes.desktop.Conversation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

public class ContactView extends HBox {
    Contact contact;
    Conversation conversation;
    ConversationView conversationView;
    Message.Contact contactMsg;

    int messagesRead = 0;
    boolean selected = false;

    @FXML ImageView image;
    @FXML Label numberLabel;
    @FXML Label nameLabel;

    public ContactView(final Contact contact, Conversation convo, State state) {
        this.contact = contact;
        this.conversation = convo;
        messagesRead = convo.getMessages().size();
        conversationView = new ConversationView(convo, this, state);
        URL resource = getClass().getResource("ContactView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        numberLabel.setText(contact.getPhoneNumber());
        nameLabel.setText(contact.getDisplayName());
        if (contact.getImageData().length > 0) {
            final Image img = new Image(new ByteArrayInputStream(contact.getImageData()));
            image.setImage(img);
        } else {
            final Image img = new Image(ContactView.class.getResource("resources/dog.jpeg").toString(), true);
            image.setImage(img);
        }

    }

    public String toString() {
        return contact.getDisplayName();
    }

    public ConversationView getConversationView() {
        return conversationView;
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

    public static class ContactListCell extends ListCell<ContactView> {
        @Override
        protected void updateItem(ContactView item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(item);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void select() {
        selected = true;
        messagesRead = conversation.getMessages().size();
        update();
    }

    public void deselect() {
        selected = false;
    }

    public int numUnread() {
        return conversation.getMessages().size() - messagesRead;
    }

    public void update() {
        if (selected) {
            messagesRead = conversation.getMessages().size();
        }
        setUnreadNum();
    }

    public void setUnreadNum() {
        int unread = numUnread();
        System.out.println("Setting unread num for : " + contact.getPhoneNumber() + " to "+Integer.toString(unread));
    }


}
