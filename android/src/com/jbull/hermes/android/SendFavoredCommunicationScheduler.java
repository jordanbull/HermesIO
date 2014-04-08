package com.jbull.hermes.android;

import android.util.Log;
import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;

import java.io.IOException;


public class SendFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {
    private int sendWindowMillis;

    public SendFavoredCommunicationScheduler(Sender<GeneratedMessage> sender, Listener listener, Runnable disconnectCalback, int sendWindowMillis) {
        super(sender, listener, disconnectCalback);
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
        try {
            mode = Mode.LISTENING;
            sender.send(MessageHelper.createModeMessage(true, 0));
            listenLoop();
        } catch (IOException e) {
            Log.e("HermesIO", "", e);
            disconnect();
        }
    }
}
