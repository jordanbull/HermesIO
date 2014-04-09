package com.jbull.hermes.android;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.jbull.jmessage.R;

public class HermesActivity extends Activity {
    private final String CONNECTED = "Connected";
    private final String DISCONNECTED = "Disconnected";
    HermesService mService;
    boolean mBound = false;
    TextView connectionStatusLabel;
    Button connectButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        connectionStatusLabel = (TextView) findViewById(R.id.connectionStatusLabel);
        connectButton = (Button) findViewById(R.id.connectButton);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(onBroadcast, new IntentFilter("com.jbull.hermes"));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
    }

    public void connectAndSetup(View view) {
        final Intent intent = new Intent(this, HermesService.class);
        //final String ip = ((EditText) findViewById(R.id.ipConnectField)).getText().toString();
        final String ip = "192.168.1.129";
        intent.putExtra("ip", ip);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                    startService(intent);
            }
        }).start();
    }

    public void setConnected(boolean connected) {
        if (connected) {
            connectButton.setEnabled(false);
            connectionStatusLabel.setText(CONNECTED);
        } else {
            connectButton.setEnabled(true);
            connectionStatusLabel.setText(DISCONNECTED);
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            HermesService.LocalBinder binder = (HermesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent i) {
            // do stuff to the UI
            Log.w("Hermes", i.getAction());
            setConnected(i.getBooleanExtra("connected", false));
        }
    };
}
