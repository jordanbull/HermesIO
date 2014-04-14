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

    private Runnable disconnectCallback;

    public CommunicationScheduler(Sender<T> sender, Listener listener, Runnable disconnectCallback) {
        this.sender = sender;
        this.listener = listener;
        mode = Mode.STOPPED;
        this.disconnectCallback = disconnectCallback;
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
        try {
            while (!queue.isEmpty() && isSending()) {
                sender.send(queue.remove());
            }
        } catch (IOException e) {
            Logger.log(e);
            disconnect();
        }
    }

    /* LISTENING CODE */
    public abstract void startListening();

    synchronized public void listenLoop() {
        try {
            while (isListening()) {
                mode = listener.listen();
            }
        } catch (IOException e) {
            Logger.log(e);
            disconnect();
        }
    }

    public void stop() {
        running = false;
        mode = Mode.STOPPED;
    }

    public void disconnect() {
        disconnectCallback.run();
    }

    public boolean isSending() {
        return mode == Mode.SENDING;
    }

    public boolean isListening() {
        return mode == Mode.LISTENING;
    }

    public boolean isStopped() { return mode == Mode.STOPPED; }
}
