package com.jbull.hermes;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the interoperability of TCPServer with TCPClient
 */
public class TCPConnectionTest extends TestCase {

    private static final int PORT = 8888;
    private static final String HOST = "localhost";
    private static final int DATA_LENGTH = 20000;
    private static final int NUM_RETRIES = 0;
    private TCPServer server;
    private TCPClient client;
    private byte[] largeData;

    public void setUp() throws Exception {
        server = new TCPServer(PORT);
        client = new TCPClient(HOST, PORT);
        largeData = new byte[DATA_LENGTH];
        new Random().nextBytes(largeData);
    }

    public void tearDown() throws Exception {
        server.close();
    }

    public void testEcho() throws Exception {
        final int sendMsgNum = new Random().nextInt(99);
        final int recMsgNum = new Random().nextInt(99);
        final Connection.MsgNumParser parser = mock(Connection.MsgNumParser.class);
        when(parser.parseMsgNum(largeData)).thenReturn(sendMsgNum, recMsgNum);
        final Connection.SendResponse[] sendResp = new Connection.SendResponse[1];
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResp[0] = client.send(largeData, sendMsgNum, NUM_RETRIES);
            }
        });
        clientThread.start();
        Connection.ReceiveResponse recResp = server.receive(DATA_LENGTH, parser, NUM_RETRIES);
        clientThread.join();
        assertTrue(recResp.isSuccess());
        assertTrue(sendResp[0].isSuccess());
        assertEquals(sendMsgNum, recResp.getMsgNum());
        assertEquals(sendMsgNum, sendResp[0].getMsgNum());
        Assert.assertArrayEquals(largeData, recResp.getData());

        final Connection.ReceiveResponse finalRecResp = recResp;
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendResp[0] = server.send(finalRecResp.getData(), recMsgNum, NUM_RETRIES);
            }
        });
        serverThread.start();
        recResp = client.receive(DATA_LENGTH, parser, NUM_RETRIES);
        serverThread.join();
        assertTrue(recResp.isSuccess());
        assertTrue(sendResp[0].isSuccess());
        assertEquals(recMsgNum, recResp.getMsgNum());
        assertEquals(recMsgNum, sendResp[0].getMsgNum());
        Assert.assertArrayEquals(largeData, recResp.getData());
    }
}
