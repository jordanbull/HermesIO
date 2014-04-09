package com.jbull.hermes.android;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.jbull.hermes.*;

public class HermesService extends Service {
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private String ip;
    private final int PORT = 8888;
    private final int SEND_PERIOD = 2000;
    private final int numRetries = 0;
    private final int TIMEOUT_MILLIS = 5000;
    private Intent intent;
    private SendFavoredCommunicationScheduler commManager;

    private final IBinder mBinder = new LocalBinder();
    private boolean connected = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.w("jMessage", "Starting Service");
        int i = super.onStartCommand(intent, flags, startId);
        this.intent = intent;
        try {
            ip = intent.getStringExtra("ip");
        } catch (Exception e) {

        }
        Connection connection = new TCPClient(ip, PORT, TIMEOUT_MILLIS);
        InstructionHandler handler = new InstructionHandler(this);
        MessageListener listener = new MessageListener(connection, handler, numRetries);
        MessageSender sender = new MessageSender(connection, numRetries);
        commManager = new SendFavoredCommunicationScheduler(sender, listener, new Runnable() {public void run() {disconnect();}}, SEND_PERIOD);
        handler.setCommunicationScheduler(commManager);
        Intent connectedIntent = new Intent("com.jbull.hermes");
        connectedIntent.putExtra("connected", true);
        connected = true;
        getApplicationContext().sendBroadcast(connectedIntent);
        Log.w("jMessage", "sending setup");
        new Thread(new Runnable() {
            @Override
            public void run() {
                commManager.send(MessageHelper.createSetupMessage());
                commManager.start();
            }
        }).start();
        Log.w("jMessage", "setup sent");
        smsBroadcastReceiver = new SmsBroadcastReceiver(commManager);
        registerReceiver(smsBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        return i;
    }

    public void disconnect() {
        connected = false;
        Log.w("HermesIO", "Disconnected");
        commManager.stop();
        Intent connectedIntent = new Intent("com.jbull.hermes");
        connectedIntent.putExtra("connected", false);
        getApplicationContext().sendBroadcast(connectedIntent);
    }

    public boolean isConnected() {
        return connected;
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        HermesService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HermesService.this;
        }
    }
}
