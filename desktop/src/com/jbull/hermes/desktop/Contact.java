package com.jbull.hermes.desktop;

import java.io.Serializable;

public class Contact implements Serializable {
    private static final long serialVersionUID = 1L;
    private String displayName;
    private byte[] imageData;
    private String phoneNumber;

    public Contact(String phoneNumber, String displayName, byte[] imageData) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.imageData = imageData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean equals(Object obj) {
        Contact contact2 = (Contact) obj;
        return displayName.equals(contact2.displayName) && phoneNumber.equals(contact2.phoneNumber) && java.util.Arrays.equals(imageData, contact2.imageData);
    }
}
