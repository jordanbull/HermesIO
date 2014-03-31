package com.jbull.hermes;

import com.google.protobuf.GeneratedMessage;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Jordan on 3/30/14.
 */
public class MessageHelperTest extends TestCase {

    private int msgNum;

    public void setUp() throws Exception {
        msgNum = new Random().nextInt(999);
    }

    public void testSetupMessage() throws Exception {
        Message.SetupMessage msg = MessageHelper.createSetupMessage();
        msg = (Message.SetupMessage) MessageHelper.returnWithMsgNum(Message.Header.Type.SETUPMESSAGE, msg, msgNum);
        Message.Header header = MessageHelper.createHeader(msg, msgNum);
        assertEquals(msgNum, msg.getMsgNum());
        byte[] bytes = msg.toByteArray();
        assertEquals(msg.getSerializedSize(), header.getLength());
        GeneratedMessage readMsg = MessageHelper.constructFromBytes(Message.Header.Type.SETUPMESSAGE, bytes);
        assertEquals(msg, readMsg);
    }

    public void testSmsMessage() throws Exception {
        Message.Contact sender = MessageHelper.createContact("sender name", "sender number", null);
        Message.Contact recipent = MessageHelper.createContact("recipent name", "recipent number", null);
        ArrayList<Message.Contact> recipents = new ArrayList<Message.Contact>();
        recipents.add(recipent);
        Message.SmsMessage msg = MessageHelper.createSmsMessage(sender, "content", 9999, recipents);
        msg = (Message.SmsMessage) MessageHelper.returnWithMsgNum(Message.Header.Type.SMSMESSAGE, msg, msgNum);
        Message.Header header = MessageHelper.createHeader(msg, msgNum);
        byte[] headerData = header.toByteArray();
        assertEquals(msgNum, msg.getMsgNum());
        byte[] bytes = msg.toByteArray();
        assertEquals(msg.getSerializedSize(), header.getLength());
        GeneratedMessage readMsg = MessageHelper.constructFromBytes(Message.Header.Type.SMSMESSAGE, bytes);
        assertEquals(msg, readMsg);

        assertEquals(header, Message.Header.parseFrom(headerData));
        assertEquals(msgNum, new MessageHelper.MessageNumParser(null).parseMsgNum(headerData));
    }
}
