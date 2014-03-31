package com.jbull.hermes.desktop;

import com.google.protobuf.GeneratedMessage;
import com.jbull.hermes.Message;
import com.jbull.hermes.MessageReactor;

/**
 * Created by Jordan on 3/30/14.
 */
public class InstructionHandler implements MessageReactor {
    @Override
    public boolean executeMessage(Message.Header.Type type, GeneratedMessage msg) {
        if (type == Message.Header.Type.MODE) {
            System.out.println("received: Mode");
            Message.Mode mode = (Message.Mode) msg;
            return !mode.getServerSend();
        } else if (type == Message.Header.Type.SMSMESSAGE) {
            System.out.println("received: SMSMessage");
        } else if (type == Message.Header.Type.SETUPMESSAGE) {
            System.out.println("received: SetupMessage");
        }
        return true;
    }
}
