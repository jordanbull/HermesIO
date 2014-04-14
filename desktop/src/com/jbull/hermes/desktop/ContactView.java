package com.jbull.hermes.desktop;


import com.jbull.hermes.Logger;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import javafx.application.Platform;
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
    private State state;
    ConversationView conversationView;
    Message.Contact contactMsg;

    int messagesRead = 0;
    boolean selected = false;

    @FXML ImageView image;
    @FXML Label numberLabel;
    @FXML Label nameLabel;
    @FXML Label unreadDisplay;

    public ContactView(final Contact contact, Conversation convo, State state) {
        this.contact = contact;
        this.conversation = convo;
        this.state = state;
        messagesRead = convo.getMessages().size();
        //conversationView = new ConversationView(convo, this, state);
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
            contactMsg = MessageHelper.createContact(contact.getDisplayName(), contact.getPhoneNumber(), null, null); //does not include image
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
        //conversationView = null;
    }

    public int numUnread() {
        return conversation.getMessages().size() - messagesRead;
    }

    public void update() {
        if (selected) {
            messagesRead = conversation.getMessages().size();
            if (conversationView == null) {
                conversationView = new ConversationView(conversation, this, state);
            } else {
                conversationView.update();
            }
        }
        setUnreadNum();
    }

    public void setUnreadNum() {
        final int unread = numUnread();
        Logger.log("Setting unread num for : " + contact.getPhoneNumber() + " to " + Integer.toString(unread));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                unreadDisplay.setText(String.valueOf(unread));
                if (unread == 0) {
                    unreadDisplay.setOpacity(0.0);
                } else {
                    unreadDisplay.setOpacity(1);
                }
                setStyle(getUnreadBackgroundStyle(unread));
            }
        });
    }

    public String getUnreadBackgroundStyle(int numUnread) {
        String opacity = "";
        if (numUnread == 0) {
            opacity = "0";
        } else if (numUnread < 10) {
            opacity = "0." + String.valueOf(50 + 5 * numUnread);
        } else {
            opacity = "1";
        }
        return "-fx-background-color: rgba(124,252,0,"+opacity+")";
    }

}
