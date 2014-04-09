package com.jbull.hermes.android;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.telephony.SmsManager;
import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import com.jbull.hermes.MessageReactor;

/**
 * The InstructionHandler is given messages sent from the server and handles them on the client side
 */
public class InstructionHandler implements MessageReactor {

    private final Context context;
    private SendFavoredCommunicationScheduler commScheduler;

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
        } else if (type == Message.Header.Type.SYNCCONTACTS) {
            sendAllContacts();
            return true;
        }
        // TODO: should not reach this point. exception?
        throw new RuntimeException("Should not reach this point");
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

    public void setCommunicationScheduler(SendFavoredCommunicationScheduler scheduler) {
        this.commScheduler = scheduler;
    }

    public void sendAllContacts() {
        commScheduler.send(MessageHelper.createBatchContacts(Contacts.getContacts(context)));

    }
}
