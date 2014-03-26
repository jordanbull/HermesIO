package org.jbull.jmessage;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.google.protobuf.GeneratedMessage;

import java.io.IOException;

/**
 * Created by jordan on 3/13/14.
 */
public class jMessageService extends Service {
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private String ip;
    private final int PORT = 8888;
    private final int SEND_PERIOD = 1000;
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
        MessageListener listener = new MessageListener(ip, PORT, new InstructionHandler());
        MessageSender sender = new MessageSender(ip, 8888);
        CommunicationManager<GeneratedMessage> commManager = new CommunicationManager<GeneratedMessage>(listener, sender, SEND_PERIOD);
        try {
            Log.w("jMessage", "sending setup");
            commManager.switchMode(CommunicationManager.Mode.SENDING);
            commManager.send(MessageHelper.createSetupMessage());
            Log.w("jMessage", "setup sent");
        } catch (IOException e) {
            throw new RuntimeException("Error starting the CommunicationManager", e);
        }
        smsBroadcastReceiver = new SmsBroadcastReceiver(commManager);
        registerReceiver(smsBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        return i;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
