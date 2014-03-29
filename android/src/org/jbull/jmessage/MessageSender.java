package org.jbull.jmessage;

import com.google.protobuf.GeneratedMessage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageSender implements CommunicationManager.Sender<GeneratedMessage> {
    private Connection conn;
    private int numRetries = 0;

    public MessageSender(Connection conn, int numRetries) {
        this.conn = conn;
        this.numRetries = numRetries;
    }
    @Override
    public void send(GeneratedMessage msg) {
        int msgNum = conn.getSendMsgNum();
        Message.Header header = MessageHelper.createHeader(msg, msgNum);
        byte[] msgData = header.toByteArray();
        Connection.SendResponse response = conn.send(msgData, msgNum, numRetries);
        if (!response.isSuccess()) { //send header failed
            //TODO
            for(Exception e : response.getExceptions())
                throw new RuntimeException(e);
        }

        response = conn.send(msg.toByteArray(), numRetries);
        if (!response.isSuccess()) { //send header failed
            // TODO handle non success

        }
    }
}
