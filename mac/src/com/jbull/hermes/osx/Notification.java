package com.jbull.hermes.osx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class Notification extends GridPane {
    private final PopupControl popup;
    private Contact sender;
    private String content;
    private CommunicationCenter communicationCenter;
    @FXML ImageView senderIcon;
    @FXML Label senderLabel;
    @FXML Label contentLabel;
    private Stage stage;

    public Notification(Contact sender, String content, CommunicationCenter communicationCenter) {
        this.sender = sender;
        this.content = content;
        this.communicationCenter = communicationCenter;

        URL resource = getClass().getResource("Notification.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        senderLabel.setText(sender.toString());
        contentLabel.setText(content);

        popup = new PopupControl();
        //this.setStyle("-fx-background-color:white");
        popup.setX(0);
        popup.setY(0);
        popup.getScene().setRoot(this);

        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setOpacity(0.0);
        stage.setWidth(0.0);
        stage.setHeight(0.0);
        //stage.show();
    }

    public void show() {
        stage.show();
        popup.show(stage);
    }

    @FXML
    public void dismiss() {
        popup.hide();
        stage.hide();
    }

    @FXML
    public void reply() {
        dismiss();
        ((Stage) communicationCenter.getScene().getWindow()).toFront();
    }

}
