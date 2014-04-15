package com.jbull.hermes;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jbull.hermes.messages.Header;
import com.jbull.hermes.messages.Packet;

import java.io.IOException;
import java.util.ArrayList;

public class MessageListener implements Listener {
    protected Connection conn;
    protected final MessageReactor messageReactor;
    protected int numRetries;
    protected final Connection.MsgNumParser headerParser = new Connection.MsgNumParser() {
        @Override
        public int parseMsgNum(byte[] serializedMsg) throws IOException {
            return Header.fromBytes(serializedMsg).getMsgNum();
        }
    };

    protected final Connection.MsgNumParser packetParser = new Connection.MsgNumParser() {
        @Override
        public int parseMsgNum(byte[] serializedMsg) throws IOException {
            return Packet.fromBytes(serializedMsg).getMsgNum();

            // TODO: cache these values
        }
    };

    public MessageListener(Connection conn, MessageReactor handler, int numRetries) {
        this.conn = conn;
        this.messageReactor = handler;
        this.numRetries = numRetries;
    }

    @Override
    public Mode listen() throws IOException {
        Connection.ReceiveResponse response = conn.receive(Header.LENGTH, headerParser, numRetries);
        checkAndHandleErrors(response, "Error receiving header. No exception thrown");
        try {
            Header header = Header.fromBytes(response.getData());
            response = conn.receive(header.getLength(), packetParser, numRetries);
            checkAndHandleErrors(response, "Error receiving message. No exception thrown");
            if (messageReactor.executeMessage(Packet.fromBytes(response.getData())))
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
