package com.jbull.hermes;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

import static org.mockito.Mockito.*;

/**
 * Created by jordan on 3/18/14.
 */
public class TCPServerTest extends TestCase {
    static final int PORT = 8888;
    static final String HOST = "localhost";
    private static final int LONG_TIMEOUT = 9000000;
    private static final int SHORT_TIMEOUT = 1;
    private TCPServer server;
    private byte[] data1;
    private byte[] data2;

    public void setUp() throws Exception {
        server = new TCPServer(PORT, LONG_TIMEOUT);
        data1 = new byte[]{1,2,3,4,5};
        data2 = new byte[]{4,3,2,1};
    }

    public void tearDown() throws Exception {
        server.close();
    }

    public void testSend() throws Exception {
        final TCPConnection.SendResponse[] sendResponses = new TCPConnection.SendResponse[1];
        final int firstAckNum = server.getSendMsgNum();
        // Asserts the TCPConnection sends data as expected, does not retry with 0 retries and returns true when all goes well
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = server.send(data1, firstAckNum, 0);
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
        final int secondAckNum = server.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = server.send(data2, secondAckNum, 0);
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
        final int thirdAckNum = server.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = server.send(data2, thirdAckNum, 3);
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
        final int ackNum = server.getSendMsgNum();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResponses[0] = server.send(data2, ackNum, 3);
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
        //can't seem to test this
    }

    private void readFromConn(byte[] data, int ackNum) throws IOException {
        byte[] buffer = new byte[data.length];
        Socket s = new Socket(HOST, PORT);
        s.getInputStream().read(buffer);
        s.close();
        Assert.assertArrayEquals(data, buffer);
        Message.Ack ack = MessageHelper.createAck(ackNum);
        s = new Socket(HOST, PORT);
        s.getOutputStream().write(ack.toByteArray());
        s.close();
    }

    public void testRead() throws Exception {
        final TCPConnection.ReceiveResponse[] receiveResponses = new TCPConnection.ReceiveResponse[1];
        final int msgNum = (int) Math.random() * 9999;
        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                receiveResponses[0] = server.receive(data1.length, new TCPConnection.MsgNumParser() {
                    @Override
                    public int parseMsgNum(byte[] serializedMsg) {
                        return msgNum;
                    }
                }, 0);
            }
        });
        receiveThread.start();
        int receivedMsgNum = sendToConn(data1, false);
        receiveThread.join();
        assertEquals(msgNum, receivedMsgNum);
        assertTrue(receiveResponses[0].isSuccess());
        assertEquals(0, receiveResponses[0].getNumRetries());
        assertTrue(receiveResponses[0].getExceptions().isEmpty());
        Assert.assertArrayEquals(data1, receiveResponses[0].getData());
        assertEquals(msgNum, receiveResponses[0].getMsgNum());

        // Handles exceptions as expected
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                receiveResponses[0] = server.receive(data1.length, new TCPConnection.MsgNumParser() {
                    @Override
                    public int parseMsgNum(byte[] serializedMsg) throws IOException {
                        throw new IOException();
                    }
                }, 0);
            }
        });
        receiveThread.start();
        try {
            sendToConn(data1, true);
        } catch (IOException e) {
            //intentionally thrown
        }
        receiveThread.join();
        assertFalse(receiveResponses[0].isSuccess());
        assertEquals(0, receiveResponses[0].getNumRetries());
        assertTrue(receiveResponses[0].getExceptions().get(0) instanceof IOException);
        Assert.assertArrayEquals(null, receiveResponses[0].getData());
        assertEquals(-1, receiveResponses[0].getMsgNum());

        // test retries until success and only until success
        final TCPConnection.MsgNumParser mockParser = mock(TCPConnection.MsgNumParser.class);
        when(mockParser.parseMsgNum(data1)).thenThrow(new IOException()).thenReturn(msgNum);
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                receiveResponses[0] = server.receive(data1.length, mockParser, 3);
            }
        });
        receiveThread.start();
        try {
            sendToConn(data1, true);
        } catch (IOException e) {
            //intentionally thrown
        }
        sendToConn(data1, false);
        receiveThread.join();
        assertTrue(receiveResponses[0].isSuccess());
        assertEquals(1, receiveResponses[0].getNumRetries());
        assertTrue(receiveResponses[0].getExceptions().get(0) instanceof IOException);
        assertEquals(1, receiveResponses[0].getExceptions().size());
        Assert.assertArrayEquals(data1, receiveResponses[0].getData());
        assertEquals(msgNum, receiveResponses[0].getMsgNum());

        //test retries up to numRetries times
        doThrow(new IOException()).when(mockParser).parseMsgNum(data1);
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                receiveResponses[0] = server.receive(data1.length, mockParser, 3);
            }
        });
        receiveThread.start();
        try {sendToConn(data1, true);} catch (IOException e) {/*intentionally thrown*/}
        try {sendToConn(data1, true);} catch (IOException e) {/*intentionally thrown*/}
        try {sendToConn(data1, true);} catch (IOException e) {/*intentionally thrown*/}
        try {sendToConn(data1, true);} catch (IOException e) {/*intentionally thrown*/}
        receiveThread.join();
        assertFalse(receiveResponses[0].isSuccess());
        assertEquals(3, receiveResponses[0].getNumRetries());
        assertEquals(4, receiveResponses[0].getExceptions().size());
        for (Exception e : receiveResponses[0].getExceptions())
            assertTrue(e instanceof IOException);
        Assert.assertArrayEquals(null, receiveResponses[0].getData());
        assertEquals(-1, receiveResponses[0].getMsgNum());

        //test reads massive data
        final byte[] largeArray = new byte[9999];
        new Random().nextBytes(largeArray);
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                receiveResponses[0] = server.receive(largeArray.length, new TCPConnection.MsgNumParser() {
                    @Override
                    public int parseMsgNum(byte[] serializedMsg) {
                        return msgNum;
                    }
                }, 0);
            }
        });
        receiveThread.start();
        receivedMsgNum = sendToConn(largeArray, false);
        receiveThread.join();
        assertEquals(msgNum, receivedMsgNum);
        assertTrue(receiveResponses[0].isSuccess());
        assertEquals(0, receiveResponses[0].getNumRetries());
        assertTrue(receiveResponses[0].getExceptions().isEmpty());
        Assert.assertArrayEquals(largeArray, receiveResponses[0].getData());
        assertEquals(msgNum, receiveResponses[0].getMsgNum());
    }

    public void testTimeout() throws Exception {
        server.close();
        TCPServer s = new TCPServer(PORT, SHORT_TIMEOUT);
        Connection.ReceiveResponse resp = s.receive(1000, mock(Connection.MsgNumParser.class), 0);
        assertTrue(resp.getExceptions().get(0) instanceof IOException);

        Connection.SendResponse sendResp = s.send(data1, 0, 0);
        assertTrue(sendResp.getExceptions().get(0) instanceof IOException);
        s.close();
    }

    /*
     returns the msg number received from the ack
     */
    private int sendToConn(byte[] data, boolean crash) throws IOException {
        Socket s = new Socket(HOST, PORT);
        OutputStream os = s.getOutputStream();
        os.write(data);
        os.flush();
        s.close();
        if (crash)
            throw new IOException();
        byte[] buffer = new byte[MessageHelper.ackMessageSize()];
        s = new Socket(HOST, PORT);
        s.getInputStream().read(buffer);
        s.close();
        return MessageHelper.readNumFromSerializedAck(buffer);
    }

}
