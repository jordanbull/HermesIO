package org.jbull.jmessage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jordan on 3/28/14.
 */
public abstract class Connection {
    private int sendMsgNum = 0;

    public SendResponse send(byte[] data, int numRetries) {
        int msgNum = getSendMsgNum();
        return send(data, msgNum, numRetries);
    }

    public abstract SendResponse send(byte[] data, int msgNum, int numRetries);

    public abstract ReceiveResponse receive(int numBytes, MsgNumParser msgNumParser, int numRetries);

    public synchronized int getSendMsgNum() {
        sendMsgNum++;
        return sendMsgNum;
    }


    public static class SendResponse {
        private boolean success;
        private int numRetries;
        private int msgNum;
        private ArrayList<Exception> exceptions = new ArrayList<Exception>();

        SendResponse(boolean success, int numRetries, int msgNum) {
            this.success = success;
            this.numRetries = numRetries;
            this.msgNum = msgNum;
        }
        protected SendResponse incrementRetries() {
            this.numRetries++;
            return this;
        }
        protected SendResponse addException(Exception e) {
            exceptions.add(0, e);
            return this;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getNumRetries() {
            return numRetries;
        }

        public ArrayList<Exception> getExceptions() {
            return exceptions;
        }

        public int getMsgNum() {
            return msgNum;
        }
    }

    public static class ReceiveResponse extends SendResponse {

        public byte[] getData() {
            return data;
        }

        private final byte[] data;

        ReceiveResponse(boolean success, int numRetries, int msgNum, byte[] data) {
            super(success, numRetries, msgNum);
            this.data = data;
        }
    }

    public interface MsgNumParser {
        public int parseMsgNum(byte[] serializedMsg) throws IOException;
    }
}
