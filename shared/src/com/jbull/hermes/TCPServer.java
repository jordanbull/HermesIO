package com.jbull.hermes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 3/29/14.
 */
public class TCPServer extends TCPConnection {

    private final ServerSocket server;
    private int port;

    public TCPServer(int port) throws IOException {
        this.port = port;
        server = new ServerSocket(port);
    }

    @Override
    protected Socket openSocket() throws IOException {
        return server.accept();
    }

    public void close() throws IOException {
        server.close();
    }
}
