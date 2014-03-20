package org.jbull.jmessage;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

public class jMessageActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    }

    public void connectAndSetup(View view) {
        final Intent intent = new Intent(this, jMessageService.class);
        //final String ip = ((EditText) findViewById(R.id.ipConnectField)).getText().toString();
        final String ip = "192.168.1.127";
        intent.putExtra("ip", ip);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.w("jMessage", ip);
                    MessageHandler.sendMessage(ip, 8888, MessageHandler.createSetupMessage());
                    startService(intent);
                } catch (IOException e) {
                    Log.e("jMessage", "Error creating connection", e);
                    //System.exit(-1);
                    //TODO handle more gracefully
                }
            }
        }).start();


    }
}
