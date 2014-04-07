package com.jbull.hermes;

import com.google.protobuf.GeneratedMessage;
import junit.framework.TestCase;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MessageListenerTest extends TestCase {
    private MessageListener listener;
    MessageReactor reactor;
    Connection conn;
    private int numRetries;
    private int msgAckNum1;
    private GeneratedMessage smsMsg;
    private Message.Header headerSMS;
    private GeneratedMessage modeMsg;
    private int msgAckNum2;
    private Message.Header headerMode;

    public void setUp() throws Exception {
        conn = mock(Connection.class);
        reactor = mock(MessageReactor.class);
        numRetries = 0;
        listener = new MessageListener(conn, reactor, numRetries);
        msgAckNum1 = new Random().nextInt(9999);
        msgAckNum2 = new Random().nextInt(9999);
        smsMsg = MessageHelper.createSmsMessage(MessageHelper.createContact("name", "number", null, null).toBuilder().setMsgNum(msgAckNum1).build(), "smsMsg content", System.currentTimeMillis(), new ArrayList<Message.Contact>(), true).toBuilder().setMsgNum(msgAckNum1).build();
        modeMsg = MessageHelper.createModeMessage(true, System.currentTimeMillis()).toBuilder().setMsgNum(msgAckNum2).build();
        headerSMS = MessageHelper.createHeader(smsMsg, msgAckNum1);
        headerMode = MessageHelper.createHeader(modeMsg, msgAckNum2);
    }

    public void testListen() throws Exception {
        // tests handling of header and message and continues listening
        when(reactor.executeMessage(any(Message.Header.Type.class), any(GeneratedMessage.class))).thenReturn(true);
        Connection.ReceiveResponse response = mock(Connection.ReceiveResponse.class);
        when(response.isSuccess()).thenReturn(true).thenReturn(true);
        when(response.getData()).thenReturn(headerSMS.toByteArray()).thenReturn(smsMsg.toByteArray());
        when(conn.receive(eq(MessageHelper.HEADER_LENGTH), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        when(conn.receive(eq(smsMsg.getSerializedSize()), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        Mode mode = listener.listen();
        InOrder inOrder = inOrder(conn);
        inOrder.verify(conn).receive(eq(MessageHelper.HEADER_LENGTH), any(Connection.MsgNumParser.class), eq(numRetries));
        inOrder.verify(conn).receive(eq(smsMsg.getSerializedSize()), any(Connection.MsgNumParser.class), eq(numRetries));
        verify(reactor).executeMessage(headerSMS.getType(), smsMsg);
        assertEquals(Mode.LISTENING, mode);

        // stops listening when receiving false
        when(reactor.executeMessage(any(Message.Header.Type.class), any(GeneratedMessage.class))).thenReturn(false);
        response = mock(Connection.ReceiveResponse.class);
        when(response.isSuccess()).thenReturn(true).thenReturn(true);
        when(response.getData()).thenReturn(headerMode.toByteArray()).thenReturn(modeMsg.toByteArray());
        when(conn.receive(eq(MessageHelper.HEADER_LENGTH), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        when(conn.receive(eq(modeMsg.getSerializedSize()), any(Connection.MsgNumParser.class), eq(numRetries))).thenReturn(response);
        mode = listener.listen();
        inOrder = inOrder(conn);
        inOrder.verify(conn).receive(eq(MessageHelper.HEADER_LENGTH), any(Connection.MsgNumParser.class), eq(numRetries));
        inOrder.verify(conn).receive(eq(modeMsg.getSerializedSize()), any(Connection.MsgNumParser.class), eq(numRetries));
        verify(reactor).executeMessage(headerMode.getType(), modeMsg);
        assertEquals(Mode.SENDING, mode);
    }
}
