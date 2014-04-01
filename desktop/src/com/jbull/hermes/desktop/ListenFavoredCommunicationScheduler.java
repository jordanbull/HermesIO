package com.jbull.hermes.desktop;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;


public class ListenFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {

    public ListenFavoredCommunicationScheduler(Sender<GeneratedMessage> sender, Listener listener) {
        super(sender, listener);
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
        flush();
        mode = Mode.LISTENING;
        sender.send(MessageHelper.createModeMessage(false, 0));
    }


    @Override
    public void startListening() {
        listenLoop();
    }
}
