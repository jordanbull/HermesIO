package com.jbull.hermes.messages;

import java.io.IOException;

public class MessageSerializationException extends IOException {
    public MessageSerializationException(Throwable t) {
        super(t);
    }

    public MessageSerializationException(String message, Throwable t) {
        super(message, t);
    }

    public MessageSerializationException(String message) {
        super(message);
    }
}
