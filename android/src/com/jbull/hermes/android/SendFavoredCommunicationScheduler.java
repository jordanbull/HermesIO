package com.jbull.hermes.android;

import com.jbull.hermes.*;
import com.jbull.hermes.messages.HermesMessage;
import com.jbull.hermes.messages.ModeMessage;
import com.jbull.hermes.messages.Packet;

import java.io.IOException;


public class SendFavoredCommunicationScheduler extends CommunicationScheduler<HermesMessage, Packet> {
    private final Runnable startListenTimer;
    private int sendWindowMillis;

    public SendFavoredCommunicationScheduler(Sender<Packet> sender, Listener listener, Runnable startListenTimer, Runnable disconnectCalback, int sendWindowMillis) {
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
    public void flush() {
        Packet packet = new Packet();
        try {
            while (!queue.isEmpty() && isSending()) {
                packet.addMessage(queue.remove());
            }
            sender.send(packet);
        } catch (IOException e) {
            Logger.log(e);
            disconnect();
        }
    }

    @Override
    synchronized public void startListening() {
        send(new ModeMessage(System.currentTimeMillis(), System.currentTimeMillis(), true));
        mode = Mode.LISTENING;
        listenLoop();
        if (running && !isStopped())
            startSending();
    }


}
