package com.jbull.hermes.osx;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageReactor;
import com.jbull.hermes.desktop.DataStore;
import javafx.application.Platform;

public class InstructionHandler implements MessageReactor {

    private final DataStore dataStore;
    private CommunicationCenter communicationCenter;

    public InstructionHandler(CommunicationCenter communicationCenter) {
        this.communicationCenter = communicationCenter;
        dataStore = communicationCenter.getDataStore();
    }

    @Override
    public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg) {
        if (type == Message.Header.Type.MODE) {
            System.out.println("received: Mode "+Long.toString(System.currentTimeMillis()));
            Message.Mode mode = (Message.Mode) msg;
            return !mode.getServerSend();
        } else if (type == Message.Header.Type.SMSMESSAGE) {
            executeSMS((Message.SmsMessage) msg);
        } else if (type == Message.Header.Type.SETUPMESSAGE) {
            System.out.println("received: SetupMessage");
        } else if (type == Message.Header.Type.CONTACT) {
            executeContact((Message.Contact) msg);
        }
        return true;
    }

    private void executeSMS(final Message.SmsMessage sms) {
        System.out.println("received: SMSMessage");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final ContactView contact = communicationCenter.addContactIfNew(sms.getSender());
                new Notification(contact, sms.getContent(), communicationCenter).show();
            }
        });
        communicationCenter.stateChanged = true;
        dataStore.addMessageToConversation(sms.getSender().getPhoneNumber(), sms.getContent(), false, sms.getTimeStamp());
    }

    private void executeContact(final Message.Contact contact) {
        System.out.println("received: ContactView "+contact.getName());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                communicationCenter.addContactIfNew(contact);
            }
        });
    }
}
