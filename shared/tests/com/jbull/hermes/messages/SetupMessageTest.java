package com.jbull.hermes.messages;

import junit.framework.TestCase;

import java.util.Random;

public class SetupMessageTest extends TestCase {
    private int sendPeriod;
    private SetupMessage setupMessage;

    public void setUp() throws Exception {
        super.setUp();
        sendPeriod = new Random(99).nextInt();
        setupMessage = new SetupMessage(sendPeriod);
    }

    public void testSetupMessageTest() throws Exception {
        ProtobufRep.Setup proto = setupMessage.getProtobufRep();
        SetupMessage rebuilt =  SetupMessage.createFromProtobufRep(proto, SetupMessage.class);
        assertEquals(sendPeriod, rebuilt.getSendPeriod());
        assertEquals(setupMessage.getAppName(), rebuilt.getAppName());
        assertEquals(setupMessage.getVersion(), rebuilt.getVersion());
    }
}
