package com.jbull.hermes.desktop;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;


public class DefaultNotification extends VBox implements Notification {
    private PopupControl popup = null;

    private static PopupControl currentPopup = null;

    @FXML Label subjectLabel;
    @FXML Label contentLabel;
    private static Stage stage;

    public DefaultNotification() {
        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setOpacity(0.0);
        stage.setWidth(0.0);
        stage.setHeight(0.0);
    } //Used only for launching from

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

        subjectLabel.setText(sender);
        contentLabel.setText(content);

        popup = new PopupControl();
        popup.setX(0);
        popup.setY(0);
        popup.getScene().setRoot(this);
    }

    public void show() {
        KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1.0);
        KeyValue fadeOutEnd   = new KeyValue(popup.opacityProperty(), 0.0);

        KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        KeyFrame kfEnd   = new KeyFrame(Duration.millis(500), fadeOutEnd);

        Timeline timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(Duration.millis(10000));
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        popup.hide();
                        if (currentPopup == popup) {
                            currentPopup = null;
                        }
                    }
                });

            }
        });
        stage.toFront();
        stage.show();
        popup.show(stage);
        timeline.play();
        if (currentPopup != null) {
            currentPopup.hide();
        }
        currentPopup = popup;
    }

    @Override
    public boolean notify(String subject, String content, byte[] imageData) {
        new DefaultNotification(subject, content, imageData).show();
        return true;
    }
}
