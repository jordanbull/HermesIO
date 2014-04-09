package com.jbull.hermes.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class SmsHelper {
    public static int getSmsId(Context context, long timestamp, String address) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                null,
                "address = '" + address + "' AND date = '" + Long.toString(timestamp) + "'",
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = Integer.parseInt(cursor.getString(0));
            return id;
        } else {
            Log.w("Hermes", "could not locate id of message");
            return -1;
        }
    }

    public static void markSmsRead(Context context, int smsId) {
        ContentValues values = new ContentValues();
        values.put("read",true);
        context.getContentResolver().update(Uri.parse("content://sms/"),values, "_id="+smsId, null);
    }
}
