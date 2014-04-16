package com.jbull.hermes;

import com.jbull.hermes.messages.*;

/**
 * The MessageReactor handles any actions that should be taken by the application that is receiving messages
 */
public abstract class MessageReactor {
    /**
     * Executes any actions that should be performed as a result of receiving msg from the server
     * @param packet the message received from the server
     * @returns true if the communicationManager should continue to listen and false if it should stop listening
     */
    public boolean executeMessage(Packet packet) {
        for (SmsMessage msg : packet.getSmsMessages()) {
            executeSms(msg);
        }
        for (SetupMessage msg : packet.getSetupMessages()) {
            executeSetup(msg);
        }
        for (ContactMessage msg : packet.getContactMessages()) {
            executeContact(msg);
        }
        for (SyncContactsMessage msg : packet.getSyncContactsMessages()) {
            executeSyncContacts(msg);
        }
        for (Packet p : packet.getPackets()) {
            executeMessage(p);
        }

        // Mode must be last since it can return
        for (ModeMessage msg : packet.getModeMessages()) {
            Logger.log("received: Mode " + Long.toString(System.currentTimeMillis()));
            return executeMode(msg);
        }
        return true;
    }

    protected abstract boolean executeMode(ModeMessage msg);

    protected abstract void executeSyncContacts(SyncContactsMessage msg);

    protected abstract void executeContact(ContactMessage msg);

    protected abstract void executeSetup(SetupMessage msg);

    protected abstract void executeSms(SmsMessage msg);
}
