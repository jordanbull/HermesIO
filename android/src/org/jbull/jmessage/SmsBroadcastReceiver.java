package org.jbull.jmessage;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.*;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SmsBroadcastReceiver extends BroadcastReceiver{

    private final SmsManager SMS = SmsManager.getDefault();
    private SmsHandler smsHandler;
    private Context context;

    public SmsBroadcastReceiver(SmsHandler smsHandler) {
        super();
        this.smsHandler = smsHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("jMessage", "message received");
        this.context = context;
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    final Message.Contact sender = getContactByNumber(currentMessage.getDisplayOriginatingAddress());
                    final String message = currentMessage.getDisplayMessageBody();
                    final long timeMs = currentMessage.getTimestampMillis();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.w("jMessage", "handling sms");
                                smsHandler.handleSms(sender, message, timeMs);
                                Log.w("jMessage", "sms handled");
                            } catch (IOException e) {
                                Log.e("jMessage", "error handling send of sms to desktop", e);
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            Log.e("jMessage", "Exception smsReceiver" , e);
        }
    }

    public Message.Contact getContactByNumber(String phoneNumber) {
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
        Message.Contact.Builder contactBuilder = Message.Contact.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setName(name);
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
                contactBuilder.setImage(ByteString.copyFrom(buffer.toByteArray()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contactBuilder.build();
    }

    private void smsError(Exception e) {
        //TODO
    }

    public interface SmsHandler {
        public void handleSms(Message.Contact sender, String message, long timeMs) throws IOException;
    }
}
