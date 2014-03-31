package com.jbull.hermes.desktop;


import com.jbull.hermes.MessageListener;
import com.jbull.hermes.MessageSender;
import com.jbull.hermes.TCPServer;

public class HermesIO {

    public static void main(String[] args) throws Exception {
        int numRetries = 0;
        TCPServer server = new TCPServer(8888);
        InstructionHandler handler = new InstructionHandler();
        MessageListener listener = new MessageListener(server, handler, numRetries);
        MessageSender sender = new MessageSender(server, numRetries);
        ListenFavoredCommunicationScheduler commScheduler = new ListenFavoredCommunicationScheduler(sender, listener);
        commScheduler.start();
    }
}
