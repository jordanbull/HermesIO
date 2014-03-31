package com.jbull.hermes.android;

import com.jbull.hermes.Listener;
import com.jbull.hermes.Mode;
import com.jbull.hermes.Sender;
import junit.framework.TestCase;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

/**
 * Created by Jordan on 3/30/14.
 */
public class SendFavoredCommunicationSchedulerTest extends TestCase {

    private Sender<String> sender;
    private Listener listener;
    private int sendWindow;
    private SendFavoredCommunicationScheduler commScheduler;

    public void setUp() throws Exception {
        listener = mock(Listener.class);
        sender = mock(Sender.class);
        sendWindow = -1;
        commScheduler = new SendFavoredCommunicationScheduler(sender, listener, sendWindow);
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
        commScheduler = new SendFavoredCommunicationScheduler(sender, listener, 5);
        commScheduler.send("1");
        when(listener.listen()).thenReturn(Mode.STOPPED);
        commScheduler.start();
        InOrder inOrder = inOrder(sender, listener);
        inOrder.verify(sender, times(1)).send("1");
        inOrder.verify(listener, times(1)).listen();

        //alternate send and listen
        listener = mock(Listener.class);
        sender = mock(Sender.class);
        commScheduler = new SendFavoredCommunicationScheduler(sender, listener, 5);
        commScheduler.send("1");
        when(listener.listen()).thenReturn(Mode.SENDING).thenReturn(Mode.STOPPED);
        commScheduler.start();
        inOrder = inOrder(sender, listener);
        inOrder.verify(sender, times(1)).send("1");
        inOrder.verify(listener, times(2)).listen();
    }

}
