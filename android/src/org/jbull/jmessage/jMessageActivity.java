package org.jbull.jmessage;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.google.protobuf.GeneratedMessage;
import junit.framework.TestListener;

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
        final String ip = "192.168.2.25";
        intent.putExtra("ip", ip);
        new Thread(new Runnable() {
            @Override
            public void run() {
                    startService(intent);
            }
        }).start();


    }
}
