package com.jbull.hermes.osx;

import com.aquafx_project.AquaFx;
import com.jbull.hermes.desktop.ListenFavoredCommunicationScheduler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    protected ListenFavoredCommunicationScheduler commScheduler;

    @Override
    public void start(Stage primaryStage) throws Exception{
        AquaFx.style();

        CommunicationCenter communicationCenter = new CommunicationCenter();

        //new Notification(new Contact("number", "name"), "text content").show();





        primaryStage.setTitle("HermesIO");
        primaryStage.setScene(new Scene(communicationCenter, 600, 800));
        primaryStage.setMinWidth(communicationCenter.getPrefWidth());
        primaryStage.setMinHeight(communicationCenter.getPrefHeight());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
