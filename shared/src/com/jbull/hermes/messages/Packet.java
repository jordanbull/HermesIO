package com.jbull.hermes.messages;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

public class Packet extends HermesMessage<ProtobufRep.Packet> {
    int msgNum;
    ArrayList<SmsMessage> smsMessages = new ArrayList<SmsMessage>();
    ArrayList<ContactMessage> contactMessages = new ArrayList<ContactMessage>();
    ArrayList<SetupMessage> setupMessages = new ArrayList<SetupMessage>(1);
    ArrayList<ModeMessage> modeMessages = new ArrayList<ModeMessage>(1);
    ArrayList<SyncContactsMessage> syncContactsMessages = new ArrayList<SyncContactsMessage>(1);

    public ArrayList<Packet> getPackets() {
        return packets;
    }

    ArrayList<Packet> packets = new ArrayList<Packet>();

    private ProtobufRep.Packet protobufRep;

    public byte[] getBytes() throws MessageSerializationException {
        if (protobufRep == null) {
            throw new MessageSerializationException("Protobuf Representation not initialized.", new NullPointerException());
        }
        return protobufRep.toByteArray();
    }

    public Header getHeader(int msgNum) {
        setMsgNum(msgNum);
        protobufRep = getProtobufRep();
        return new Header(msgNum, protobufRep.getSerializedSize());
    }

    public static Packet fromBytes(byte[] byteRep) throws MessageSerializationException {
        try {
            ProtobufRep.Packet proto = ProtobufRep.Packet.parseFrom(byteRep);
            Packet packet = new Packet();
            packet.fromProtobufRep(proto);
            return packet;
        } catch (InvalidProtocolBufferException e) {
            throw new MessageSerializationException("Error deserializing Packet.", e);
        }
    }

    public void addMessage(HermesMessage msg) throws MessageSerializationException {
        if (msg instanceof SmsMessage) {
            smsMessages.add((SmsMessage) msg);
        } else if (msg instanceof ContactMessage) {
            contactMessages.add((ContactMessage) msg);
        } else if (msg instanceof ModeMessage) {
            modeMessages.add((ModeMessage) msg);
        } else if (msg instanceof SetupMessage) {
            setupMessages.add((SetupMessage) msg);
        } else if (msg instanceof SyncContactsMessage) {
            syncContactsMessages.add((SyncContactsMessage) msg);
        } else {
            throw new MessageSerializationException("Attempting to add an unknown message type: " + msg.getClass().toString());
        }
    }

    private void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    private static <T extends HermesMessage, P extends AbstractMessageLite> ArrayList<P> createProtobufs(ArrayList<T> data, Class<P> c) {
        ArrayList<P> outList = new ArrayList<P>(data.size());
        for (T item : data) {
            outList.add((P) item.getProtobufRep());
        }
        return outList;
    }

    private static <T extends HermesMessage, P extends AbstractMessageLite> ArrayList<T> fromProtobufs(List<P> data, Class<T> c) {
        ArrayList<T> outList = new ArrayList<T>(data.size());
        for (P item : data) {
            outList.add((T) HermesMessage.createFromProtobufRep(item, c));
        }
        return outList;
    }

    public int getMsgNum() {
        return msgNum;
    }

    public ArrayList<SmsMessage> getSmsMessages() {
        return smsMessages;
    }

    public ArrayList<ContactMessage> getContactMessages() {
        return contactMessages;
    }

    public ArrayList<SetupMessage> getSetupMessages() {
        return setupMessages;
    }

    public ArrayList<ModeMessage> getModeMessages() {
        return modeMessages;
    }

    public ArrayList<SyncContactsMessage> getSyncContactsMessages() {
        return syncContactsMessages;
    }

    @Override
    public ProtobufRep.Packet getProtobufRep() {
        return ProtobufRep.Packet.newBuilder()
                .setMsgNum(msgNum)
                .addAllSms(createProtobufs(smsMessages, ProtobufRep.Sms.class))
                .addAllContact(createProtobufs(contactMessages, ProtobufRep.Contact.class))
                .addAllSetup(createProtobufs(setupMessages, ProtobufRep.Setup.class))
                .addAllMode(createProtobufs(modeMessages, ProtobufRep.Mode.class))
                .addAllSyncContacts(createProtobufs(syncContactsMessages, ProtobufRep.SyncContacts.class))
                .addAllPacket(createProtobufs(packets, ProtobufRep.Packet.class))
                .build();
    }

    @Override
    public Packet fromProtobufRep(ProtobufRep.Packet protobufRep) {
        msgNum = protobufRep.getMsgNum();
        smsMessages.addAll(fromProtobufs(protobufRep.getSmsList(), SmsMessage.class));
        syncContactsMessages.addAll(fromProtobufs(protobufRep.getSyncContactsList(), SyncContactsMessage.class));
        setupMessages.addAll(fromProtobufs(protobufRep.getSetupList(), SetupMessage.class));
        modeMessages.addAll(fromProtobufs(protobufRep.getModeList(), ModeMessage.class));
        contactMessages.addAll(fromProtobufs(protobufRep.getContactList(), ContactMessage.class));
        packets.addAll(fromProtobufs(protobufRep.getPacketList(), Packet.class));
        return this;
    }
}
