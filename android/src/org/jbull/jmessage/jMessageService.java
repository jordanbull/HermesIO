package org.jbull.jmessage;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by jordan on 3/13/14.
 */
public class jMessageService extends Service {
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private String ip;
    private Intent intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = super.onStartCommand(intent, flags, startId);
        this.intent = intent;
        try {
            ip = intent.getStringExtra("ip");
        } catch (Exception e) {

        }
        smsBroadcastReceiver = new SmsBroadcastReceiver(new SmsForwarder(ip, 8888));
        registerReceiver(smsBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        return i;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
