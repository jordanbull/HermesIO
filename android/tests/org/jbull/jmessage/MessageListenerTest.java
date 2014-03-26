package org.jbull.jmessage;

import com.google.protobuf.GeneratedMessage;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageListenerTest extends TestCase {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    public void testListen() throws Exception {
        TCPConnection.TCPServer server = TCPConnection.createTCPServer(PORT);
        TestInstrHandler trueInstrHandler = new TestInstrHandler(true);
        final MessageListener trueListener = new MessageListener(HOST, PORT, trueInstrHandler);
        /* Test that the process of reading a header and message and passing it to the
           InstructionHandler
         */
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assertEquals(CommunicationManager.Mode.LISTENING, trueListener.listen());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
        Message.SmsMessage sms = MessageHandler.createSmsMessage(
                MessageHandler.createContact(
                        "name",
                        "phoneNumber",
                        null),
                "text content",
                System.currentTimeMillis(),
                new ArrayList<Message.Contact>());
        sendSmsToListener(server, sms);
        t.join();
        assertEquals(Message.Header.Type.SMSMESSAGE, trueInstrHandler.recentType);
        assertEquals(sms, trueInstrHandler.recentMsg);

        // case where the CommunicationManager stops listening and starts sending
        TestInstrHandler falseInstrHandler = new TestInstrHandler(false);
        final MessageListener falseListener = new MessageListener(HOST, PORT, falseInstrHandler);
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assertEquals(CommunicationManager.Mode.SENDING, falseListener.listen());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
        sendSmsToListener(server, sms);
        t.join();
      }

    private void sendSmsToListener(TCPConnection.TCPServer server, Message.SmsMessage sms) throws IOException {
        TCPConnection conn = server.accept();
        Message.Header header = MessageHandler.createHeader(sms, 1);
        conn.write(header.toByteArray());
        byte[] buffer = new byte[header.toByteArray().length];
        conn.read(buffer);
        conn.close();
        conn = server.accept();
        Assert.assertArrayEquals(header.toByteArray(), buffer);
        conn.write(sms.toByteArray());
        conn.close();
    }

    public class TestInstrHandler implements MessageListener.InstructionHandler {
        private final boolean returnVal;
        public Message.Header.Type recentType;
        public GeneratedMessage recentMsg;

        public TestInstrHandler(boolean returnVal) {
            this.returnVal = returnVal;
        }

        public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg) {
            recentType = type;
            recentMsg = msg;
            return returnVal;
        }
    }
}
