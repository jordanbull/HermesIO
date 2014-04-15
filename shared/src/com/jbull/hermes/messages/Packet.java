package com.jbull.hermes.messages;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

public class Packet {
    int msgNum;
    ArrayList<SmsMessage> smsMessages = new ArrayList<SmsMessage>();
    ArrayList<ContactMessage> contactMessages = new ArrayList<ContactMessage>();
    ArrayList<SetupMessage> setupMessages = new ArrayList<SetupMessage>(1);
    ArrayList<ModeMessage> modeMessages = new ArrayList<ModeMessage>(1);
    ArrayList<SyncContactsMessage> syncContactsMessages = new ArrayList<SyncContactsMessage>(1);

    private ProtobufRep.Packet protobufRep;

    private ProtobufRep.Packet constructProtobufRep() {
        return ProtobufRep.Packet.newBuilder()
                .setMsgNum(msgNum)
                .addAllSms(createProtobufs(smsMessages, ProtobufRep.Sms.class))
                .addAllContact(createProtobufs(contactMessages, ProtobufRep.Contact.class))
                .addAllSetup(createProtobufs(setupMessages, ProtobufRep.Setup.class))
                .addAllMode(createProtobufs(modeMessages, ProtobufRep.Mode.class))
                .addAllSyncContacts(createProtobufs(syncContactsMessages, ProtobufRep.SyncContacts.class))
                .build();
    }

    public byte[] getBytes() throws MessageSerializationException {
        if (protobufRep == null) {
            throw new MessageSerializationException("Protobuf Representation not initialized.", new NullPointerException());
        }
        return protobufRep.toByteArray();
    }

    public Header getHeader(int msgNum) {
        setMsgNum(msgNum);
        protobufRep = constructProtobufRep();
        return new Header(msgNum, protobufRep.getSerializedSize());
    }

    public static Packet fromBytes(byte[] byteRep) throws MessageSerializationException {
        try {
            ProtobufRep.Packet proto = ProtobufRep.Packet.parseFrom(byteRep);
            Packet packet = new Packet();
            packet.msgNum = proto.getMsgNum();
            packet.smsMessages.addAll(fromProtobufs(proto.getSmsList(), SmsMessage.class));
            packet.syncContactsMessages.addAll(fromProtobufs(proto.getSyncContactsList(), SyncContactsMessage.class));
            packet.setupMessages.addAll(fromProtobufs(proto.getSetupList(), SetupMessage.class));
            packet.modeMessages.addAll(fromProtobufs(proto.getModeList(), ModeMessage.class));
            packet.contactMessages.addAll(fromProtobufs(proto.getContactList(), ContactMessage.class));
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

    private static <T extends HermesMessage, P extends GeneratedMessageLite> ArrayList<P> createProtobufs(ArrayList<T> data, Class<P> c) {
        ArrayList<P> outList = new ArrayList<P>(data.size());
        for (T item : data) {
            outList.add((P) item.getProtobufRep());
        }
        return outList;
    }

    private static <T extends HermesMessage, P extends GeneratedMessageLite> ArrayList<T> fromProtobufs(List<P> data, Class<T> c) {
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
}
