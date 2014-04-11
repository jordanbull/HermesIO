package com.jbull.hermes;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.util.ArrayList;

public class MessageListener implements Listener {
    protected Connection conn;
    protected final MessageReactor messageReactor;
    protected int numRetries;
    protected final MessageHelper.MessageNumParser headerParser = new MessageHelper.MessageNumParser(null);

    public MessageListener(Connection conn, MessageReactor handler, int numRetries) {
        this.conn = conn;
        this.messageReactor = handler;
        this.numRetries = numRetries;
    }

    @Override
    public Mode listen() throws IOException {
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

    protected void checkAndHandleErrors(final Connection.ReceiveResponse response, final String noExcptionString) throws IOException {
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
