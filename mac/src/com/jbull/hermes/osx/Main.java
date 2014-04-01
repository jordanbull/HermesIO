package com.jbull.hermes.osx;

import com.aquafx_project.AquaFx;
import com.jbull.hermes.MessageListener;
import com.jbull.hermes.MessageSender;
import com.jbull.hermes.TCPServer;
import com.jbull.hermes.desktop.ListenFavoredCommunicationScheduler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        AquaFx.style();

        CommunicationCenter communicationCenter = new CommunicationCenter();

        //new Notification(new Contact("number", "name"), "text content").show();

        int numRetries = 0;
        TCPServer server = new TCPServer(8888);
        InstructionHandler handler = new InstructionHandler(communicationCenter);
        MessageListener listener = new MessageListener(server, handler, numRetries);
        MessageSender sender = new MessageSender(server, numRetries);
        final ListenFavoredCommunicationScheduler commScheduler = new ListenFavoredCommunicationScheduler(sender, listener);
        Thread commThread = new Thread(new Runnable() {
            @Override
            public void run() {
                commScheduler.start();
            }
        });
        commThread.start();



        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(communicationCenter, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
