package com.jbull.hermes.messages;

import junit.framework.TestCase;
import org.junit.Assert;

public class ContactMessageTest extends TestCase {

    private String phone;
    private String name;
    private byte[] img;
    private ContactMessage contact;

    public void setUp() throws Exception {
        phone = "123456789";
        name = "first last";
        img = new byte[] {1, 2, 3, 4};
        contact = new ContactMessage(phone, name, img);
    }

    public void testContactMessage() throws Exception {
        ProtobufRep.Contact proto = contact.getProtobufRep();
        ContactMessage rebuilt = ContactMessage.createFromProtobufRep(proto, ContactMessage.class);
        assertEquals(phone, rebuilt.getPhoneNumber());
        assertEquals(name, rebuilt.getDisplayName());
        Assert.assertArrayEquals(img, rebuilt.getImageData());
    }
}
