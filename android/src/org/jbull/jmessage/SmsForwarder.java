package org.jbull.jmessage;

import java.io.IOException;

/**
 * Created by jordan on 3/18/14.
 */
public class SmsForwarder implements SmsBroadcastReceiver.SmsHandler {

    private final String host;
    private final int port;

    public SmsForwarder(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public synchronized void handleSms(Message.Contact sender, String message, long timeMs) throws IOException {
        Message.SmsMessage sms = Message.SmsMessage.newBuilder()
                .setTimeStamp(timeMs)
                .setSender(sender)
                .setContent(message)
                .build();
        sendMessage(sms);
    }

    private void sendMessage(Message.SmsMessage sms) throws IOException {
        //TCPConnection conn = new TCPConnection(host, port);
        MessageHandler.sendMessage(host, port, sms);
        //conn.close();
    }
}
