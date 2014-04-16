package com.jbull.hermes.android;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.telephony.SmsManager;
import com.jbull.hermes.Logger;
import com.jbull.hermes.MessageReactor;
import com.jbull.hermes.messages.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The InstructionHandler is given messages sent from the server and handles them on the client side
 */
public class InstructionHandler extends MessageReactor {

    private final Context context;
    private SendFavoredCommunicationScheduler commScheduler;

    public InstructionHandler(Context context) {
        this.context = context;
    }

    public void executeSms(SmsMessage sms) {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> msgParts = smsManager.divideMessage(sms.getContent());
        smsManager.sendMultipartTextMessage(sms.getRecipient().getPhoneNumber(), null, msgParts, null, null);
        ContentValues values = new ContentValues();
        values.put("address", sms.getRecipient().getPhoneNumber());
        values.put("body", sms.getContent());
        context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
    }

    public void executeSetup(SetupMessage setup) {}

    public void executeContact(ContactMessage contactMessage) {}

    public void executeSyncContacts(SyncContactsMessage syncContactsMessage) {
        sendAllContacts();
    }

    @Override
    protected void executeDisconnect(DisconnectMessage msg) {
        // TODO: requires passing in state that contains all methods
    }

    public boolean executeMode(ModeMessage modeMessage) {
        return modeMessage.isServerSend();
    }

    public void setCommunicationScheduler(SendFavoredCommunicationScheduler scheduler) {
        this.commScheduler = scheduler;
    }

    private void sendAllContacts() {
        //do in background
        new Thread(new Runnable() {
            @Override
            public void run() {
                Packet packet = new Packet();
                try {
                    for (ContactMessage contactMessage : Contacts.getContacts(context)) {
                        packet.addMessage(contactMessage);
                    }
                    commScheduler.send(packet);
                    Logger.log("All contacts sent");
                } catch (IOException e) {
                    Logger.log(e);
                }
            }
        }).start();
    }
}
