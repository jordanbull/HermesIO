package com.jbull.hermes.osx;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;
import com.jbull.hermes.desktop.*;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class State {

    private final String DATA_STORE_FILENAME = "data.ser";
    private final long SECONDS_BETWEEN_SAVES = 5;

    private DataStore dataStore;
    private RadixTrie trie = new RadixTrie();
    private CommunicationScheduler<GeneratedMessage> commScheduler;
    private HashMap<String, ContactView> numberToContactView = new HashMap<String, ContactView>();

    public boolean stateChanged = false;
    private CommunicationCenter commCenter;


    public State(CommunicationCenter commCenter) {
        this.commCenter = commCenter;
        commCenter.setState(this);
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean createdDataStore = false;
        if (dataStore == null) {
            dataStore = new DataStore();
            createdDataStore = true;
        }
        try {
            commScheduler = initCommunication();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void populateFromDataStore() {
        CommunicationCenter.TimeSortedContacts defaultContacts = commCenter.getDefaultContacts();
        for (Contact contact : dataStore.getAllContacts()) {
            Conversation convo = dataStore.getConversation(contact.getPhoneNumber());
            addContactToGui(contact, convo);
            ContactView contactView = numberToContactView.get(contact.getPhoneNumber());
            contactView.getConversationView().update();
            if (convo.getMessages().size() > 0) {
                defaultContacts.add(contactView);
            }
        }
    }

    public void addContact(Message.Contact cMsg) {
        String number = PhoneNumber.format(cMsg.getPhoneNumber());
        Contact contact = dataStore.addContact(number, cMsg.getName(), cMsg.getImage().toByteArray(), false);
        if (contact != null) {
            addContactToGui(contact, dataStore.getConversation(number));
            stateChanged = true;
        }
    }

    private void addContactToGui(Contact contact, Conversation conversation) {
        if (!numberToContactView.containsKey(contact.getPhoneNumber())) {
            numberToContactView.put(contact.getPhoneNumber(), new ContactView(contact, conversation, this));
            trie.insertContact(contact);
        }
    }

    public void addSms(Message.SmsMessage smsMsg, boolean senderOfMsg) {
        Message.Contact c = null;
        if (senderOfMsg) {
            c = smsMsg.getRecipents(0);
        } else {
            c = smsMsg.getSender();
        }
        addContact(c);
        String number = PhoneNumber.format(c.getPhoneNumber());
        Sms sms = dataStore.addMessageToConversation(number, smsMsg.getContent(), senderOfMsg, smsMsg.getTimeStamp());
        addSmsToGui(number, sms);
        stateChanged = true;
    }

    private void addSmsToGui(String number, Sms sms) {
        ContactView c = numberToContactView.get(number);
        c.update();
        c.getConversationView().update();
        commCenter.getDefaultContacts().add(c);
    }

    public void send(final GeneratedMessage msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                commScheduler.send(msg);
            }
        }).start();
    }

    public ObservableList<ContactView> searchContacts(String query) {
        Set<Contact> contactSet = trie.getContact(query);
        if (contactSet.size() > 0) {
            String s = "";
            for (Contact c : contactSet) {
                s += c.getDisplayName() + ", ";
            }
            System.out.println(s);
        }
        ObservableList<ContactView> contacts = new ListView<ContactView>().getItems();
        for (Contact c : contactSet) {
            contacts.add(numberToContactView.get(c.getPhoneNumber()));
        }
        return contacts;
    }

    private void requestContacts() {
        commScheduler.send(MessageHelper.createSyncContacts());
    }

    synchronized private void writeDataStore() {
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

    private CommunicationScheduler<GeneratedMessage> initCommunication() throws IOException {
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

    public void close() {
        //TODO
        commScheduler.stop();
    }
}
