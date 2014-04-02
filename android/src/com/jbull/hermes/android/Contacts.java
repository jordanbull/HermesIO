package com.jbull.hermes.android;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.google.protobuf.ByteString;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Jordan on 4/1/14.
 */
public class Contacts {
    public static ArrayList<Message.Contact> getContacts(Context context) {
        ArrayList<Message.Contact> contactList = new ArrayList<Message.Contact>();


        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                ByteString imageData = null;
                Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), photoUri);
                if (input != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    try {
                        while ((nRead = input.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                        imageData = ByteString.copyFrom(buffer.toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (pCur.moveToNext()) {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactList.add(MessageHelper.createContact(name, contactNumber, imageData));
                        Log.w("Hermes", name);
                    }
                    pCur.close();
                }
            }
        }

        return contactList;
    }
}
