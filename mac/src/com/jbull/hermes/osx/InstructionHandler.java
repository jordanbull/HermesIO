package com.jbull.hermes.osx;

import com.google.protobuf.GeneratedMessage;
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
        state.addSms(sms, false);
    }

    private void executeContact(final Message.Contact contact) {
        System.out.println("received: ContactView "+contact.getName());
        state.addContact(contact);
    }
}
