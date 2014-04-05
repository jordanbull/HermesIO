package com.jbull.hermes.desktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;


public class DefaultNotification extends GridPane implements Notification {
    private final PopupControl popup;

    @FXML ImageView senderIcon;
    @FXML Label senderLabel;
    @FXML Label contentLabel;
    private Stage stage;

    public DefaultNotification(String sender, String content, byte[] imgData) {
        URL resource = getClass().getResource("DefaultNotification.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        senderLabel.setText(sender);
        contentLabel.setText(content);
        if (imgData != null)
            senderIcon.setImage(new Image(new ByteArrayInputStream(imgData)));

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
        //TODO
    }

    @Override
    public boolean notify(String sender, String content, byte[] imageData) {
        new DefaultNotification(sender, content, imageData).show();
        return true;
    }
}
