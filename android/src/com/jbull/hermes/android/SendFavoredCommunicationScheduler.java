package com.jbull.hermes.android;

import android.os.Handler;
import com.jbull.hermes.CommunicationScheduler;
import com.jbull.hermes.Listener;
import com.jbull.hermes.Mode;
import com.jbull.hermes.Sender;


public class SendFavoredCommunicationScheduler<T> extends CommunicationScheduler<T> {
    private int sendWindowMillis;

    public SendFavoredCommunicationScheduler(Sender sender, Listener listener, int sendWindowMillis) {
        super(sender, listener);
        this.sendWindowMillis = sendWindowMillis;
    }

    public void start() {
        mode = Mode.SENDING;
        while(!isStopped()) {
            if (isSending()) {
                startSending();
            }
            assert !isListening();
        }
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

    protected void setListenAlarm(int delay) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startListening();
            }
        }, delay);
    }

    @Override
    public void startListening() {
        mode = Mode.LISTENING;
        listenLoop();
    }
}
