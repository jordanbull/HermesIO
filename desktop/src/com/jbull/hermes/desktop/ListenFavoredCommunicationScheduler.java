package com.jbull.hermes.desktop;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;

import java.io.IOException;


public class ListenFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {

    public ListenFavoredCommunicationScheduler(Sender<GeneratedMessage> sender, Listener listener) {
        super(sender, listener);
    }

    public void start() throws IOException {
        running = true;
        mode = Mode.LISTENING;
        while (running && !isStopped()) {
            assert !isSending();
            startListening();
            assert !isListening();
            if (isSending())
                startSending();
        }
        stop();
    }

    @Override
    public void startSending() throws IOException {
        flush();
        mode = Mode.LISTENING;
        sender.send(MessageHelper.createModeMessage(false, 0));
    }


    @Override
    public void startListening() throws IOException {
        listenLoop();
    }
}
