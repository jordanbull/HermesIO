package com.jbull.hermes.desktop;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.Logger;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageReactor;

public class InstructionHandler implements MessageReactor {

    private State state;

    public InstructionHandler(State state) {
        this.state = state;
    }

    @Override
    public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg) {
        if (type == Message.Header.Type.MODE) {
            Logger.log("received: Mode "+Long.toString(System.currentTimeMillis()));
            Message.Mode mode = (Message.Mode) msg;
            executeMode();
            return !mode.getServerSend();
        } else if (type == Message.Header.Type.SMSMESSAGE) {
            executeSMS((Message.SmsMessage) msg);
        } else if (type == Message.Header.Type.SETUPMESSAGE) {
            executeSetup((Message.SetupMessage) msg);
        } else if (type == Message.Header.Type.CONTACT) {
            executeContact((Message.Contact) msg);
        } else if (type == Message.Header.Type.BATCHCONTACTS) {
            executeBatchContacts((Message.BatchContacts) msg);
        }
        return true;
    }

    private void executeMode() {
        state.updateTimeout();
    }

    private void executeSetup(Message.SetupMessage msg) {
        Logger.log("received: SetupMessage");
        state.connected(msg.getSendPeriod());
    }

    private void executeBatchContacts(Message.BatchContacts msg) {
        for (Message.Contact contact : msg.getContactsList()) {
            executeContact(contact);
        }
    }

    private void executeSMS(final Message.SmsMessage sms) {
        Logger.log("received: SMSMessage");
        state.notify(sms.getSender().getName(), sms.getContent(), sms.getSender().getImage().toByteArray());
        state.addSms(sms, false);
    }

    private void executeContact(final Message.Contact contact) {
        Logger.log("received: ContactView " + contact.getName());
        state.addContact(contact);
    }
}
