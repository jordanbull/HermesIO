package com.jbull.hermes;

import com.jbull.hermes.messages.ContactMessage;
import com.jbull.hermes.messages.Header;
import com.jbull.hermes.messages.ModeMessage;
import com.jbull.hermes.messages.Packet;
import junit.framework.TestCase;
import org.mockito.InOrder;

import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MessageListenerTest extends TestCase {
    private MessageListener listener;
    MessageReactor reactor;
    Connection conn;
    private int numRetries;
    private int msgAckNum1;
    private int msgAckNum2;
    private ContactMessage contact;
    private ModeMessage mode;
    private Packet packet;
    private Header header;

    public void setUp() throws Exception {
        conn = mock(Connection.class);
        reactor = mock(MessageReactor.class);
        numRetries = 0;
        listener = new MessageListener(conn, reactor, numRetries);
        msgAckNum1 = new Random().nextInt(9999);
        msgAckNum2 = new Random().nextInt(9999);
        contact = new ContactMessage("number", "name");
        mode = new ModeMessage(0 ,0, true);
        packet = new Packet();
        packet.addMessage(contact);
        packet.addMessage(mode);
        header = packet.getHeader(0);
    }

    public void testListen() throws Exception {
        // tests handling of header and message and continues listening
        when(reactor.executeMessage(any(Packet.class))).thenReturn(true);
        Connection.ReceiveResponse response = mock(Connection.ReceiveResponse.class);
        when(response.isSuccess()).thenReturn(true).thenReturn(true);
        when(response.getData()).thenReturn(header.toBytes()).thenReturn(packet.getBytes());
        when(conn.receive(eq(Header.LENGTH), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        when(conn.receive(eq(header.getLength()), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        Mode mode = listener.listen();
        InOrder inOrder = inOrder(conn);
        inOrder.verify(conn).receive(eq(Header.LENGTH), any(Connection.MsgNumParser.class), eq(numRetries));
        inOrder.verify(conn).receive(eq(header.getLength()), any(Connection.MsgNumParser.class), eq(numRetries));
        verify(reactor).executeMessage(any(Packet.class));
        assertEquals(Mode.LISTENING, mode);

        // stops listening when receiving false
        //
        when(reactor.executeMessage(any(Packet.class))).thenReturn(false);
        response = mock(Connection.ReceiveResponse.class);
        when(response.isSuccess()).thenReturn(true).thenReturn(true);
        when(response.getData()).thenReturn(header.toBytes()).thenReturn(packet.getBytes());
        when(conn.receive(eq(Header.LENGTH), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        when(conn.receive(eq(header.getLength()), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        mode = listener.listen();
        inOrder = inOrder(conn);
        inOrder.verify(conn).receive(eq(Header.LENGTH), any(Connection.MsgNumParser.class), eq(numRetries));
        inOrder.verify(conn).receive(eq(header.getLength()), any(Connection.MsgNumParser.class), eq(numRetries));
        verify(reactor, times(2)).executeMessage(any(Packet.class));
        assertEquals(Mode.SENDING, mode);
    }
}
