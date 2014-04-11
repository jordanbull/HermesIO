package com.jbull.hermes.desktop;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jbull.hermes.*;

import java.io.IOException;

public class SetupOnlyListener  extends MessageListener {

    private final Connection.MsgNumParser headerParser = new Connection.MsgNumParser() {
        @Override
        public int parseMsgNum(byte[] serializedMsg) throws IOException {
            Message.Header header = Message.Header.parseFrom(serializedMsg);
            if (header.getType() == Message.Header.Type.SETUPMESSAGE) {
                return header.getMsgNum();
            } else {
                System.err.println("Received non setup message");
                throw new IOException("The received header was not a setup message");
            }
        }
    };

    public SetupOnlyListener(Connection conn, MessageReactor handler, int numRetries) {
        super(conn, handler, numRetries);
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
}
