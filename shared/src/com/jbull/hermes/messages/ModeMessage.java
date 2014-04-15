package com.jbull.hermes.messages;

public class ModeMessage extends   HermesMessage<ProtobufRep.Mode>{

    private long lastUpdateTimestamp;
    private long currentTimestamp;
    private boolean serverSend;

    private ModeMessage(){}

    public ModeMessage(long lastUpdateTimestamp, long currentTimestamp, boolean serverSend) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.currentTimestamp = currentTimestamp;
        this.serverSend = serverSend;
    }

    @Override
    public ProtobufRep.Mode getProtobufRep() {
        return ProtobufRep.Mode.newBuilder()
                .setCurrentTimestamp(currentTimestamp)
                .setLastUpdate(lastUpdateTimestamp)
                .setServerSend(serverSend)
                .build();
    }

    @Override
    public ModeMessage fromProtobufRep(ProtobufRep.Mode protobufRep) {
        this.lastUpdateTimestamp = protobufRep.getLastUpdate();
        this.currentTimestamp = protobufRep.getCurrentTimestamp();
        this.serverSend = protobufRep.getServerSend();
        return this;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }

    public boolean isServerSend() {
        return serverSend;
    }
}
