package org.jbull.jmessage;

import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class CommunicationManager<T> {

    private final long sendPeriod;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private Mode mode;
    private final Listener listener;
    private final Sender sender;

    private Queue<T> queue = new LinkedList<T>();


    public enum Mode {
        LISTENING,
        SENDING,
        STOPPED
    }


    /**
     * Instantiates a CommunicationManager that handles the sending and receiving looping as well as
     * switching modes between the two. It delegates the actual sending and listening to Sender and Listener objects
     * @param l a listener that will be called to listen to the connection and handle the received messages
     * @param s a sender that will be called to send to the connection
     * @param sendPeriodMs the time to remain in send mode before polling the server for messages
     */
    public CommunicationManager(Listener l, Sender<T> s, long sendPeriodMs) {
        this.listener = l;
        this.sender = s;
        this.mode = Mode.STOPPED;
        this.sendPeriod = sendPeriodMs;
    }

    public synchronized void listenLoop() throws IOException {
        while (mode == Mode.LISTENING) {
            mode = listener.listen();
        }
    }

    public void send(T msg) throws IOException {
        queue.add(msg);
        if (mode == Mode.SENDING) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        flushMessages();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    public synchronized void flushMessages() throws IOException {
        while (mode == Mode.SENDING && !queue.isEmpty()) {
            sender.send(queue.remove());
        }
    }

    public void startSendTimer(final Semaphore s) throws InterruptedException {
        if (sendPeriod >= 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mode = Mode.LISTENING;
                    s.release();
                }
            }, sendPeriod);
        }
    }

    public void loop(Mode toMode) throws IOException, InterruptedException {
        Semaphore s = new Semaphore(0);
        if (toMode != null)
            mode = toMode;
        while (mode != Mode.STOPPED) {
            if (mode == Mode.SENDING) {
                startSendTimer(s);
                flushMessages();
                s.acquire();
            } else if (mode == Mode.LISTENING) {
                //TODO this is not synchronized

                Message.Mode modeMessage = MessageHelper.createModeMessage(true, System.currentTimeMillis());
                sender.send(modeMessage);
                listenLoop();
            } else {
                assert mode == Mode.STOPPED;
            }
        }
    }

    public static interface Sender<T> {
        public void send(T msg) throws IOException;
    }

    public static interface Listener {
        public Mode listen() throws IOException;
    }
}
