package com.jbull.hermes.desktop;

import com.jbull.hermes.Logger;
import com.jbull.hermes.MessageReactor;
import com.jbull.hermes.messages.*;

public class InstructionHandler extends MessageReactor {

    private State state;

    public InstructionHandler(State state) {
        this.state = state;
    }

    protected void executeSetup(SetupMessage msg) {
        Logger.log("received: SetupMessage");
        state.connected(msg.getSendPeriod(), msg.getVersion());
    }

    @Override
    protected void executeSms(final SmsMessage sms) {
        Logger.log("received: SMSMessage");
        state.notify(sms.getSender().getDisplayName(), sms.getContent(), sms.getSender().getImageData());
        state.addSms(sms, false);
    }

    @Override
    protected void executeDisconnect(DisconnectMessage msg) {
        Logger.log("received: DisconnectMessage");
        state.disconnect();
    }

    @Override
    protected boolean executeMode(ModeMessage msg) {
        state.updateTimeout();
        return !msg.isServerSend();
    }

    @Override
    protected void executeSyncContacts(SyncContactsMessage msg) {

    }

    protected void executeContact(final ContactMessage contact) {
        Logger.log("received: ContactView " + contact.getDisplayName());
        state.addContact(contact);
    }
}
