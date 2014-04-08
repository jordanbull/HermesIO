package com.jbull.hermes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class TCPClient extends TCPConnection {

    private final int timeoutMillis;
    private final InetSocketAddress addr;
    private String host;
    private int port;

    public TCPClient(String host, int port, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
        this.addr = new InetSocketAddress(host, port);
    }

    @Override
    protected Socket openSocket() throws IOException {
        Socket s = new Socket(host, port);
        s.setSoTimeout(timeoutMillis);
        //s.connect(addr, timeoutMillis);
        return s;
    }
}
