package com.jbull.hermes.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.jbull.hermes.*;

import java.util.concurrent.atomic.AtomicInteger;

public class HermesService extends Service {
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private final int PORT = 8888;
    private final int SEND_PERIOD = 2000;
    private final int numRetries = 0;
    private final int TIMEOUT_MILLIS = 5000;
    private SendFavoredCommunicationScheduler commManager;

    private final IBinder mBinder = new LocalBinder();
    private boolean connected = false;

    private final String LISTENING_ACTION = "com.jbull.hermes.android.HermesService:startListening";
    private final AtomicInteger LISTEN_COUNT = new AtomicInteger(0);
    private PendingIntent pendInt;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(onBroadcast, new IntentFilter(LISTENING_ACTION));
        int i = super.onStartCommand(intent, flags, startId);
        return i;
    }

    public void connect(String ip) {
        Connection connection = new TCPClient(ip, PORT, TIMEOUT_MILLIS);
        InstructionHandler handler = new InstructionHandler(this);
        MessageListener listener = new MessageListener(connection, handler, numRetries);
        MessageSender sender = new MessageSender(connection, numRetries);
        commManager = new SendFavoredCommunicationScheduler(sender, listener, new Runnable() {
            @Override
            public void run() {
                sendStartListenTimer();
            }
        }, new Runnable() {public void run() {disconnect();}}, SEND_PERIOD);
        handler.setCommunicationScheduler(commManager);
        Intent connectedIntent = new Intent("com.jbull.hermes");
        connectedIntent.putExtra("connected", true);
        connected = true;
        getApplicationContext().sendBroadcast(connectedIntent);
        new Thread(new Runnable() {
            @Override
            public void run() {
                commManager.send(MessageHelper.createSetupMessage(SEND_PERIOD));
                commManager.start();
            }
        }).start();
        Log.w("Hermes", "setup sent");
        smsBroadcastReceiver = new SmsBroadcastReceiver(commManager);
        registerReceiver(smsBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    public void disconnect() {
        connected = false;
        Log.w("HermesIO", "Disconnected");
        commManager.stop();
        Intent connectedIntent = new Intent("com.jbull.hermes");
        connectedIntent.putExtra("connected", false);
        getApplicationContext().sendBroadcast(connectedIntent);
        unregisterReceiver(smsBroadcastReceiver);
        if (pendInt != null) {
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendInt);
        }
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

    public void sendStartListenTimer() {
        Intent intent = new Intent(LISTENING_ACTION);
        PendingIntent pendInt = PendingIntent.getBroadcast(this, LISTEN_COUNT.incrementAndGet(), intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SEND_PERIOD, pendInt);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SEND_PERIOD, pendInt);
        }
    }

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent i) {
            // do stuff to the UI
            Log.w("Hermes", i.getAction());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (commManager != null && !commManager.isStopped())
                    commManager.startListening();
                }
            }).start();

        }
    };
}
