package com.jbull.hermes;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordan on 3/18/14.
 */
public class MessageHelper {
    // auto-detect the length of a header
    public static final int HEADER_LENGTH = createHeader(createSetupMessage(0), 1).getSerializedSize();

    public static Message.SetupMessage createSetupMessage(int sendPeriod) {
        return Message.SetupMessage.getDefaultInstance().toBuilder().setSendPeriod(sendPeriod).buildPartial();
    }

    public static Message.SmsMessage createSmsMessage(Message.Contact sender, String content, long timeMs, ArrayList<Message.Contact> recipents, boolean notify) {
        Message.SmsMessage sms = Message.SmsMessage.newBuilder()
                .setTimeStamp(timeMs)
                .setSender(sender)
                .setContent(content)
                .addAllRecipents(recipents)
                .setNotify(notify)
                .buildPartial();
        return sms;
    }

    public static Message.Contact createContact(String name, String phoneNumber, ByteString imageData, Integer msgNum) {
        Message.Contact.Builder builder = Message.Contact.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setName(name);
        if (imageData != null)
            builder.setImage(imageData);
        if (msgNum != null)
            builder.setMsgNum(msgNum);
        return builder.buildPartial();
    }

    public static Message.BatchContacts createBatchContacts(List<Message.Contact> contacts) {
        return Message.BatchContacts.newBuilder()
                .addAllContacts(contacts)
                .buildPartial();
    }

    public static Message.Header createHeader(GeneratedMessage msg, int msgNum) {
        Message.Header.Type type = null;
        if (msg instanceof Message.SetupMessage) {
            type = Message.Header.Type.SETUPMESSAGE;
        } else if (msg instanceof Message.SmsMessage) {
            type = Message.Header.Type.SMSMESSAGE;
        } else if (msg instanceof Message.Contact) {
            type = Message.Header.Type.CONTACT;
        } else if (msg instanceof Message.Mode) {
            type = Message.Header.Type.MODE;
        } else if (msg instanceof Message.SyncContacts) {
            type = Message.Header.Type.SYNCCONTACTS;
        } else if (msg instanceof Message.BatchContacts) {
            type = Message.Header.Type.BATCHCONTACTS;
        } else {
            // Should never get here
            throw new RuntimeException("Header could not be created for this type");
        }
        msg = returnWithMsgNum(type, msg, msgNum);
        Message.Header header = Message.Header.newBuilder()
                .setMsgNum(msgNum)
                .setLength(msg.getSerializedSize())
                .setType(type)
                .build();
        return header;
    }

    public static Message.Mode createModeMessage(boolean serverSend, long lastUpdated) {
        return Message.Mode.newBuilder()
                .setCurrentTimestamp(System.currentTimeMillis())
                .setServerSend(serverSend)
                .setLastUpdate(lastUpdated)
                .buildPartial();
    }

    public static GeneratedMessage constructFromBytes(Message.Header.Type type, byte[] buffer) throws InvalidProtocolBufferException {
        if (type == Message.Header.Type.SMSMESSAGE) {
            return Message.SmsMessage.parseFrom(buffer);
        } else if (type == Message.Header.Type.MODE) {
            return Message.Mode.parseFrom(buffer);
        } else if (type == Message.Header.Type.SETUPMESSAGE) {
            return Message.SetupMessage.parseFrom(buffer);
        } else if (type == Message.Header.Type.CONTACT) {
            return Message.Contact.parseFrom(buffer);
        } else if (type == Message.Header.Type.SYNCCONTACTS) {
            return Message.SyncContacts.parseFrom(buffer);
        } else if (type == Message.Header.Type.BATCHCONTACTS) {
            return Message.BatchContacts.parseFrom(buffer);
        } else {
            // should not reach this point
            throw new RuntimeException("Could not construct a message of this type");
        }
    }

    public static Message.Ack createAck(int msgNum) {
        return Message.Ack.newBuilder()
                .setMsgNum(msgNum)
                .build();
    }

    public static int ackMessageSize() {
        return createAck(0).getSerializedSize();
    }

    public static int readNumFromSerializedAck(byte[] ackBytes) throws InvalidProtocolBufferException {
        return Message.Ack.parseFrom(ackBytes).getMsgNum();
    }

    public static Message.SyncContacts createSyncContacts() {
        return Message.SyncContacts.getDefaultInstance();
    }

    public static class MessageNumParser implements Connection.MsgNumParser {
        private Message.Header.Type type;

        public  MessageNumParser(Message.Header.Type type) {
            this.type = type;
        }
        @Override
        public int parseMsgNum(byte[] serializedMsg) throws IOException {
            if (type == null) {
                Message.Header header = Message.Header.parseFrom(serializedMsg);
                return header.getMsgNum();
            }
            GeneratedMessage msg = constructFromBytes(type, serializedMsg);
            return (Integer) msg.getField(msg.getDescriptorForType().findFieldByName("msgNum"));
        }
    }

    public static GeneratedMessage returnWithMsgNum(Message.Header.Type type, GeneratedMessage msg, int msgNum) {
        switch (type) {
            case SETUPMESSAGE:
                return ((Message.SetupMessage) msg).toBuilder().setMsgNum(msgNum).build();
            case SMSMESSAGE:
                Message.SmsMessage sms = ((Message.SmsMessage) msg);
                ArrayList<Message.Contact> contacts = new ArrayList<Message.Contact>();
                for (Message.Contact contact : sms.getRecipentsList()) {
                    contacts.add((Message.Contact) returnWithMsgNum(Message.Header.Type.CONTACT, contact, msgNum));
                }
                sms = sms.toBuilder()
                    .setMsgNum(msgNum)
                    .setSender((Message.Contact) returnWithMsgNum(Message.Header.Type.CONTACT, sms.getSender(), msgNum))
                    .clearRecipents()
                    .addAllRecipents(contacts)
                    .build();
                return sms;
            case MODE:
                return ((Message.Mode) msg).toBuilder().setMsgNum(msgNum).build();
            case CONTACT:
                return ((Message.Contact) msg).toBuilder().setMsgNum(msgNum).build();
            case SYNCCONTACTS:
                return ((Message.SyncContacts) msg).toBuilder().setMsgNum(msgNum).build();
            case BATCHCONTACTS:
                return ((Message.BatchContacts) msg).toBuilder().setMsgNum(msgNum).build();
            default:
                throw new RuntimeException("Should not reach this point");
        }
    }
}
