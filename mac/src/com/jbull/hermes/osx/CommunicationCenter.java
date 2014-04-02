package com.jbull.hermes.osx;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;
import com.jbull.hermes.desktop.Contact;
import com.jbull.hermes.desktop.Conversation;
import com.jbull.hermes.desktop.DataStore;
import com.jbull.hermes.desktop.ListenFavoredCommunicationScheduler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.*;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CommunicationCenter extends BorderPane {
    private final ObservableList<ContactView> contacts;
    private final CommunicationScheduler<GeneratedMessage> commScheduler;
    private final String DATA_STORE_FILENAME = "data.ser";
    private DataStore dataStore;
    public boolean stateChanged = false;

    private final long SECONDS_BETWEEN_SAVES = 5;

    @FXML ListView<ContactView> contactsList;
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

        contactsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ContactView>() {
            @Override
            public void changed(ObservableValue<? extends ContactView> observable, ContactView oldValue, ContactView newValue) {
                ConversationView convo = newValue.getConversation();
                AnchorPane.setBottomAnchor(convo, 0.0);
                AnchorPane.setTopAnchor(convo, 0.0);
                AnchorPane.setLeftAnchor(convo, 0.0);
                AnchorPane.setRightAnchor(convo, 0.0);
                messagingPane.getChildren().clear();
                messagingPane.getChildren().add(convo);
                System.out.println("Selected item: " + newValue);
            }
        });

        File file = new File(DATA_STORE_FILENAME);
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(DATA_STORE_FILENAME);
                ObjectInputStream ois = new ObjectInputStream(fis);
                dataStore = (DataStore) ois.readObject();
                ois.close();
                populateFromDataStore();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        boolean createdDataStore = false;
        if (dataStore == null) {
            dataStore = new DataStore();
            createdDataStore = true;
        }
        commScheduler = initCommunication();
        if (createdDataStore) {
            requestContacts();
        }

        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        stpe.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (stateChanged) {
                    stateChanged = false;
                    writeDataStore();
                    System.out.println("Saving State");
                }
            }
        }, SECONDS_BETWEEN_SAVES, SECONDS_BETWEEN_SAVES, TimeUnit.SECONDS);

    }

    public void close() {
        writeDataStore();
    }

    public void requestContacts() {
        commScheduler.send(MessageHelper.createSyncContacts());
    }

    private void populateFromDataStore() {
        for (Contact contact : dataStore.getAllContacts()) {
            Conversation convo = dataStore.getConversation(contact.getPhoneNumber());
            ContactView contactView = new ContactView(contact, convo, this);
            contacts.add(contactView);
        }
    }

    synchronized public void writeDataStore() {
        try {
            FileOutputStream fos = new FileOutputStream(DATA_STORE_FILENAME+"1");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataStore);
            oos.close();
            new File(DATA_STORE_FILENAME).delete();
            new File(DATA_STORE_FILENAME+"1").renameTo(new File(DATA_STORE_FILENAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public ContactView addContactIfNew(Message.Contact contactMsg) {
        int cid = findContact(contactMsg.getPhoneNumber());
        if (cid == -1) {
            ContactView contact = new ContactView(contactMsg, this);
            dataStore.addContact(contactMsg.getPhoneNumber(), contactMsg.getName(), contactMsg.getImage().toByteArray(), false);
            contacts.add(contact);
            stateChanged = true;
            return contact;
        }
        return contacts.get(cid);
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public int findContact(String number) {
        for (int i = 0; i < contacts.size(); i++) {
            ContactView contact = contacts.get(i);
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