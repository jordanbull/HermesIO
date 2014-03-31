package com.jbull.hermes;

import junit.framework.TestCase;
import org.mockito.InOrder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Jordan on 3/30/14.
 */
public class CommunicationSchedulerTest extends TestCase {
    private Listener listener;
    private Sender<String> sender;
    private CommunicationScheduler<String> commScheduler;

    public void setUp() throws Exception {
        listener = mock(Listener.class);
        sender = mock(Sender.class);
        commScheduler = new CommunicationScheduler<String>(sender, listener) {
            public void startSending() {mode = Mode.SENDING;}
            public void startListening() {mode = Mode.LISTENING;}
        };
    }

    public void testSend() throws Exception {
        //test sends immediately when in sending mode
        commScheduler.startSending();
        verify(sender, never()).send(anyString());
        commScheduler.send("1");
        verify(sender, times(1)).send(anyString());
        verify(sender, times(1)).send("1");
        verify(sender, never()).send("2");
        commScheduler.send("2");
        verify(sender, times(2)).send(anyString());
        verify(sender, times(1)).send("1");
        verify(sender, times(1)).send("2");

        //verify nothing sent when not in sending
        commScheduler.startListening();
        commScheduler.send("3");
        verify(sender, times(2)).send(anyString());
        verify(sender, never()).send("3");
    }

    public void testFlush() throws Exception {
        //flush sends nothing if nothing queued
        commScheduler.startSending();
        commScheduler.flush();
        verify(sender, never()).send(anyString());

        //flush sends nothing if not in sending mode
        commScheduler.startListening();
        commScheduler.send("1");
        commScheduler.send("2");
        commScheduler.send("3");
        commScheduler.flush();
        verify(sender, never()).send(anyString());

        //flush sends in order
        commScheduler.startSending();
        commScheduler.flush();
        InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).send("1");
        inOrder.verify(sender).send("2");
        inOrder.verify(sender).send("3");
    }

    public void testListenLoop() throws Exception {
        //does not listen if not in listening mode
        commScheduler.listenLoop();
        verify(listener, never()).listen();

        //calls listen until it returns something other than Mode.LISTENING
        when(listener.listen()).thenReturn(Mode.LISTENING, Mode.LISTENING, Mode.LISTENING, Mode.SENDING);
        commScheduler.startListening();
        assertTrue(commScheduler.isListening());
        commScheduler.listenLoop();
        verify(listener, times(4)).listen();
        assertTrue(commScheduler.isSending());
    }

    public void testIsSending() throws Exception {
        assertFalse(commScheduler.isSending());
        commScheduler.startListening();
        assertFalse(commScheduler.isSending());
        commScheduler.startSending();
        assertTrue(commScheduler.isSending());
        commScheduler.startListening();
        assertFalse(commScheduler.isSending());
    }

    public void testIsListening() throws Exception {
        assertFalse(commScheduler.isListening());
        commScheduler.startSending();
        assertFalse(commScheduler.isListening());
        commScheduler.startListening();
        assertTrue(commScheduler.isListening());
        commScheduler.startSending();
        assertFalse(commScheduler.isListening());
    }

    public void testIsStopped() throws Exception {
        assertTrue(commScheduler.isStopped());
        commScheduler.startSending();
        assertFalse(commScheduler.isStopped());
        commScheduler.startListening();
        assertFalse(commScheduler.isStopped());
        commScheduler.stop();
        assertTrue(commScheduler.isStopped());
    }
}
