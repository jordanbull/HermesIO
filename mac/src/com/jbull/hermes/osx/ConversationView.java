package com.jbull.hermes.osx;

import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import com.jbull.hermes.desktop.Conversation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class ConversationView extends BorderPane {
    @FXML TextArea textInput;
    @FXML ListView messageList;
    @FXML Button sendButton;
    Message.Contact me = MessageHelper.createContact("Me", "My Number", null);

    ContactView contact;
    private Conversation conversation;
    private State state;

    public ConversationView(Conversation conversation, ContactView contact, State state) {
        this.state = state;
        URL resource = getClass().getResource("ConversationView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.contact = contact;
        if (conversation == null) {
            conversation = new Conversation(contact.getPhoneNumber());
        }
        this.conversation = conversation;
    }

    public void update() {
        System.out.println("updating GUI for: "+contact.getPhoneNumber());
        //TODO
    }

    @FXML
    public void send() {
        ArrayList<Message.Contact> recipents = new ArrayList<Message.Contact>();
        recipents.add(contact.getContactMsg());
        String textContent = textInput.getText();
        Message.SmsMessage msg = MessageHelper.createSmsMessage(me, textContent, System.currentTimeMillis(), recipents, true);
        state.send(msg);
        textInput.clear();
    }
}
