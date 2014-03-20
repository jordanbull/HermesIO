package org.jbull.jmessage;

import junit.framework.TestCase;

import org.junit.Assert;

/**
 * Created by jordan on 3/18/14.
 */
public class TCPConnectionTest extends TestCase {
    static final int PORT = 8888;
    private TCPConnection client;
    private TCPConnection server;
    private byte[] buffEqualSize;
    private byte[] buffGreaterSize;
    private byte[] buffLessSize;
    private byte[] fitData;
    private byte[] multibufDataP1;
    private byte[] multibufDataP2;
    private byte[] partialFullData;
    private TCPConnection.TCPServer tcpServ;

    public void setUp() throws Exception {
        super.setUp();
        tcpServ = TCPConnection.createTCPServer(PORT);
        client = new TCPConnection("localhost", PORT);
        server = tcpServ.accept();
        buffEqualSize = new byte[5];
        buffGreaterSize = new byte[6];
        buffLessSize = new byte[4];
        fitData = new byte[] {1,2,3,4,5};
        multibufDataP1 = new byte[] {1,2,3,4};
        multibufDataP2 = new byte[4];
        multibufDataP2[0] = 5;
        partialFullData = new byte[6];
        for (byte i = 1; i < 6; i++)
            partialFullData[i-1] = i;
    }

    public void tearDown() throws Exception {
        //TODO
        client.close();
        server.close();
        tcpServ.close();
    }

    public void testClientWriteServerRead() throws Exception {
        client.write(fitData);
        int n = server.read(buffEqualSize);
        assertEquals(fitData.length, n);
        Assert.assertArrayEquals(fitData, buffEqualSize);

        client.write(fitData);
        n = server.read(buffGreaterSize);
        assertEquals(fitData.length, n);
        Assert.assertArrayEquals(partialFullData, buffGreaterSize);

        client.write(fitData);
        n = server.read(buffLessSize);
        assertEquals(buffLessSize.length, n);
        Assert.assertArrayEquals(multibufDataP1, buffLessSize);
        buffLessSize = new byte[4];
        n = server.read(buffLessSize);
        assertEquals(1, n);
        Assert.assertArrayEquals(multibufDataP2, buffLessSize);
    }

    public void testEchoFromClient() throws Exception {
        client.write(fitData);
        server.read(buffEqualSize);
        server.write(buffEqualSize);
        byte[] buff = new byte[5];
        client.read(buff);
        Assert.assertArrayEquals(buff, fitData);
    }

    public void testDoubleEcho() throws Exception {
        client.write(fitData);
        server.read(buffEqualSize);
        server.write(buffEqualSize);
        byte[] buff = new byte[5];
        client.read(buff);
        client.write(buff);
        buff = new byte[5];
        server.read(buff);
        Assert.assertArrayEquals(fitData, buff);
        server.write(buff);
        buff = new byte[5];
        client.read(buff);
        Assert.assertArrayEquals(fitData, buff);
    }
}
