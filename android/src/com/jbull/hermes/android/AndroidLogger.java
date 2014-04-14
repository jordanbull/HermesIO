package com.jbull.hermes.android;

import android.util.Log;
import com.jbull.hermes.Logger;

public class AndroidLogger extends Logger {
    private static final String APP_NAME = "HermesIO";
    @Override
    protected void doLog(String message) {
        Log.w(APP_NAME, message);
    }

    @Override
    protected void doLog(Exception e) {
        Log.e(APP_NAME, "", e);
    }
}
