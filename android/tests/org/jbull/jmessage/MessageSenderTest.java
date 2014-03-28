package org.jbull.jmessage;

import com.google.protobuf.GeneratedMessage;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageSenderTest extends TestCase {
    private final int PORT = 8888;

    public void testByteEquivalent() throws Exception {
        byte[] emptyArray = new byte[] {};
        byte[] oneByte = new byte[] {1};
        byte[] manyBytes = new byte[] {1,2};
        byte[] diffBytes = new byte[] {2,1};
        assertTrue(MessageSender.byteEquivalent(emptyArray, new byte[] {}));
        assertTrue(MessageSender.byteEquivalent(oneByte, new byte[] {1}));
        assertTrue(MessageSender.byteEquivalent(manyBytes, new byte[] {1,2}));
        assertTrue(MessageSender.byteEquivalent(oneByte, manyBytes));
        assertTrue(MessageSender.byteEquivalent(manyBytes, oneByte));
        assertFalse(MessageSender.byteEquivalent(manyBytes, diffBytes));
        assertFalse(MessageSender.byteEquivalent(diffBytes, oneByte));
    }

    /*public void testSend() throws Exception {
        TCPConnection.TCPServer server = TCPConnection.createTCPServer(PORT);
        final MessageSender sender = new MessageSender("localhost", PORT);
        final GeneratedMessage msg = MessageHelper.createSetupMessage();
        Message.Header header = MessageHelper.createHeader(msg, 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sender.send(msg);
                } catch (IOException e) {
                    fail("IOException on send");
                }
            }
        }).start();
        TCPConnection servConn = server.accept();
        byte[] buffer = new byte[header.toByteArray().length];
        servConn.read(buffer);
        Assert.assertArrayEquals(header.toByteArray(), buffer);
        servConn.write(buffer);
        servConn.close();
        servConn = server.accept();
        buffer = new byte[header.getLength()];
        servConn.read(buffer);
        Assert.assertArrayEquals(msg.toByteArray(), buffer);
        servConn.close();
    }*/
}
