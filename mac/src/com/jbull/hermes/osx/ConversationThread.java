package com.jbull.hermes.osx;

import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class ConversationThread extends BorderPane {
    @FXML TextArea textInput;
    @FXML ListView messageList;
    @FXML Button sendButton;
    Message.Contact me = MessageHelper.createContact("Me", "My Number", null);

    Contact contact;
    private CommunicationCenter commCenter;

    public ConversationThread(Contact contact, CommunicationCenter commCenter) {
        this.commCenter = commCenter;
        URL resource = getClass().getResource("ConversationThread.fxml");
         FXMLLoader fxmlLoader = new FXMLLoader(resource);
         fxmlLoader.setRoot(this);
         fxmlLoader.setController(this);
         try {
             fxmlLoader.load();
         } catch (IOException exception) {
             throw new RuntimeException(exception);
         }
         this.contact = contact;
     }

    public void send() {
        ArrayList<Message.Contact> recipents = new ArrayList<Message.Contact>();
        recipents.add(contact.getContactMsg());
        String textContent = textInput.getText();
        Message.SmsMessage msg = MessageHelper.createSmsMessage(me, textContent, System.currentTimeMillis(), recipents, true);
        commCenter.send(msg);
        textInput.clear();
    }
}
