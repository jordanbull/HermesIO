package com.jbull.hermes;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

public class MessageListener implements Listener {
    private Connection conn;
    private final MessageReactor messageReactor;
    private int numRetries;
    private final MessageHelper.MessageNumParser headerParser = new MessageHelper.MessageNumParser(null);

    public MessageListener(Connection conn, MessageReactor handler, int numRetries) {
        this.conn = conn;
        this.messageReactor = handler;
        this.numRetries = numRetries;
    }

    @Override
    public Mode listen() {
        Connection.ReceiveResponse response = conn.receive(MessageHelper.HEADER_LENGTH, headerParser, numRetries);
        checkAndHandleErrors(response, "Error receiving header. No exception thrown");
        try {
            Message.Header header = Message.Header.parseFrom(response.getData());
            response = conn.receive(header.getLength(), new MessageHelper.MessageNumParser(header.getType()), numRetries);
            checkAndHandleErrors(response, "Error receiving message. No exception thrown");
            if (messageReactor.executeMessage(header.getType(), MessageHelper.constructFromBytes(header.getType(), response.getData())))
                return Mode.LISTENING;
            else
                return Mode.SENDING;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkAndHandleErrors(final Connection.ReceiveResponse response, final String noExcptionString) {
        if (!response.isSuccess()) {
            ArrayList<Exception> exceptions = response.getExceptions();
            if (exceptions.size() > 0) {
                throw new RuntimeException(exceptions.get(exceptions.size()-1));
            }
            throw new RuntimeException(noExcptionString);
        }
    }
}
