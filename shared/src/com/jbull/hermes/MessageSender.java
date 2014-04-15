package com.jbull.hermes;

import com.google.protobuf.GeneratedMessage;

import java.io.IOException;
import java.util.ArrayList;

public class MessageSender implements Sender<GeneratedMessage> {
    private Connection conn;
    private int numRetries = 0;

    public MessageSender(Connection conn, int numRetries) {
        this.conn = conn;
        this.numRetries = numRetries;
    }
    @Override
    synchronized public void send(GeneratedMessage msg) throws IOException {
        int msgNum = conn.getSendMsgNum();

        Message.Header header = MessageHelper.createHeader(msg, msgNum);
        byte[] msgData = header.toByteArray();
        Connection.SendResponse response = conn.send(msgData, msgNum, numRetries);
        checkAndHandleErrors(response, "Send failed with no exception");
        msg = MessageHelper.returnWithMsgNum(header.getType(), msg, msgNum);
        response = conn.send(msg.toByteArray(), msgNum, numRetries);
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
