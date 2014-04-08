package com.jbull.hermes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by jordan on 3/29/14.
 */
public class TCPServer extends TCPConnection {

    private final ServerSocket server;
    private int timeoutMillis;
    private int port;

    public TCPServer(int port, int timeoutMillis) throws IOException {
        this.port = port;
        this.timeoutMillis = timeoutMillis;
        server = new ServerSocket(port);
        server.setSoTimeout(timeoutMillis);
    }

    @Override
    protected Socket openSocket() throws IOException {
        return server.accept();
    }

    @Override
    public void setTimeout(int timeoutMillis) throws SocketException {
        this.timeoutMillis = timeoutMillis;
        server.setSoTimeout(timeoutMillis);
    }

    public void close() throws IOException {
        server.close();
    }
}
