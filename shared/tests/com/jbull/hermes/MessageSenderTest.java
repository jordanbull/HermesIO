package com.jbull.hermes;

import com.jbull.hermes.messages.Header;
import com.jbull.hermes.messages.Packet;
import com.jbull.hermes.messages.SetupMessage;
import junit.framework.TestCase;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Random;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


public class MessageSenderTest extends TestCase {
    private MessageSender sender;
    private Connection conn;
    private SetupMessage setupMessage;
    private int msgNum1;
    private int msgNum2;
    private int numRetries;
    private int sendPeriod;
    private Packet packet;
    private Header header;

    public void setUp() throws Exception {
        conn = mock(Connection.class);
        Connection.SendResponse sendResponse = mock(Connection.SendResponse.class);
        when(sendResponse.isSuccess()).thenReturn(true).thenReturn(true);
        msgNum1 = new Random().nextInt(99);
        numRetries = new Random().nextInt(99);
        when(conn.getSendMsgNum()).thenReturn(msgNum1).thenReturn(msgNum2);
        when(conn.send(Mockito.any(byte[].class), anyInt(), anyInt())).thenReturn(sendResponse);
        when(conn.send(Mockito.any(byte[].class), anyInt())).thenReturn(sendResponse);
        sender = new MessageSender(conn, numRetries);
        sendPeriod = 500;
        setupMessage = new SetupMessage(sendPeriod);
        packet = new Packet();
        packet.addMessage(setupMessage);
        header = packet.getHeader(msgNum1);
    }

    public void testSend() throws Exception {
        sender.send(packet);
        InOrder inOrder = inOrder(conn);
        inOrder.verify(conn).send(header.toBytes(), msgNum1, numRetries);
        inOrder.verify(conn).send(packet.getBytes(), msgNum1, numRetries);
    }
}
