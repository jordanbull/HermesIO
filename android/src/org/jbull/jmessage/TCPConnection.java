package org.jbull.jmessage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jordan on 3/18/14.
 */
public class TCPConnection {
    String host;
    int port;
    private int sendMsgNum = 0;

    public TCPConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }


    public synchronized SendResponse send(byte[] data, int msgNum, int numRetries) {
        try {
            /* write message */
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();
            os.write(data);
            os.flush();
            s.close();

            /* read ack */
            s = new Socket(host, port);
            InputStream is = s.getInputStream();
            Message.Ack ack = Message.Ack.parseFrom(is);
            s.close();

            /* verify ack */
            if (ack.getMsgNum() == msgNum) {
                return new SendResponse(true, 0, msgNum);
            } else {
                if (numRetries > 0) {
                    numRetries--;
                    return send(data, msgNum, numRetries).incrementRetries();
                }
            }
        } catch (IOException e) {
            // TODO handle retries and such
            e.printStackTrace();
            if (numRetries > 0) {
                numRetries--;
                return send(data, msgNum, numRetries).incrementRetries().addException(e);
            } else {
                return new SendResponse(false, 0, msgNum).addException(e);
            }
        }
        return new SendResponse(false, 0, msgNum);
    }

    public ReceiveResponse receive(int numBytes, MsgNumParser msgNumParser, int numRetries) {
        try {
            byte[] data = new byte[numBytes];
            Socket s = new Socket(host, port);
            s.getInputStream().read(data);
            s.close();
            int msgNum = msgNumParser.parseMsgNum(data);
            byte[] ackBytes = MessageHelper.createAck(msgNum).toByteArray();
            s = new Socket(host, port);
            OutputStream os = s.getOutputStream();
            os.write(ackBytes);
            os.flush();
            s.close();
            return new ReceiveResponse(true, 0, msgNum, data);
        } catch (IOException e) {
            if (numRetries > 0) {
                return (ReceiveResponse) receive(numBytes, msgNumParser, numRetries-1).incrementRetries().addException(e);
            } else {
                return (ReceiveResponse) new ReceiveResponse(false, 0, -1, null).addException(e);
            }
        }
    }

    public synchronized int getSendMsgNum() {
        sendMsgNum++;
        return sendMsgNum;
    }

    public static class SendResponse {
        private boolean success;
        private int numRetries;
        private int msgNum;
        private ArrayList<Exception> exceptions = new ArrayList<Exception>();

        private SendResponse(boolean success, int numRetries, int msgNum) {
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

        private ReceiveResponse(boolean success, int numRetries, int msgNum, byte[] data) {
            super(success, numRetries, msgNum);
            this.data = data;
        }
    }

    public interface MsgNumParser {
        public int parseMsgNum(byte[] serializedMsg);
    }
}
