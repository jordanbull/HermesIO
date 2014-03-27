package org.jbull.jmessage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import com.google.protobuf.GeneratedMessage;

/**
 * The InstructionHandler is given messages sent from the server and handles them on the client side
 */
public class InstructionHandler implements MessageListener.MessageReactor {

    private final Context context;

    public InstructionHandler(Context context) {
        this.context = context;
    }

    @Override
    public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg) {
        if (type == Message.Header.Type.SMSMESSAGE) {
            Message.SmsMessage sms = (Message.SmsMessage) msg;
            sendSms(sms);
            return true;
        } else if (type == Message.Header.Type.MODE) {
            Message.Mode mode = (Message.Mode) msg;
            assert mode.getServerSend() == false;
            return false;
        }
        // TODO: should not reach this point. exception?
        assert false;
        return false;
    }

    public void sendSms(Message.SmsMessage sms) {
        SmsManager smsManager = SmsManager.getDefault();
        for (Message.Contact recipent : sms.getRecipentsList()) {
            smsManager.sendTextMessage(recipent.getPhoneNumber(), null, sms.getContent(), null, null);
            ContentValues values = new ContentValues();
            values.put("address", recipent.getPhoneNumber());
            values.put("body", sms.getContent());
            context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        }
    }
}
