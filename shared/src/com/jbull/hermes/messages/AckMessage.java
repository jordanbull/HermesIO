package com.jbull.hermes.messages;

import com.google.protobuf.InvalidProtocolBufferException;

public class AckMessage {
    int ackNum;
    public static final int LENGTH = new AckMessage(1).toBytes().length;

    public AckMessage(int ackNum) {
        this.ackNum = ackNum;
    }

    public static AckMessage fromBytes(byte[] byteRep) throws MessageSerializationException {
        try {
            return new AckMessage(ProtobufRep.Ack.parseFrom(byteRep).getMsgNum());
        } catch (InvalidProtocolBufferException e) {
            throw new MessageSerializationException("Error parsing Ack.", e);
        }
    }

    public byte[] toBytes() {
        return ProtobufRep.Ack.newBuilder()
                .setMsgNum(ackNum)
                .build()
                .toByteArray();
    }

    public int getAckNum() {
        return ackNum;
    }
}
