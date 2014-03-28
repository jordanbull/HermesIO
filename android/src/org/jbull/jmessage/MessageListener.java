package org.jbull.jmessage;

import com.google.protobuf.GeneratedMessage;

import java.io.IOException;

/**
 * Created by jordan on 3/25/14.
 */
public class MessageListener implements CommunicationManager.Listener {
    private final String host;
    private final int port;
    private final MessageReactor messageReactor;
    private long lastUpdate = 0;

    public MessageListener(String host, int port, MessageReactor handler) {
        this.host = host;
        this.port = port;
        this.messageReactor = handler;
    }

    @Override
    public CommunicationManager.Mode listen() throws IOException {
        /*TCPConnection conn = new TCPConnection(host, port);
        byte[] headerBytes = new byte[MessageHelper.HEADER_LENGTH]; //hopefully headers are never longer than this
        conn.read(headerBytes);
        Message.Header header = Message.Header.parseFrom(headerBytes);
        conn.write(header.toByteArray());
        conn.close();
        byte[] buffer = new byte[header.getLength()];
        conn.reconnect();
        conn.read(buffer);
        conn.close();
        Message.Header.Type type = header.getType();
        GeneratedMessage msg = MessageHelper.constructFromBytes(type, buffer);
        if (messageReactor.executeMessage(type, msg)) {
            return CommunicationManager.Mode.LISTENING;
        } else {
            return CommunicationManager.Mode.SENDING;
        }*/
        return CommunicationManager.Mode.STOPPED;
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
