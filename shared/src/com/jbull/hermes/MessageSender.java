package com.jbull.hermes;

import com.google.protobuf.GeneratedMessage;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageSender implements Sender<GeneratedMessage> {
    private Connection conn;
    private int numRetries = 0;

    public MessageSender(Connection conn, int numRetries) {
        this.conn = conn;
        this.numRetries = numRetries;
    }
    @Override
    synchronized public void send(GeneratedMessage msg) {
        int msgNum = conn.getSendMsgNum();

        Message.Header header = MessageHelper.createHeader(msg, msgNum);
        byte[] msgData = header.toByteArray();
        Connection.SendResponse response = conn.send(msgData, msgNum, numRetries);
        if (!response.isSuccess()) { //send header failed
            //TODO
            for(Exception e : response.getExceptions())
                throw new RuntimeException(e);
        }
        msg = MessageHelper.returnWithMsgNum(header.getType(), msg, msgNum);
        response = conn.send(msg.toByteArray(), msgNum, numRetries);
        if (!response.isSuccess()) { //send msg failed
            // TODO handle non success
        }
    }
}
