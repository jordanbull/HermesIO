package com.jbull.hermes;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public class CommunicationSchedulerTest extends TestCase {
    private Listener listener;
    private Sender<String> sender;
    private CommunicationScheduler<String, String> commScheduler;

    public void setUp() throws Exception {
        listener = mock(Listener.class);
        sender = mock(Sender.class);
        commScheduler = new CommunicationScheduler<String, String>(sender, listener, null) {
            public void startSending() {mode = Mode.SENDING;}

            @Override
            public void flush() {

            }

            public void startListening() {mode = Mode.LISTENING;}
        };
    }

    public void testSend() throws Exception {
        // TODO
    }

    public void testFlush() throws Exception {
        // TODO
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
