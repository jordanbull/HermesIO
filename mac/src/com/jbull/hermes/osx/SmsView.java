package com.jbull.hermes.osx;

import com.jbull.hermes.desktop.Sms;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;


public class SmsView extends HBox {

    private final Sms sms;

    @FXML Label content;

    public SmsView(Sms sms) {
        this.sms = sms;
        String fxmlFile;
        if (sms.isSenderOfMessage()) {
            fxmlFile = "SentSmsView.fxml";
        } else {
            fxmlFile = "ReceivedSmsView.fxml";
        }
        URL resource = getClass().getResource(fxmlFile);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        content.setText(sms.getContent());
    }

    public static class SmsListCell extends ListCell<SmsView> {
        @Override
        protected void updateItem(SmsView item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(item);
        }
    }
}
