package com.jbull.hermes.android;

import android.util.Log;
import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;

import java.io.IOException;


public class SendFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {
    private final Runnable startListenTimer;
    private int sendWindowMillis;

    public SendFavoredCommunicationScheduler(Sender<GeneratedMessage> sender, Listener listener, Runnable startListenTimer, Runnable disconnectCalback, int sendWindowMillis) {
        super(sender, listener, disconnectCalback);
        this.sendWindowMillis = sendWindowMillis;
        this.startListenTimer = startListenTimer;
    }

    public void start() {
        running = true;
        mode = Mode.SENDING;
        if (isSending()) {
            startSending();
        }
    }

    @Override
    synchronized public void startSending() {
        flush();
        if (running && !isStopped() && sendWindowMillis > -1) {
            startListenTimer.run();
        }
    }

    @Override
    synchronized public void startListening() {
        try {
            mode = Mode.LISTENING;
            sender.send(MessageHelper.createModeMessage(true, 0));
            listenLoop();
            if (running && !isStopped())
                startSending();
        } catch (IOException e) {
            Log.e("HermesIO", "", e);
            disconnect();
        }
    }


}
