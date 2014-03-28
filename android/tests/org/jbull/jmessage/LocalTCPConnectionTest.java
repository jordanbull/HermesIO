package org.jbull.jmessage;

import junit.framework.TestCase;

import org.junit.Assert;

import java.io.IOException;
import java.io.OutputStream;
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
        final TCPConnection.SendResponse[] sendResponses = new TCPConnection.SendResponse[1];
        final int firstAckNum = client.getSendMsgNum();
        // Asserts the TCPConnection sends data as expected, does not retry with 0 retries and returns true when all goes well
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data1, firstAckNum, 0);
            }
        });
        sendThread.start();
        readFromConn(data1, firstAckNum);
        sendThread.join();
        assertTrue(sendResponses[0].isSuccess());
        assertEquals(0, sendResponses[0].getNumRetries());
        assertTrue(sendResponses[0].getExceptions().isEmpty());
        assertEquals(firstAckNum, sendResponses[0].getMsgNum());

        // Asserts the TCPConnection sends data as expected, does not retry with 0 retries and returns false with a bad ack
        final int secondAckNum = client.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data2, secondAckNum, 0);
            }
        });
        sendThread.start();
        readFromConn(data2, firstAckNum);
        sendThread.join();
        assertFalse(sendResponses[0].isSuccess());
        assertEquals(0, sendResponses[0].getNumRetries());
        assertTrue(sendResponses[0].getExceptions().isEmpty());
        assertEquals(secondAckNum, sendResponses[0].getMsgNum());

        // Asserts the TCPConnection will retry up to as many times as necessary
        final int thirdAckNum = client.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data2, thirdAckNum, 3);
            }
        });
        sendThread.start();
        readFromConn(data2, firstAckNum);
        readFromConn(data2, firstAckNum);
        readFromConn(data2, firstAckNum);
        readFromConn(data2, firstAckNum);
        sendThread.join();
        assertFalse(sendResponses[0].isSuccess());
        assertEquals(3, sendResponses[0].getNumRetries());
        assertTrue(sendResponses[0].getExceptions().isEmpty());
        assertEquals(thirdAckNum, sendResponses[0].getMsgNum());

        // Asserts the TCPConnection will retry only enough to succeed and report success after n attempts
        final int ackNum = client.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = client.send(data2, ackNum, 3);
            }
        });
        sendThread.start();
        readFromConn(data2, firstAckNum);
        readFromConn(data2, ackNum);
        sendThread.join();
        assertTrue(sendResponses[0].isSuccess());
        assertEquals(1, sendResponses[0].getNumRetries());
        assertTrue(sendResponses[0].getExceptions().isEmpty());
        assertEquals(ackNum, sendResponses[0].getMsgNum());

        //TODO: test exception occurs
    }

    private void readFromConn(byte[] data, int ackNum) throws IOException {
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
        final TCPConnection.ReceiveResponse[] receiveResponses = new TCPConnection.ReceiveResponse[1];
        final int msgNum = (int) Math.random() * 9999;
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {

                receiveResponses[0] = client.receive(data1.length, new TCPConnection.MsgNumParser() {
                    @Override
                    public int parseMsgNum(byte[] serializedMsg) {
                        return msgNum;
                    }
                }, 0);
            }
        });
        sendThread.start();
        int receivedMsgNum = sendToConn(data1);
        sendThread.join();
        assertEquals(msgNum, receivedMsgNum);
        assertTrue(receiveResponses[0].isSuccess());
        assertEquals(0, receiveResponses[0].getNumRetries());
        assertTrue(receiveResponses[0].getExceptions().isEmpty());
        Assert.assertArrayEquals(data1, receiveResponses[0].getData());
        assertEquals(msgNum, receiveResponses[0].getMsgNum());

        //TODO: test retry on error
    }

    /*
     returns the msg number received from the ack
     */
    private int sendToConn(byte[] data) throws IOException {
        Socket s = server.accept();
        OutputStream os = s.getOutputStream();
        os.write(data);
        os.flush();
        s.close();
        byte[] buffer = new byte[MessageHelper.ackMessageSize()];
        s = server.accept();
        s.getInputStream().read(buffer);
        s.close();
        return MessageHelper.readNumFromSerializedAck(buffer);

    }

}
