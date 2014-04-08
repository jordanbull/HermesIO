package com.jbull.hermes.desktop;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;

import java.io.IOException;


public class ListenFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {

    public ListenFavoredCommunicationScheduler(Sender<GeneratedMessage> sender, Listener listener, Runnable disconnectCallback) {
        super(sender, listener, disconnectCallback);
    }

    public void start() {
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
    public void startSending() {
        try {
            flush();
            mode = Mode.LISTENING;
            sender.send(MessageHelper.createModeMessage(false, 0));
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }


    @Override
    public void startListening() {
        listenLoop();
    }
}
