package com.jbull.hermes.messages;

public class SetupMessage extends HermesMessage<ProtobufRep.Setup> {
    private int sendPeriod;
    int version = ProtobufRep.Setup.getDefaultInstance().getVersion();
    String appName = ProtobufRep.Setup.getDefaultInstance().getApplicationName();

    private SetupMessage() {}

    protected SetupMessage(int sendPeriod) {
        this.sendPeriod = sendPeriod;
    }

    @Override
    public ProtobufRep.Setup getProtobufRep() {
        return ProtobufRep.Setup.newBuilder()
                .setSendPeriod(sendPeriod)
                .setVersion(version)
                .setApplicationName(appName)
                .build();
    }

    @Override
    public SetupMessage fromProtobufRep(ProtobufRep.Setup protobufRep) {
        this.sendPeriod = protobufRep.getSendPeriod();
        this.version = protobufRep.getVersion();
        this.appName = protobufRep.getApplicationName();
        return this;
    }

    public int getSendPeriod() {
        return sendPeriod;
    }

    public int getVersion() {
        return version;
    }

    public String getAppName() {
        return appName;
    }
}
