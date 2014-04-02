package com.jbull.hermes.osx;

import com.aquafx_project.AquaFx;
import com.jbull.hermes.desktop.ListenFavoredCommunicationScheduler;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    protected ListenFavoredCommunicationScheduler commScheduler;

    @Override
    public void start(Stage primaryStage) throws Exception{
        AquaFx.style();
        long start = System.currentTimeMillis();
        final CommunicationCenter communicationCenter = new CommunicationCenter();
        System.out.println(System.currentTimeMillis() - start);
        //new Notification(new ContactView("number", "name"), "text content").show();





        primaryStage.setTitle("HermesIO");
        primaryStage.setScene(new Scene(communicationCenter, 600, 800));
        primaryStage.setMinWidth(communicationCenter.getPrefWidth());
        primaryStage.setMinHeight(communicationCenter.getPrefHeight());
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                communicationCenter.close();
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
