package com.jbull.hermes.desktop;

import com.jbull.hermes.*;
import com.jbull.hermes.messages.HermesMessage;
import com.jbull.hermes.messages.ModeMessage;
import com.jbull.hermes.messages.Packet;

import java.io.IOException;


public class ListenFavoredCommunicationScheduler extends CommunicationScheduler<HermesMessage, Packet> {

    public ListenFavoredCommunicationScheduler(Sender<Packet> sender, Listener listener, Runnable disconnectCallback) {
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
        queue.add(new ModeMessage(System.currentTimeMillis(), System.currentTimeMillis(), false));
        flush();
        mode = Mode.LISTENING;
    }

    @Override
    synchronized public void flush() {
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
    public void startListening() {
        listenLoop();
    }
}
