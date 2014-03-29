package org.jbull.jmessage;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageListener implements CommunicationManager.Listener {
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
    public CommunicationManager.Mode listen() {
        Connection.ReceiveResponse response = conn.receive(MessageHelper.HEADER_LENGTH, headerParser, numRetries);
        checkAndHandleErrors(response, "Error receiving header. No exception thrown");
        try {
            Message.Header header = Message.Header.parseFrom(response.getData());
            response = conn.receive(header.getLength(), new MessageHelper.MessageNumParser(header.getType()), numRetries);
            checkAndHandleErrors(response, "Error receiving message. No exception thrown");
            if (messageReactor.executeMessage(header.getType(), MessageHelper.constructFromBytes(header.getType(), response.getData())))
                return CommunicationManager.Mode.LISTENING;
            else
                return CommunicationManager.Mode.SENDING;
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


    public static interface MessageReactor {
        /**
         * Executes any actions that should be performed as a result of receiving msg from the server
         * @param type the type of message received
         * @param msg the message received from the server
         * @returns true if the communicationManager should continue to listen and false if it should stop listening
         */
        public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg);
    }
}
