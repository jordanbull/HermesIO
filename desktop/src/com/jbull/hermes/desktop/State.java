package com.jbull.hermes.desktop;

import com.jbull.hermes.Logger;
import com.jbull.hermes.MessageListener;
import com.jbull.hermes.MessageSender;
import com.jbull.hermes.TCPServer;
import com.jbull.hermes.messages.ContactMessage;
import com.jbull.hermes.messages.HermesMessage;
import com.jbull.hermes.messages.SmsMessage;
import com.jbull.hermes.messages.SyncContactsMessage;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class State {
    private static final int MESSAGE_VERSION = 2;
    private static final int MIN_MESSAGE_VERSION = 2;

    private final String DATA_STORE_FILENAME = "data.ser";
    private final String SYNCING_CONTACTS_TEXT = "Syncing Contacts...";

    private String pathToDataStore;
    private final long SECONDS_BETWEEN_SAVES = 5;

    private boolean connected = false;

    private int timeoutMillis = 0;
    private int timeoutConstant = 5000;
    private int numRetries = 1;
    private long lastHeartBeat = -1;
    private int rcvWindow = 0;

    private DataStore dataStore;
    private RadixTrie trie = new RadixTrie();
    private ListenFavoredCommunicationScheduler commScheduler;
    private HashMap<String, ContactView> numberToContactView = new HashMap<String, ContactView>();

    public boolean stateChanged = false;
    private CommunicationCenter commCenter;
    private TCPServer server;
    private boolean requestingContacts;


    public State(CommunicationCenter commCenter) {
        this.commCenter = commCenter;
        commCenter.setState(this);
        final File f = new File(State.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File file = new File(f.getParent()+"/"+DATA_STORE_FILENAME);
        pathToDataStore = file.getAbsolutePath();
        Logger.log(pathToDataStore);
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(pathToDataStore);
                ObjectInputStream ois = new ObjectInputStream(fis);
                dataStore = (DataStore) ois.readObject();
                ois.close();
                populateFromDataStore();
            } catch (ClassNotFoundException e) {
                Logger.log(e);
            } catch (FileNotFoundException e) {
                Logger.log(e);
            } catch (IOException e) {
                Logger.log(e);
            }
        }
        boolean createdDataStore = false;
        if (dataStore == null) {
            dataStore = new DataStore();
            createdDataStore = true;
        }
        initCommunication();
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
                    Logger.log("Saving State");
                }
            }
        }, SECONDS_BETWEEN_SAVES, SECONDS_BETWEEN_SAVES, TimeUnit.SECONDS);
    }

    private void populateFromDataStore() {
        final CommunicationCenter.TimeSortedContacts defaultContacts = commCenter.getDefaultContacts();
        final List<ContactView> convs = new ArrayList<ContactView>();
        for (Contact contact : dataStore.getAllContacts()) {
            Conversation convo = dataStore.getConversation(contact.getPhoneNumber());
            addContactToGui(contact, convo);
            ContactView contactView = numberToContactView.get(contact.getPhoneNumber());
            if (convo.getMessages().size() > 0) {
                convs.add(contactView);
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                defaultContacts.addAll(convs);
            }
        });
    }

    public void addContact(ContactMessage cMsg) {
        if (requestingContacts) {
            requestingContacts = false;
            commCenter.setExtraInfo("");
        }
        String number = PhoneNumber.format(cMsg.getPhoneNumber());
        Contact contact = dataStore.addContact(number, cMsg.getDisplayName(), cMsg.getImageData(), false);
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

    public void addSms(SmsMessage smsMsg, boolean senderOfMsg) {
        ContactMessage c = null;
        if (senderOfMsg) {
            c = smsMsg.getRecipient();
            commCenter.resetSearch();
        } else {
            c = smsMsg.getSender();
            commCenter.searchTyped();
        }
        addContact(c);
        String number = PhoneNumber.format(c.getPhoneNumber());
        Sms sms = dataStore.addMessageToConversation(number, smsMsg.getContent(), senderOfMsg, smsMsg.getTimeMillis());
        addSmsToGui(number, sms);
        stateChanged = true;
    }

    private void addSmsToGui(String number, Sms sms) {
        final ContactView c = numberToContactView.get(number);
        c.update();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                commCenter.getDefaultContacts().add(c);
            }
        });
    }

    public void send(final HermesMessage msg) {
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
            Logger.log(s);
        }
        ObservableList<ContactView> contacts = new ListView<ContactView>().getItems();
        for (Contact c : contactSet) {
            contacts.add(numberToContactView.get(c.getPhoneNumber()));
        }
        return contacts;
    }

    private void requestContacts() {
        requestingContacts = true;
        while (commScheduler == null) {
            try {
                Thread.sleep(100); //avoid nullpointer
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        send(new SyncContactsMessage());
    }

    synchronized private void writeDataStore() {
        try {
            FileOutputStream fos = new FileOutputStream(pathToDataStore+"1");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataStore);
            oos.close();
            new File(pathToDataStore).delete();
            new File(pathToDataStore+"1").renameTo(new File(pathToDataStore));
        } catch (FileNotFoundException e) {
            Logger.log(e);
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    private void initCommunication() {
        Logger.log("Initializing Communication");
        Thread commThread = new Thread(new Runnable() {
            @Override
            public void run() {
                InstructionHandler handler = new InstructionHandler(State.this);
                try {
                    server = new TCPServer(8888, 0);
                    MessageListener listener = new MessageListener(server, handler, numRetries);
                    MessageSender sender = new MessageSender(server, numRetries);
                    commScheduler = new ListenFavoredCommunicationScheduler(sender, listener, new Runnable() {
                        @Override
                        public void run() {
                            disconnect();
                        }
                    });
                    commScheduler.start();
                } catch (IOException e) {
                    Logger.log(e);
                    disconnect();
                }
            }
        });
        commThread.start();
    }

    public void disconnect() {
        if (connected) {
            connected = false;
            if (requestingContacts) {
                requestingContacts = false;
                commCenter.setExtraInfo("");
            }
            Logger.log("Disconnected at " + Long.toString(System.currentTimeMillis()));
            commCenter.setConnectionStatusLabel(false);
            commScheduler.stop();
            try {
                server.close();
                timeoutMillis = 0;
                initCommunication();
            } catch (IOException e) {
                Logger.log(e);
            }
        }
    }

    public void connected(int sendPeriod, int messageVersionNumber) {
        if (messageVersionNumber < MIN_MESSAGE_VERSION) {
            commCenter.systemNotify("Android Update Required", "The Android app is using a messaging version that is lower than the minimum version required for this desktop app.", null);
        } else if (messageVersionNumber < MESSAGE_VERSION) {
            commCenter.systemNotify("Android Update Advisable", "The Android app is using a messaging version that is lower than the preferred version. Some features may not be supported.", null);
        } else if (messageVersionNumber > MESSAGE_VERSION) {
            commCenter.systemNotify("Desktop Update Advisable", "The Desktop app is using a messaging version that is lower than the Android version. It may be incompatible.", null);
        }
        if (requestingContacts) {
            commCenter.setExtraInfo(SYNCING_CONTACTS_TEXT);
        }
        connected = true;
        commCenter.setConnectionStatusLabel(true);
        lastHeartBeat = System.currentTimeMillis();
        rcvWindow = 2*sendPeriod+timeoutConstant;
        try {
            server.setTimeout(rcvWindow);
        } catch (SocketException e) {
            Logger.log(e);
        }
    }

    public void updateTimeout() {
        long time = System.currentTimeMillis();
        if (lastHeartBeat == -1) {
            rcvWindow = 0;
        } else {
            int twind = (int) (2l * (time - lastHeartBeat));
            if (twind > rcvWindow) {
                rcvWindow = twind;
            }
        }
        lastHeartBeat = time;
        try {
            server.setTimeout(rcvWindow);
        } catch (SocketException e) {
            Logger.log(e);
        }
        Logger.log(Integer.toString(rcvWindow));
    }

    public void notify(String subject, String body, byte[] imageData) {
        commCenter.messageNotify(subject, body, imageData);
    }

    public void close() throws IOException {
        //TODO
        disconnect();
    }
}
