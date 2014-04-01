package com.jbull.hermes.desktop;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.*;
import java.util.ArrayList;


public class DataStoreTest extends TestCase {

    private DataStore dataStore;
    private byte[] data1;
    private String name1;
    private String number1;
    private byte[] data2;
    private String msg1;
    private String msg3;
    private String msg2;
    private long time1;
    private long time2;
    private long time3;
    private DataStore filledDataStore;
    private String number2;
    private String name2;

    public void setUp() throws Exception {
        dataStore = new DataStore();
        number1 = "1234567890";
        number2 = "9876543210";
        name1 = "First Last";
        name2 = "Hella Yolo";
        data1 = new byte[]{1,2,3,4,5};
        data2 = new byte[]{9,8,7,6};
        msg1 = "message1";
        msg2 = "msg2";
        msg3 = "m3";
        time1 = 1l;
        time2 = 2l;
        time3 = 3l;
        filledDataStore = new DataStore();
        filledDataStore.addContact(number1, name1, data1, false);
        filledDataStore.addContact(number2, name2, data2, false);
        filledDataStore.addMessageToConversation(number1, msg1, time1);
        filledDataStore.addMessageToConversation(number1, msg2, time2);
        filledDataStore.addMessageToConversation(number2, msg3, time3);
    }

    public void testAddAndGetContact() throws Exception {
        dataStore.addContact(number1, name1, data1, false);
        DataStore.ContactData retrievedContact = dataStore.getContact(number1);
        assertEquals(name1, retrievedContact.getDisplayName());
        Assert.assertArrayEquals(data1, retrievedContact.getImageData());
        assertEquals(number1, retrievedContact.getPhoneNumber());

        // does not overwrite when flag is false
        dataStore.addContact(number1, name1, data2, false);
        retrievedContact = dataStore.getContact(number1);
        assertEquals(name1, retrievedContact.getDisplayName());
        Assert.assertArrayEquals(data1, retrievedContact.getImageData());
        assertEquals(number1, retrievedContact.getPhoneNumber());

        // overwrites when flag is true
        dataStore.addContact(number1, name1, data2, true);
        retrievedContact = dataStore.getContact(number1);
        assertEquals(name1, retrievedContact.getDisplayName());
        Assert.assertArrayEquals(data2, retrievedContact.getImageData());
        assertEquals(number1, retrievedContact.getPhoneNumber());
    }

    public void testAddAndGetConversation() throws Exception {
        dataStore.addMessageToConversation(number1, msg1, time1);
        DataStore.ConversationData convo = dataStore.getConversation(number1);
        assertEquals(number1, convo.getPhoneNumber());
        ArrayList<DataStore.MessageData> msgs = convo.getMessages();
        assertEquals(1, msgs.size());
        assertEquals(msg1, msgs.get(0).getContent());
        assertEquals(new Long(time1), msgs.get(0).getTimeMillis());

        //adds multiple
        dataStore.addMessageToConversation(number1, msg3, time3);
        convo = dataStore.getConversation(number1);
        assertEquals(number1, convo.getPhoneNumber());
        msgs = convo.getMessages();
        assertEquals(2, msgs.size());
        assertEquals(msg1, msgs.get(0).getContent());
        assertEquals(new Long(time1), msgs.get(0).getTimeMillis());
        assertEquals(msg3, msgs.get(1).getContent());
        assertEquals(new Long(time3), msgs.get(1).getTimeMillis());

        //orders by timestamp
        dataStore.addMessageToConversation(number1, msg2, time2);
        convo = dataStore.getConversation(number1);
        assertEquals(number1, convo.getPhoneNumber());
        msgs = convo.getMessages();
        assertEquals(3, msgs.size());
        assertEquals(msg1, msgs.get(0).getContent());
        assertEquals(new Long(time1), msgs.get(0).getTimeMillis());
        assertEquals(msg2, msgs.get(1).getContent());
        assertEquals(new Long(time2), msgs.get(1).getTimeMillis());
        assertEquals(msg3, msgs.get(2).getContent());
        assertEquals(new Long(time3), msgs.get(2).getTimeMillis());
    }

    public void testMessageDataEquality() throws Exception {
        DataStore.MessageData m1 = new DataStore.MessageData(msg1, time1);
        DataStore.MessageData m2 = new DataStore.MessageData(msg2, time1);
        DataStore.MessageData m3 = new DataStore.MessageData(msg1, time1);
        DataStore.MessageData m4 = new DataStore.MessageData(msg1, time3);
        assertEquals(m1, m3);
        assertFalse(m1.equals(m2));
        assertFalse(m1.equals(m4));
    }

    public void testConversationDataEquality() throws Exception {
        DataStore.ConversationData c1 = new DataStore.ConversationData(number1);
        DataStore.ConversationData c2 = new DataStore.ConversationData(number1);
        DataStore.ConversationData c3 = new DataStore.ConversationData(number2);
        assertEquals(c1, c2);
        assertFalse(c1.equals(c3));

        c1.addMessage(msg1, time1);
        c2.addMessage(msg2, time2);
        c1.addMessage(msg2, time2);
        c2.addMessage(msg1, time1);
        c3.addMessage(msg1, time1);
        c3.addMessage(msg2, time2);
        assertEquals(c1, c2);
        assertFalse(c1.equals(c3));

        c1.addMessage(msg3, time3);
        assertFalse(c1.equals(c2));
    }

    public void testContactDataEquality() throws Exception {
        DataStore.ContactData c1 = new DataStore.ContactData(number1, name1, data1);
        DataStore.ContactData c2 = new DataStore.ContactData(number1, name1, java.util.Arrays.copyOf(data1, data1.length));
        DataStore.ContactData c3 = new DataStore.ContactData(number2, name1, data1);
        DataStore.ContactData c4 = new DataStore.ContactData(number1, name2, data1);
        DataStore.ContactData c5 = new DataStore.ContactData(number1, name1, data2);
        DataStore.ContactData c6 = new DataStore.ContactData(number1, name1, null);

        assertEquals(c1, c2);
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(c4));
        assertFalse(c1.equals(c5));
        assertFalse(c1.equals(c6));
        assertFalse(c6.equals(c1));  //check no crash for null on either side
    }

    public void testDataStoreEquality() throws Exception {
        DataStore ds2 = new DataStore();
        ds2.addContact(number1, name1, data1, false);
        ds2.addContact(number2, name2, data2, false);
        ds2.addMessageToConversation(number1, msg1, time1);
        ds2.addMessageToConversation(number1, msg2, time2);
        ds2.addMessageToConversation(number2, msg3, time3);

        assertEquals(filledDataStore, ds2);
        assertFalse(filledDataStore.equals(new DataStore()));
        ds2.addContact("a","b",null,true);
        assertFalse(filledDataStore.equals(ds2));
    }

    public void testDataStoreSerialization() {
        try {
            FileOutputStream fos = new FileOutputStream("tempdata.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(filledDataStore);
            oos.close();
        }
        catch (Exception ex) {
            fail("Exception thrown during test: " + ex.toString());
        }

        try {
            FileInputStream fis = new FileInputStream("tempdata.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            DataStore deserDataStore = (DataStore) ois.readObject();
            ois.close();
            System.out.println(filledDataStore.equals(deserDataStore));
            assertEquals(filledDataStore, deserDataStore);
            // Clean up the file
            new File("tempdata.ser").delete();
        }
        catch (Exception ex) {
            fail("Exception thrown during test: " + ex.toString());
        }
    }
}
