package com.jbull.hermes.android;

import com.jbull.hermes.Listener;
import com.jbull.hermes.Mode;
import com.jbull.hermes.Sender;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Created by jordan on 3/25/14.
 */
public class CommunicationManagerTest extends TestCase {

    private TestListener listenOnce;
    private TestListener listenTwice;
    private TestSender sender;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        listenOnce = new TestListener(new Mode[]{Mode.SENDING});
        listenTwice = new TestListener(new Mode[]{Mode.LISTENING, Mode.SENDING});
        sender = new TestSender();
    }

    public void testListenLoop() throws Exception {
        // listens only once and never sends
        CommunicationManager<String> comm = new CommunicationManager<String>(listenOnce, sender, 0);
        comm.setMode(Mode.LISTENING);
        comm.listenLoop();
        assertEquals(1, listenOnce.counter);
        assertEquals(0, sender.counter);

        // will loop until Communication manger returns SENDING
        comm = new CommunicationManager<String>(listenTwice, sender, 0);
        comm.setMode(Mode.LISTENING);
        comm.listenLoop();
        assertEquals(2, listenTwice.counter);
        assertEquals(0, sender.counter);
    }

    public void testFlushMessages() throws Exception {
        CommunicationManager<String> commSender = new CommunicationManager<String>(listenOnce, sender, -1);
        commSender.send("1");
        commSender.send("2");
        commSender.flushMessages();
        // nothing should be sent while not in sending mode
        assertEquals(0, sender.counter);
        commSender.setMode(Mode.SENDING);
        commSender.flushMessages();
        assertEquals(2, sender.counter);
        Assert.assertArrayEquals(new String[]{"1","2"}, sender.sentMsgs.toArray());
    }

    public void testSend() throws Exception {
        CommunicationManager<String> commSender = new CommunicationManager<String>(listenOnce, sender, -1);
        assertEquals(0, sender.counter);
        // make sure the queue is flushed and in order only once sending starts
        commSender.setMode(Mode.SENDING);
        commSender.send("1");
        assertEquals(1, sender.counter);
        commSender.send("2");
        assertEquals(2, sender.counter);
        commSender.send("3");
        assertEquals(3, sender.counter);
        Assert.assertArrayEquals(new String[]{"1","2","3"}, sender.sentMsgs.toArray());
        //does not send when not in sending mode
        commSender.setMode(Mode.STOPPED);
        commSender.send("4");
        assertEquals(3, sender.counter);
        Assert.assertArrayEquals(new String[]{"1","2","3"}, sender.sentMsgs.toArray());
    }

    public void testStartSendTimer() throws Exception {
        CommunicationManager<String> manager = new CommunicationManager<String>(listenOnce, sender, 500);
        assertEquals(Mode.STOPPED, manager.getMode());
        manager.setMode(Mode.SENDING);
        assertEquals(Mode.SENDING, manager.getMode());
        manager.startSendTimer(new Semaphore(0));
        Thread.sleep(600);
        assertEquals(Mode.LISTENING, manager.getMode());
    }

    public class TestListener implements Listener {
        private final Mode[] modes;
        public int counter = 0;

        public TestListener(Mode[] returnModes) {
            modes = returnModes;
        }

        @Override
        public Mode listen() {
            return modes[counter++];
        }
    }

    public class TestSender implements Sender<String> {
        public ArrayList<String> sentMsgs = new ArrayList<String>();
        public int counter = 0;

        @Override
        public void send(String msg) {
            counter++;
            sentMsgs.add(msg);
        }
    }
}
