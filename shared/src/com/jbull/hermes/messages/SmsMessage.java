package com.jbull.hermes.messages;

public class SmsMessage extends HermesMessage<ProtobufRep.Sms> {
    private ContactMessage sender;
    private ContactMessage recipient;
    private String content;
    private long timeMillis;

    private SmsMessage() {}

    public SmsMessage(ContactMessage sender, ContactMessage recipient, String content, long timeMillis) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timeMillis = timeMillis;
    }

    @Override
    public ProtobufRep.Sms getProtobufRep() {
        return ProtobufRep.Sms.newBuilder()
                .setContent(content)
                .setSender(sender.getProtobufRep())
                .setRecipient(recipient.getProtobufRep())
                .setTimeStamp(timeMillis)
                .build();
    }

    @Override
    public SmsMessage fromProtobufRep(ProtobufRep.Sms protobufRep) {
        this.sender = (ContactMessage) ContactMessage.createFromProtobufRep(protobufRep.getSender(), ContactMessage.class);
        this.recipient = (ContactMessage) ContactMessage.createFromProtobufRep(protobufRep.getRecipient(), ContactMessage.class);
        this.content = protobufRep.getContent();
        this.timeMillis = protobufRep.getTimeStamp();
        return this;
    }

    public ContactMessage getSender() {
        return sender;
    }

    public ContactMessage getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public long getTimeMillis() {
        return timeMillis;
    }
}
