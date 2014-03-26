package org.jbull.jmessage;

import android.util.Log;
import com.google.protobuf.GeneratedMessage;

import java.io.IOException;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageSender implements CommunicationManager.Sender<GeneratedMessage> {
    private final String host;
    private final int port;
    private int msgNum = 0;

    public MessageSender(String host, int port) {
        this.host = host;
        this.port = port;
    }
    @Override
    public void send(GeneratedMessage msg) throws IOException {
        TCPConnection conn = new TCPConnection(host, port);
        Message.Header header = MessageHandler.createHeader(msg, ++msgNum);
        byte[] headerData = header.toByteArray();

        conn.write(headerData);
        byte[] buffer = new byte[headerData.length];
        conn.read(buffer);
        // TODO make sure right amount of bytes are read or timeout and handle
        if (byteEquivalent(headerData, buffer)) {
            conn.reconnect();
            conn.write(msg.toByteArray());
        } else {
            throw new RuntimeException("Error reading header as a match");
        }
        conn.close();
    }

    public static boolean byteEquivalent(byte[] a, byte[] b) {
        int size;
        if (a.length >= b.length)
            size = b.length;
        else
            size = a.length;
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }
}
