package com.jbull.hermes.messages;

import junit.framework.TestCase;

/**
 * Created by Jordan on 4/14/14.
 */
public class ModeMessageTest extends TestCase {
    private long lastUpdate;
    private long curTime;
    private boolean serverSend;
    private ModeMessage modeMessage;

    public void setUp() throws Exception {
        super.setUp();
        lastUpdate = System.currentTimeMillis();
        curTime = System.currentTimeMillis();
        serverSend = true;
        modeMessage = new ModeMessage(lastUpdate, curTime, serverSend);
    }

    public void testModeMessage() throws Exception {
        ProtobufRep.Mode proto = modeMessage.getProtobufRep();
        ModeMessage rebuilt = ModeMessage.createFromProtobufRep(proto, ModeMessage.class);
        assertEquals(lastUpdate, rebuilt.getLastUpdateTimestamp());
        assertEquals(curTime, rebuilt.getCurrentTimestamp());
        assertEquals(serverSend, rebuilt.isServerSend());
    }
}
