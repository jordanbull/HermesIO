package com.jbull.hermes.osx;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;


public class CommunicationCenter extends BorderPane {
    private final ObservableList<Contact> contacts;
    @FXML ListView<Contact> contactsList;

    public CommunicationCenter() {
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
    }

    public Contact addContactIfNew(String phoneNumber, String name) {
        int cid = findContact(phoneNumber);
        if (cid == -1) {
            Contact contact = new Contact(phoneNumber, name);
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

}
