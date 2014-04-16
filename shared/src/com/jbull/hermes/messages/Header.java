package com.jbull.hermes.messages;

import com.google.protobuf.InvalidProtocolBufferException;

public class Header {
    public static final int LENGTH = new Packet().getHeader(1).toBytes().length;

    private final int msgNum;
    private final int length;

    protected Header(int msgNum, int length) {
        this.msgNum = msgNum;
        this.length = length;
    }

    public byte[] toBytes() {
        return ProtobufRep.Header.newBuilder()
                .setMsgNum(msgNum)
                .setLength(length)
                .build()
                .toByteArray();
    }

    public static Header fromBytes(byte[] byteRep) throws MessageSerializationException {
        try {
            ProtobufRep.Header rep = ProtobufRep.Header.parseFrom(byteRep);
            return new Header(rep.getMsgNum(), rep.getLength());
        } catch (InvalidProtocolBufferException e) {
            throw new MessageSerializationException(e);
        }
    }

    public int getMsgNum() {
        return msgNum;
    }

    public int getLength() {
        return length;
    }
}
