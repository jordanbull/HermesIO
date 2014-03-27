package org.jbull.jmessage;

import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class CommunicationManager<T> {

    private final long sendPeriod;

    public Mode getMode() {
        return mode;
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
        Mode curMode = mode;
        while (curMode == Mode.LISTENING) {
            curMode = listener.listen();
        }
    }

    public synchronized void send(T msg) {
        queue.add(msg);
        if (mode == Mode.SENDING) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        flushMessages();
                    } catch (IOException e) {
                        new RuntimeException("Error flushing messages in send", e);
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

    public void startSendTimer() {
        if (sendPeriod >= 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        switchMode(Mode.LISTENING);
                    } catch (IOException e) {
                        // TODO: handle better
                        e.printStackTrace();
                    }
                }
            }, sendPeriod);
        }
    }

    public void switchMode(Mode toMode) throws IOException {
        mode = toMode;
        if (toMode == Mode.SENDING) {
            startSendTimer();
            flushMessages();
        } else if (toMode == Mode.LISTENING) {
            //TODO this is not synchronized

            Message.Mode modeMessage = MessageHelper.createModeMessage(true, System.currentTimeMillis());
            sender.send(modeMessage);
            listenLoop();
        } else {
            assert toMode == Mode.STOPPED;
        }
    }

    public static interface Sender<T> {
        public void send(T msg) throws IOException;
    }

    public static interface Listener {
        public Mode listen() throws IOException;
    }
}
