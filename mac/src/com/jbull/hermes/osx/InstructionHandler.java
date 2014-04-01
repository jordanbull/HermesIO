package com.jbull.hermes.osx;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageReactor;
import javafx.application.Platform;

public class InstructionHandler implements MessageReactor {

    private CommunicationCenter communicationCenter;

    public InstructionHandler(CommunicationCenter communicationCenter) {
        this.communicationCenter = communicationCenter;
    }

    @Override
    public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg) {
        if (type == Message.Header.Type.MODE) {
            System.out.println("received: Mode");
            Message.Mode mode = (Message.Mode) msg;
            return !mode.getServerSend();
        } else if (type == Message.Header.Type.SMSMESSAGE) {
            executeSMS((Message.SmsMessage) msg);
        } else if (type == Message.Header.Type.SETUPMESSAGE) {
            System.out.println("received: SetupMessage");
        }
        return true;
    }

    private void executeSMS(final Message.SmsMessage sms) {
        System.out.println("received: SMSMessage");
        Message.Contact sender = sms.getSender();
        String number = sender.getPhoneNumber();
        final Contact contact = communicationCenter.addContactIfNew(number, sender.getName());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                new Notification(contact, sms.getContent(), communicationCenter).show();
            }
        });

    }
}
