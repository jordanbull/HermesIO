package com.jbull.hermes.messages;

public class SyncContactsMessage extends HermesMessage<ProtobufRep.SyncContacts> {

    public SyncContactsMessage() {}

    @Override
    public ProtobufRep.SyncContacts getProtobufRep() {
        return ProtobufRep.SyncContacts.getDefaultInstance();
    }

    @Override
    public SyncContactsMessage fromProtobufRep(ProtobufRep.SyncContacts protobufRep) {
        return this;
    }
}
