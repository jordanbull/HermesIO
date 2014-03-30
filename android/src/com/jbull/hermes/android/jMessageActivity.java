package com.jbull.hermes.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import org.jbull.jmessage.R;

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
                    startService(intent);
            }
        }).start();


    }
}
