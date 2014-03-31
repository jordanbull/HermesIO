package com.jbull.hermes.android;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;


public class SendFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {
    private int sendWindowMillis;

    public SendFavoredCommunicationScheduler(Sender<GeneratedMessage> sender, Listener listener, int sendWindowMillis) {
        super(sender, listener);
        this.sendWindowMillis = sendWindowMillis;
    }

    public void start() {
        running = true;
        mode = Mode.SENDING;
        while(running && !isStopped()) {
            if (isSending()) {
                startSending();
            }
            assert !isListening();
        }
        stop();
    }

    @Override
    public void startSending() {
        flush();
        if (sendWindowMillis > -1) {
            try {
                Thread.sleep(sendWindowMillis);
                startListening();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void startListening() {
        mode = Mode.LISTENING;
        sender.send(MessageHelper.createModeMessage(true, 0));
        listenLoop();
    }
}
