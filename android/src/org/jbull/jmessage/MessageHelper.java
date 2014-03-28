package org.jbull.jmessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

/**
 * Created by jordan on 3/18/14.
 */
public class MessageHelper {
    // auto-detect the length of a header
    public static final int HEADER_LENGTH = createHeader(createSetupMessage(), 1).toByteArray().length;

    public static Message.SetupMessage createSetupMessage() {
        return Message.SetupMessage.getDefaultInstance();
    }

    public static Message.SmsMessage createSmsMessage(Message.Contact sender, String content, long timeMs, ArrayList<Message.Contact> recipents) {
        Message.SmsMessage sms = Message.SmsMessage.newBuilder()
                .setTimeStamp(timeMs)
                .setSender(sender)
                .setContent(content)
                .addAllRecipents(recipents)
                .build();
        return sms;
    }

    public static Message.Contact createContact(String name, String phoneNumber, ByteString imageData) {
        Message.Contact.Builder builder = Message.Contact.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setName(name);
        if (imageData != null)
            builder.setImage(imageData);
        return builder.build();
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
        } else {
            // Should never get here
            assert false;
        }
        Message.Header header = Message.Header.newBuilder()
                .setMsgNum(msgNum)
                .setLength(msg.toByteArray().length)
                .setType(type)
                .build();
        return header;
    }

    public static Message.Mode createModeMessage(boolean serverSend, long lastUpdated) {
        return Message.Mode.newBuilder()
                .setCurrentTimestamp(System.currentTimeMillis())
                .setServerSend(serverSend)
                .setLastUpdate(lastUpdated)
                .build();
    }

    public static GeneratedMessage constructFromBytes(Message.Header.Type type, byte[] buffer) throws InvalidProtocolBufferException {
        if (type == Message.Header.Type.SMSMESSAGE) {
            return Message.SmsMessage.parseFrom(buffer);
        } else if (type == Message.Header.Type.MODE) {
            return Message.Mode.parseFrom(buffer);
        }
        assert false;
        // TODO: throw a relevant exception
        return null;
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
}
