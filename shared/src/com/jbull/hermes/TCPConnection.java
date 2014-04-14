package com.jbull.hermes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import static com.jbull.hermes.Message.Ack;

public abstract class TCPConnection extends Connection {

    protected abstract Socket openSocket() throws IOException;

    public abstract void setTimeout(int timeoutMillis) throws SocketException;

    public synchronized SendResponse send(byte[] data, int msgNum, int numRetries) {
        try {
            /* write message */
            Socket s = openSocket();
            OutputStream os = s.getOutputStream();
            os.write(data);
            os.flush();
            s.close();

            /* read ack */
            s = openSocket();
            InputStream is = s.getInputStream();
            Ack ack = Ack.parseFrom(is);
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
            if (numRetries > 0) {
                Logger.log("retry: "+e.getMessage());
                numRetries--;
                return send(data, msgNum, numRetries).incrementRetries().addException(e);
            } else {
                return new SendResponse(false, 0, msgNum).addException(e);
            }
        }
        return new SendResponse(false, 0, msgNum);
    }

    public synchronized ReceiveResponse receive(int numBytes, MsgNumParser msgNumParser, int numRetries) {
        try {
            /* read message */
            byte[] data = new byte[numBytes];
            Socket s = openSocket();
            int num_read = 0;
            while (num_read < numBytes)
                num_read += s.getInputStream().read(data, num_read, numBytes-num_read);
            s.close();
            /* parse message number */
            int msgNum = msgNumParser.parseMsgNum(data);
            /* create and send ack */
            byte[] ackBytes = MessageHelper.createAck(msgNum).toByteArray();
            s = openSocket();
            OutputStream os = s.getOutputStream();
            os.write(ackBytes);
            os.flush();
            s.close();
            return new ReceiveResponse(true, 0, msgNum, data);
        } catch (IOException e) {
            if (numRetries > 0) {
                Logger.log("retry: "+e.getMessage());
                return (ReceiveResponse) receive(numBytes, msgNumParser, numRetries-1).incrementRetries().addException(e);
            } else {
                return (ReceiveResponse) new ReceiveResponse(false, 0, -1, null).addException(e);
            }
        }
    }
}
