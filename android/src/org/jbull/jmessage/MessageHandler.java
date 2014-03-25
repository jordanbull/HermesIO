package org.jbull.jmessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

import java.util.ArrayList;

/**
 * Created by jordan on 3/18/14.
 */
public class MessageHandler {

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
        Message.Contact contact = Message.Contact.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setName(name)
                .setImage(imageData)
                .build();
        return contact;
    }

    public  static Message.Header createHeader(GeneratedMessage msg, int msgNum) {
        Message.Header.Type type = null;
        if (msg instanceof Message.SetupMessage) {
            type = Message.Header.Type.SETUPMESSAGE;
        } else if (msg instanceof Message.SmsMessage) {
            type = Message.Header.Type.SMSMESSAGE;
        } else if (msg instanceof Message.Contact) {
            type = Message.Header.Type.CONTACT;
        }
        Message.Header header = Message.Header.newBuilder()
                .setMsgNum(msgNum)
                .setLength(msg.toByteArray().length)
                .setType(type)
                .build();
        return header;
    }
}
