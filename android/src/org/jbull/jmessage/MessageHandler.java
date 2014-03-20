package org.jbull.jmessage;

import android.content.Intent;
import android.util.Log;
import com.google.protobuf.GeneratedMessage;

import java.io.IOException;

/**
 * Created by jordan on 3/18/14.
 */
public class MessageHandler {
    private static int msgNum = 0;

    public static void sendMessage(String host, int port, GeneratedMessage msg) throws IOException {
        TCPConnection conn = new TCPConnection(host, port);
        Message.Header.Type type = null;
        if (msg instanceof Message.SetupMessage) {
            type = Message.Header.Type.SETUPMESSAGE;
        } else if (msg instanceof Message.SmsMessage) {
            type = Message.Header.Type.SMSMESSAGE;
        } else if (msg instanceof Message.Contact) {
            type = Message.Header.Type.CONTACT;
        }
        Message.Header header = Message.Header.newBuilder()
                .setMsgNum(++msgNum)
                .setLength(msg.toByteArray().length)
                .setType(type)
                .build();
        byte[] headerData = header.toByteArray();

        conn.write(headerData);
        byte[] buffer = new byte[headerData.length];
        // TODO make sure right amount of bytes are read or timeout and handle
        Log.w("jMessage", "read " + Integer.toString(conn.read(buffer)) + " bytes should be " + Integer.toString(headerData.length));
        if (byteEquivalent(headerData, buffer)) {
            Log.w("jMessage", "sending msg");
            conn.reconnect();
            conn.write(msg.toByteArray());
            Log.w("jMessage", "msg written");
        } else {
            Log.e("jMessage", "error reading ack of header");
        }
        conn.close();
    }

    private static boolean byteEquivalent(byte[] a, byte[] b) {
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

    public static Message.SetupMessage createSetupMessage() {
        return Message.SetupMessage.newBuilder()
                .setVersion(1)
                .setApplicationName("jMessage")
                .build();
    }
}
