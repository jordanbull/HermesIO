package com.jbull.hermes.android;

import android.util.Log;
import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.*;

import java.io.IOException;


public class SendFavoredCommunicationScheduler extends CommunicationScheduler<GeneratedMessage> {
    private final String startListening = "com.jbull.hermes.android.SendFavoredCommunicationScheduler:startListening";
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
        //while(running && !isStopped()) {
            if (isSending()) {
                startSending();
            }
            //assert !isListening();
        //}
        //stop();
    }

    @Override
    public void startSending() {
        flush();
        if (sendWindowMillis > -1) {
            startListenTimer.run();
        }
    }

    @Override
    public void startListening() {
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
