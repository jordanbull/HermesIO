package com.jbull.hermes.android;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.jbull.hermes.messages.ContactMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class Contacts {
    public static ArrayList<ContactMessage> getContacts(Context context) {
        ArrayList<ContactMessage> contactList = new ArrayList<ContactMessage>();

        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                byte[] imageData = null;
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
                        imageData = buffer.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (pCur.moveToNext()) {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactList.add(new ContactMessage(contactNumber, name, imageData));
                    }
                    pCur.close();
                }
            }
        }
        cursor.close();
        Log.w("Hermes", Integer.toString(contactList.size()));
        return contactList;
    }
}
