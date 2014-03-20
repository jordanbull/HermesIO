package org.jbull.jmessage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 3/18/14.
 */
public class TCPConnection {
    Socket sock;
    //InputStreamReader isr;
    //BufferedReader reader;

    public TCPConnection(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    private TCPConnection(Socket s) throws IOException {
        sock = s;
        //isr = new InputStreamReader(sock.getInputStream());
        //reader = new BufferedReader(isr);
    }

    public void reconnect() throws IOException {
        InetAddress addr = sock.getInetAddress();
        int port = sock.getPort();
        sock.close();
        sock = new Socket(addr, port);
        //isr = new InputStreamReader(sock.getInputStream());
        //reader = new BufferedReader(isr);
    }

    public static TCPServer createTCPServer(int port) throws IOException {
        return new TCPServer(new ServerSocket(port));
    }

    public void write(byte[] bytes) throws IOException {
        sock.getOutputStream().write(bytes);
    }

    public int read(byte[] buffer) throws IOException {
        //char[] charBuff = new char[buffer.length];
        int n = sock.getInputStream().read(buffer, 0, buffer.length);
        //int n = reader.read(charBuff, 0, charBuff.length);
        //System.arraycopy(new String(charBuff).getBytes(isr.getEncoding()), 0, buffer, 0, buffer.length);
        return n;
    }

    public boolean ready() throws IOException {
        //return reader.ready();
        return false;
    }

    public void close() throws IOException {
        sock.close();
    }

    public static class TCPServer {
        ServerSocket serv;
        private TCPServer(ServerSocket server) {
            serv = server;
        }
        public TCPConnection accept() throws IOException {
            return new TCPConnection(serv.accept());
        }
        public void close() throws IOException {
            serv.close();
        }
    }
}
