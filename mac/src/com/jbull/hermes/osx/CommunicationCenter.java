package com.jbull.hermes.osx;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;
import com.jbull.hermes.desktop.ListenFavoredCommunicationScheduler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;


public class CommunicationCenter extends BorderPane {
    private final ObservableList<Contact> contacts;
    private final CommunicationScheduler<GeneratedMessage> commScheduler;

    @FXML ListView<Contact> contactsList;
    @FXML AnchorPane messagingPane;

    public CommunicationCenter() throws IOException {
        URL resource = getClass().getResource("CommunicationCenter.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        contacts = contactsList.getItems();
        //addContactIfNew("my number", "Jordan Bull");

        contactsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Contact>() {
            @Override
            public void changed(ObservableValue<? extends Contact> observable, Contact oldValue, Contact newValue) {
                ConversationThread convo = newValue.getConversation();
                AnchorPane.setBottomAnchor(convo, 0.0);
                AnchorPane.setTopAnchor(convo, 0.0);
                AnchorPane.setLeftAnchor(convo, 0.0);
                AnchorPane.setRightAnchor(convo, 0.0);
                messagingPane.getChildren().clear();
                messagingPane.getChildren().add(convo);
                System.out.println("Selected item: " + newValue);
            }
        });

        commScheduler = initCommunication();
    }

    public CommunicationScheduler<GeneratedMessage> initCommunication() throws IOException {
        int numRetries = 0;
        TCPServer server = new TCPServer(8888);
        InstructionHandler handler = new InstructionHandler(this);
        MessageListener listener = new MessageListener(server, handler, numRetries);
        MessageSender sender = new MessageSender(server, numRetries);
        final ListenFavoredCommunicationScheduler commScheduler = new ListenFavoredCommunicationScheduler(sender, listener);
        Thread commThread = new Thread(new Runnable() {
            @Override
            public void run() {
                commScheduler.start();
            }
        });
        commThread.start();
        return commScheduler;
    }

    public Contact addContactIfNew(Message.Contact contactMsg) {
        int cid = findContact(contactMsg.getPhoneNumber());
        if (cid == -1) {
            Contact contact = new Contact(contactMsg, this);
            contacts.add(contact);
            return contact;
        }
        return contacts.get(cid);
    }

    public Contact addContactIfNew(String phoneNumber, String name) {
        int cid = findContact(phoneNumber);
        if (cid == -1) {
            Contact contact = new Contact(phoneNumber, name, this);
            contacts.add(contact);
            return contact;
        }
        return contacts.get(cid);
    }

    public int findContact(String number) {
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            if (contact.getPhoneNumber().equals(number)) {
                return i;
            }
        }
        return -1;
    }

    public void send(final GeneratedMessage msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                commScheduler.send(msg);
            }
        }).start();
    }

}
