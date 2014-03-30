package com.jbull.hermes;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by jordan on 3/29/14.
 */
public class TCPClient extends TCPConnection {

    private String host;
    private int port;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected Socket openSocket() throws IOException {
        return new Socket(host, port);
    }
}
