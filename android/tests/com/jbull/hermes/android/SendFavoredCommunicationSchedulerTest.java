package com.jbull.hermes.android;

import com.jbull.hermes.MessageListener;
import com.jbull.hermes.MessageSender;
import com.jbull.hermes.Mode;
import com.jbull.hermes.messages.Packet;
import com.jbull.hermes.messages.SetupMessage;
import junit.framework.TestCase;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class SendFavoredCommunicationSchedulerTest extends TestCase {

    private MessageSender sender;
    private MessageListener listener;
    private int sendWindow;
    private SendFavoredCommunicationScheduler commScheduler;
    private SetupMessage msg;
    private Runnable startListen;

    public void setUp() throws Exception {
        listener = mock(MessageListener.class);
        sender = mock(MessageSender.class);
        startListen = new Runnable() {
            @Override
            public void run() {
                commScheduler.startListening();
            }
        };
        sendWindow = -1;
        commScheduler = new SendFavoredCommunicationScheduler(sender, listener, startListen, null, sendWindow);
        msg = new SetupMessage(0);
    }

    public void testStart() throws Exception {
        // never listens
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                commScheduler.start();
            }
        });
        thread.start();
        while(thread.isAlive()) {
            commScheduler.stop();
        }
        thread.join();
        verify(listener, never()).listen();

        // send and then listen. stop listening if listener.listen returns non Mode.LISTENING
        commScheduler = new SendFavoredCommunicationScheduler(sender, listener, startListen, null, 5);
        commScheduler.send(msg);
        when(listener.listen()).thenReturn(Mode.STOPPED);
        commScheduler.start();
        InOrder inOrder = inOrder(sender, listener);
        inOrder.verify(sender, times(1)).send(any(Packet.class));
        inOrder.verify(listener, times(1)).listen();

        //alternate send and listen
        listener = mock(MessageListener.class);
        sender = mock(MessageSender.class);
        commScheduler = new SendFavoredCommunicationScheduler(sender, listener, startListen, null, 5);
        commScheduler.send(msg);
        when(listener.listen()).thenReturn(Mode.SENDING).thenReturn(Mode.STOPPED);
        commScheduler.start();
        inOrder = inOrder(sender, listener);
        inOrder.verify(sender, times(1)).send(any(Packet.class));
        inOrder.verify(listener, times(2)).listen();
    }

}
