package org.jbull.jmessage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 3/18/14.
 */
public class TCPConnection {
    String host;
    int port;
    private int sendMsgNum = 0;

    public TCPConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }


    public synchronized Response send(byte[] data, int msgNum, int numRetries) {
        try {
            /* write message */
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();
            os.write(data);
            os.flush();
            s.close();

            /* read ack */
            s = new Socket(host, port);
            InputStream is = s.getInputStream();
            Message.Ack ack = Message.Ack.parseFrom(is);
            s.close();

            /* verify ack */
            if (ack.getMsgNum() == msgNum) {
                return new Response(true, 0);
            } else {
                if (numRetries > 0) {
                    numRetries--;
                    return send(data, msgNum, numRetries).incrementRetries();
                }
            }
        } catch (IOException e) {
            // TODO handle retries and such
            e.printStackTrace();
        }
        return new Response(false, 0);
    }

    public synchronized int getSendMsgNum() {
        sendMsgNum++;
        return sendMsgNum;
    }

    public static final class Response {
        private boolean success;
        private int numRetries;

        private Response(boolean success, int numRetries) {
            this.success = success;
            this.numRetries = numRetries;
        }
        private Response incrementRetries() {
            this.numRetries++;
            return this;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getNumRetries() {
            return numRetries;
        }
    }
}
