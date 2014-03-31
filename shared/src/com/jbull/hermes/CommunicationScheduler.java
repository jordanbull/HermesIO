package com.jbull.hermes;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jordan on 3/30/14.
 */
public abstract class CommunicationScheduler<T> {
    private final Sender<T> sender;
    private final Listener listener;
    Queue<T> queue = new LinkedBlockingQueue<T>();
    protected Mode mode;

    public CommunicationScheduler(Sender<T> sender, Listener listener) {
        this.sender = sender;
        this.listener = listener;
        mode = Mode.STOPPED;
    }

    /* SENDING CODE */
    public abstract void startSending();

    public void send(T msg) {
        queue.add(msg);
        if (isSending()) {
            flush();
        }
    }

    synchronized public void flush() {
        while (!queue.isEmpty() && isSending()) {
            sender.send(queue.remove());
        }
    }

    /* LISTENING CODE */
    public abstract void startListening();

    synchronized public void listenLoop() {
        while (isListening()) {
            mode = listener.listen();
        }
    }

    public void stop() {
        mode = Mode.STOPPED;
    }

    public boolean isSending() {
        return mode == Mode.SENDING;
    }

    public boolean isListening() {
        return mode == Mode.LISTENING;
    }

    public boolean isStopped() { return mode == Mode.STOPPED; }
}
