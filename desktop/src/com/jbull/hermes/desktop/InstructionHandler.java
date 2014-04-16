package com.jbull.hermes.desktop;

import com.jbull.hermes.Logger;
import com.jbull.hermes.MessageReactor;
import com.jbull.hermes.messages.*;

public class InstructionHandler implements MessageReactor {

    private State state;

    public InstructionHandler(State state) {
        this.state = state;
    }

    @Override
    public boolean executeMessage(Packet packet) {
        for (SmsMessage msg : packet.getSmsMessages()) {
            executeSMS(msg);
        }
        for (SetupMessage msg : packet.getSetupMessages()) {
            executeSetup(msg);
        }
        for (ContactMessage msg : packet.getContactMessages()) {
            executeContact(msg);
        }

        // Mode must be last since it can return
        for (ModeMessage msg : packet.getModeMessages()) {
            Logger.log("received: Mode "+Long.toString(System.currentTimeMillis()));
            executeMode();
            return !msg.isServerSend();
        }
        return true;
    }

    private void executeMode() {
        state.updateTimeout();
    }

    private void executeSetup(SetupMessage msg) {
        Logger.log("received: SetupMessage");
        state.connected(msg.getSendPeriod());
    }

    private void executeSMS(final SmsMessage sms) {
        Logger.log("received: SMSMessage");
        state.notify(sms.getSender().getDisplayName(), sms.getContent(), sms.getSender().getImageData());
        state.addSms(sms, false);
    }

    private void executeContact(final ContactMessage contact) {
        Logger.log("received: ContactView " + contact.getDisplayName());
        state.addContact(contact);
    }
}
