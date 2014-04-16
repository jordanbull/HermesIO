package com.jbull.hermes.android;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import com.jbull.hermes.Logger;
import com.jbull.hermes.messages.ContactMessage;
import com.jbull.hermes.messages.HermesMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SmsBroadcastReceiver extends BroadcastReceiver{

    private final SmsManager SMS = SmsManager.getDefault();
    private SendFavoredCommunicationScheduler commScheduler;
    private Context context;

    private ContactMessage me = new ContactMessage("number", "name");

    public SmsBroadcastReceiver(SendFavoredCommunicationScheduler commScheduler) {
        super();
        this.commScheduler = commScheduler;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Object[] pdusObj = (Object[]) bundle.get("pdus");
                        String body = "";
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[0]);
                        final ContactMessage sender = getContactByNumber(currentMessage.getOriginatingAddress());
                        final long timeMs = currentMessage.getTimestampMillis();
                        for (int i = 0; i < pdusObj.length; i++) {
                            currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                            body += currentMessage.getDisplayMessageBody();
                        }
                        HermesMessage sms = new com.jbull.hermes.messages.SmsMessage(sender, me, body, timeMs);
                        commScheduler.send(sms);
                        Logger.log("sms sent");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SmsHelper.markSmsRead(context, SmsHelper.getSmsId(context, timeMs, currentMessage.getOriginatingAddress()));
                    }
                }).start();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public ContactMessage getContactByNumber(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(
                uri,
                null,
                null,
                null,
                null
        );
        cur.moveToFirst();
        String contactId = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup._ID));
        String name = cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));

        byte[] imageData = null;
        Uri photoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(contactId));
        InputStream input = Contacts.openContactPhotoInputStream(context.getContentResolver(), photoUri);
        if (input != null) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            try {
                while ((nRead = input.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                imageData = buffer.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cur.close();
        return new ContactMessage(phoneNumber, name, imageData);
    }


}
