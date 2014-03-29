package org.jbull.jmessage;

import junit.framework.TestCase;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.net.ServerSocket;
import java.util.Random;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageSenderTest extends TestCase {
    private MessageSender sender;
    private Connection conn;
    private Message.SetupMessage setupMessage;
    private int msgNum1;
    private int msgNum2;
    private int numRetries;

    public void setUp() throws Exception {
        conn = mock(Connection.class);
        Connection.SendResponse sendResponse = mock(Connection.SendResponse.class);
        when(sendResponse.isSuccess()).thenReturn(true).thenReturn(true);
        msgNum1 = new Random().nextInt(99);
        msgNum2 = new Random().nextInt(99);
        numRetries = new Random().nextInt(99);
        when(conn.getSendMsgNum()).thenReturn(msgNum1).thenReturn(msgNum2);
        when(conn.send(Mockito.any(byte[].class), anyInt(), anyInt())).thenReturn(sendResponse);
        when(conn.send(Mockito.any(byte[].class), anyInt())).thenReturn(sendResponse);
        sender = new MessageSender(conn, numRetries);
        setupMessage = MessageHelper.createSetupMessage();
    }

    public void testSend() throws Exception {
        sender.send(setupMessage);
        InOrder inOrder = inOrder(conn);
        inOrder.verify(conn).send(MessageHelper.createHeader(setupMessage, msgNum1).toByteArray(), msgNum1, numRetries);
        inOrder.verify(conn).send(setupMessage.toByteArray(), numRetries);
    }
}
