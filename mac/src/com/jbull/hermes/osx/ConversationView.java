package com.jbull.hermes.osx;

import com.jbull.hermes.Message;
import com.jbull.hermes.MessageHelper;
import com.jbull.hermes.desktop.Conversation;
import com.jbull.hermes.desktop.Sms;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.TraversalEngine;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class ConversationView extends BorderPane {
    @FXML TextArea textInput;
    @FXML ListView messageList;
    @FXML Button sendButton;
    Message.Contact me = MessageHelper.createContact("Me", "My Number", null, null);

    ContactView contact;
    private Conversation conversation;
    private State state;

    public ConversationView(Conversation conversation, ContactView contact, State state) {
        this.state = state;
        URL resource = getClass().getResource("ConversationView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.contact = contact;
        if (conversation == null) {
            conversation = new Conversation(contact.getPhoneNumber());
        }
        this.conversation = conversation;

        messageList.setCellFactory(new Callback<ListView<SmsView>, ListCell<SmsView>>() {
            @Override
            public ListCell<SmsView> call(ListView<SmsView> lv) {
                SmsView.SmsListCell cell = new SmsView.SmsListCell();
                return cell;
            }
        });
        setTraversal();
    }

    /* This code sets the order for tabbing between the textInput and the sendButton and enter on the button for sending
         */
    private void setTraversal() {
        // convert tabs on the textInput to shift-tab to avoid tab characters
        textInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                // TODO Auto-generated method stub
                try {
                    Robot robot = new Robot();
                    switch ( keyEvent.getCode() ) {
                        case TAB :

                            if (!keyEvent.isShiftDown()) {
                                robot.keyPress(java.awt.event.KeyEvent.VK_SHIFT);
                                robot.keyPress(java.awt.event.KeyEvent.VK_TAB);
                                robot.keyRelease(java.awt.event.KeyEvent.VK_TAB);
                                robot.keyRelease(java.awt.event.KeyEvent.VK_SHIFT);
                                keyEvent.consume();
                                break;
                            }
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        });
        TraversalEngine engine = new TraversalEngine(this, false) {
            @Override
            public void trav(Node node, Direction drctn) {
                if (node == textInput && (drctn == Direction.NEXT || drctn == Direction.PREVIOUS)) {
                    sendButton.requestFocus();
                } else if (node == sendButton && (drctn == Direction.NEXT || drctn == Direction.PREVIOUS)) {
                    textInput.requestFocus();
                }
            }
        };
        this.setImpl_traversalEngine(engine);
        sendButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    send();
                    keyEvent.consume();
                }
            }
        });
    }

    public void update() {
        System.out.println("updating GUI for: "+contact.getPhoneNumber());
        final ObservableList<SmsView> texts = new ListView<SmsView>().getItems();
        for (Sms sms : conversation.getMessages()) {
            texts.add(new SmsView(sms));
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageList.setItems(texts);
                messageList.scrollTo(messageList.getItems().size());
            }
        });
    }

    @FXML
    public void send() {
        ArrayList<Message.Contact> recipents = new ArrayList<Message.Contact>();
        recipents.add(contact.getContactMsg());
        String textContent = textInput.getText();
        Message.SmsMessage msg = MessageHelper.createSmsMessage(me, textContent, System.currentTimeMillis(), recipents, true);
        state.send(msg);
        state.addSms(msg, true);
        textInput.clear();
    }
}
