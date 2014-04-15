package com.jbull.hermes;

import com.jbull.hermes.messages.Packet;

import java.io.IOException;
import java.util.ArrayList;

public class MessageSender implements Sender<Packet> {
    private Connection conn;
    private int numRetries = 0;

    public MessageSender(Connection conn, int numRetries) {
        this.conn = conn;
        this.numRetries = numRetries;
    }
    @Override
    synchronized public void send(Packet packet) throws IOException {
        int msgNum = conn.getSendMsgNum();
        byte[] headerData = packet.getHeader(msgNum).toBytes();
        Connection.SendResponse response = conn.send(headerData, msgNum, numRetries);
        checkAndHandleErrors(response, "Send failed with no exception");

        response = conn.send(packet.getBytes(), msgNum, numRetries);
        checkAndHandleErrors(response, "Send failed with no exception");
    }

    private void checkAndHandleErrors(final Connection.SendResponse response, final String noExcptionString) throws IOException {
        if (!response.isSuccess()) {
            ArrayList<Exception> exceptions = response.getExceptions();
            if (exceptions.size() > 0) {
                Exception e = exceptions.get(exceptions.size()-1);
                if (e instanceof IOException)
                    throw (IOException) e;
                throw new RuntimeException(e);
            }
            throw new RuntimeException(noExcptionString);
        }
    }
}
