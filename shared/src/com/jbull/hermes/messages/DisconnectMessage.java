package com.jbull.hermes.messages;

public class DisconnectMessage extends HermesMessage<ProtobufRep.Disconnect> {

    public DisconnectMessage() {
    }

    @Override
    public ProtobufRep.Disconnect getProtobufRep() {
        return ProtobufRep.Disconnect.getDefaultInstance();
    }

    @Override
    public HermesMessage<ProtobufRep.Disconnect> fromProtobufRep(ProtobufRep.Disconnect protobufRep) {
        return this;
    }
}
