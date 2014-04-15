package com.jbull.hermes.messages;

import junit.framework.TestCase;

public class SmsMessageTest extends TestCase {
    private ContactMessage sender;
    private ContactMessage recipient;
    private SmsMessage sms;
    private String content;
    private long time;

    public void setUp() throws Exception {
        super.setUp();
        sender = new ContactMessage("1", "n");
        recipient = new ContactMessage("2", "nm");
        content = "content";
        time = System.currentTimeMillis();
        sms = new SmsMessage(sender, recipient, "content", time);
    }

    public void testSms() throws Exception {
        ProtobufRep.Sms proto = sms.getProtobufRep();
        SmsMessage rebuilt = SmsMessage.createFromProtobufRep(proto, SmsMessage.class);
        assertEquals(content, rebuilt.getContent());
        assertEquals(time, rebuilt.getTimeMillis());
        assertEquals(sender.getPhoneNumber(), rebuilt.getSender().getPhoneNumber());
        assertEquals(recipient.getPhoneNumber(), rebuilt.getRecipient().getPhoneNumber());
    }
}
