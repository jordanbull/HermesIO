package com.jbull.hermes;

import com.google.protobuf.GeneratedMessage;

/**
 * The MessageReactor handles any actions that should be taken by the application that is receiving messages
 */
public interface MessageReactor {
    /**
     * Executes any actions that should be performed as a result of receiving msg from the server
     * @param type the type of message received
     * @param msg the message received from the server
     * @returns true if the communicationManager should continue to listen and false if it should stop listening
     */
    boolean executeMessage(Message.Header.Type type, GeneratedMessage msg);
}
