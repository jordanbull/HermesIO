package com.jbull.hermes.android;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.jbull.jmessage.R;

public class HermesActivity extends Activity {
    private final String CONNECTED = "Connected";
    private final String DISCONNECTED = "Disconnected";
    HermesService mService;
    boolean mBound = false;
    TextView connectionStatusLabel;
    Button connectButton;
    EditText ipInput;
    private SharedPreferences sharedPref;
    private boolean connected;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        connectionStatusLabel = (TextView) findViewById(R.id.connectionStatusLabel);
        connectButton = (Button) findViewById(R.id.connectButton);
        ipInput = (EditText) findViewById(R.id.ipConnectField);
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getString(R.string.ip), "");
        ipInput.setText(ip);
        final Intent intent = new Intent(this, HermesService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(onBroadcast, new IntentFilter("com.jbull.hermes"));
        // update any connection changes that were missed while paused
        if (mService != null)
            setConnected(mService.isConnected());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
    }

    public void connectAndSetup(View view) {
        if (connected) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.disconnect();
                }
            }).start();
        } else {
            final String ip = (ipInput).getText().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.ip), ip);
            editor.commit();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.connect(ip);
                }
            }).start();
        }
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        if (connected) {
            connectButton.setText(getString(R.string.Disconnect));
            connectionStatusLabel.setText(CONNECTED);
            ipInput.setEnabled(false);
        } else {
            connectButton.setText(getString(R.string.Connect));
            connectionStatusLabel.setText(DISCONNECTED);
            ipInput.setEnabled(true);
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
