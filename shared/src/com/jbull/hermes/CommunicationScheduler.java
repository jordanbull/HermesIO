package com.jbull.hermes;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jordan on 3/30/14.
 */
public abstract class CommunicationScheduler<T> {
    protected final Sender<T> sender;
    private final Listener listener;
    Queue<T> queue = new LinkedBlockingQueue<T>();
    protected Mode mode;
    protected boolean running = false;

    public CommunicationScheduler(Sender<T> sender, Listener listener) {
        this.sender = sender;
        this.listener = listener;
        mode = Mode.STOPPED;
    }

    /* SENDING CODE */
    public abstract void startSending() throws IOException;

    public void send(T msg) throws IOException {
        queue.add(msg);
        if (isSending()) {
            flush();
        }
    }

    synchronized public void flush() throws IOException {
        while (!queue.isEmpty() && isSending()) {
            sender.send(queue.remove());
        }
    }

    /* LISTENING CODE */
    public abstract void startListening() throws IOException;

    synchronized public void listenLoop() throws IOException {
        while (isListening()) {
            mode = listener.listen();
        }
    }

    public void stop() {
        running = false;
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
