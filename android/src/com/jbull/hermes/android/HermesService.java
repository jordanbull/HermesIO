package com.jbull.hermes.android;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.jbull.hermes.*;

/**
 * Created by jordan on 3/13/14.
 */
public class HermesService extends Service {
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private String ip;
    private final int PORT = 8888;
    private final int SEND_PERIOD = 2000;
    private final int numRetries = 0;
    private Intent intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.w("jMessage", "Starting Service");
        int i = super.onStartCommand(intent, flags, startId);
        this.intent = intent;
        try {
            ip = intent.getStringExtra("ip");
        } catch (Exception e) {

        }
        Connection connection = new TCPClient(ip, PORT);
        InstructionHandler handler = new InstructionHandler(this);
        MessageListener listener = new MessageListener(connection, handler, numRetries);
        MessageSender sender = new MessageSender(connection, numRetries);
        final SendFavoredCommunicationScheduler commManager = new SendFavoredCommunicationScheduler(sender, listener, SEND_PERIOD);
        handler.setCommunicationScheduler(commManager);
        Log.w("jMessage", "sending setup");
        new Thread(new Runnable() {
            @Override
            public void run() {
                commManager.send(MessageHelper.createSetupMessage());
                commManager.start();
            }
        }).start();

        commManager.send(MessageHelper.createSetupMessage());
        Log.w("jMessage", "setup sent");
        smsBroadcastReceiver = new SmsBroadcastReceiver(commManager);
        registerReceiver(smsBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        return i;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
