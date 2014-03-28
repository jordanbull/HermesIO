package org.jbull.jmessage;

import junit.framework.TestCase;

import org.junit.Assert;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 3/18/14.
 */
public class LocalTCPConnectionTest extends TestCase {
    static final int PORT = 8888;
    static final String HOST = "localhost";
    private TCPConnection client;
    private ServerSocket server;
    private byte[] data1;
    private byte[] data2;

    public void setUp() throws Exception {
        client = new TCPConnection(HOST, PORT);
        server = new ServerSocket(PORT);
        data1 = new byte[]{1,2,3,4,5};
        data2 = new byte[]{4,3,2,1};
    }

    public void tearDown() throws Exception {
        server.close();
    }

    public void testClientSend() throws Exception {
        final TCPConnection.Response[] sendResponses = new TCPConnection.Response[1];
        final int firstAckNum = client.getSendMsgNum();
        // Asserts the TCPConnection sends data as expected, does not retry with 0 retries and returns true when all goes well
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data1, firstAckNum, 0);
            }
        });
        sendThread.start();
        sendToConn(data1, firstAckNum);
        sendThread.join();
        assertTrue(sendResponses[0].isSuccess());
        assertEquals(0, sendResponses[0].getNumRetries());

        // Asserts the TCPConnection sends data as expected, does not retry with 0 retries and returns false with a bad ack
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data2, client.getSendMsgNum(), 0);
            }
        });
        sendThread.start();
        sendToConn(data2, firstAckNum);
        sendThread.join();
        assertFalse(sendResponses[0].isSuccess());
        assertEquals(0, sendResponses[0].getNumRetries());

        // Asserts the TCPConnection will retry up to as many times as necessary
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data2, client.getSendMsgNum(), 3);
            }
        });
        sendThread.start();
        sendToConn(data2, firstAckNum);
        sendToConn(data2, firstAckNum);
        sendToConn(data2, firstAckNum);
        sendToConn(data2, firstAckNum);
        sendThread.join();
        assertFalse(sendResponses[0].isSuccess());
        assertEquals(3, sendResponses[0].getNumRetries());

        // Asserts the TCPConnection will retry only enough to succeed and report success after n attempts
        final int ackNum = client.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data2, ackNum, 3);
            }
        });
        sendThread.start();
        sendToConn(data2, firstAckNum);
        sendToConn(data2, ackNum);
        sendThread.join();
        assertTrue(sendResponses[0].isSuccess());
        assertEquals(1, sendResponses[0].getNumRetries());
    }

    private void sendToConn(byte[] data, int ackNum) throws IOException {
        byte[] buffer = new byte[data.length];
        Socket s = server.accept();
        s.getInputStream().read(buffer);
        s.close();
        Assert.assertArrayEquals(data, buffer);
        Message.Ack ack = MessageHelper.createAck(ackNum);
        s = server.accept();
        s.getOutputStream().write(ack.toByteArray());
        s.close();
    }

    public void testClientRead() throws Exception {

    }

}
