package com.jbull.hermes.messages;

import com.google.protobuf.ByteString;

public class ContactMessage extends HermesMessage<ProtobufRep.Contact> {
    private String phoneNumber;
    private String displayName;
    private byte[] imageData;

    private ContactMessage() {}

    public ContactMessage(String phoneNumber, String displayName) {
        this(phoneNumber, displayName, null);
    }

    public ContactMessage(String phoneNumber, String displayName, byte[] imageData) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.imageData = imageData;
    }

    @Override
    public ProtobufRep.Contact getProtobufRep() {
        ProtobufRep.Contact.Builder builder =  ProtobufRep.Contact.newBuilder()
                .setName(displayName)
                .setPhoneNumber(phoneNumber);
        if (imageData != null) {
            builder.setImage(ByteString.copyFrom(imageData));
        }
        return builder.build();
    }

    @Override
    public ContactMessage fromProtobufRep(ProtobufRep.Contact protobufRep) {
        this.phoneNumber = protobufRep.getPhoneNumber();
        this.displayName = protobufRep.getName();
        if (protobufRep.hasImage())
            this.imageData = protobufRep.getImage().toByteArray();
        return this;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDisplayName() {
        return displayName;
    }
}
