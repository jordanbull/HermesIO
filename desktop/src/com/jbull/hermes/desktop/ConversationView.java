package com.jbull.hermes.desktop;

import com.jbull.hermes.Logger;
import com.jbull.hermes.messages.ContactMessage;
import com.jbull.hermes.messages.SmsMessage;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.TraversalEngine;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class ConversationView extends BorderPane {
    private static final int SMS_BATCH_SIZE = 10;
    private static final int SCROLL_ADD_SMS_SIZE = 5;
    @FXML TextArea textInput;
    @FXML ListView messageList;
    @FXML Button sendButton;
    ContactMessage me = new ContactMessage("My Number", "Name");

    ContactView contact;
    private Conversation conversation;
    private State state;
    private ObservableList<SmsView> smsViews;
    int topListIndex = Integer.MAX_VALUE;
    int bottomListIndex = 0;

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
        smsViews = messageList.getItems();
        this.contact = contact;
        /*if (conversation == null) {
            conversation = new Conversation(contact.getPhoneNumber());
        }*/
        this.conversation = conversation;
        topListIndex = Math.max(conversation.getMessages().size() - SMS_BATCH_SIZE, 0);
        bottomListIndex = topListIndex;
        update();

        messageList.setCellFactory(new Callback<ListView<SmsView>, ListCell<SmsView>>() {
            @Override
            public ListCell<SmsView> call(ListView<SmsView> lv) {
                SmsView.SmsListCell cell = new SmsView.SmsListCell();
                return cell;
            }
        });
        messageList.setOnScrollFinished(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                // hacky way to check if scrolled to top
                for (Node node: messageList.lookupAll(".scroll-bar")) {
                    if (node instanceof ScrollBar) {
                        final ScrollBar bar = (ScrollBar) node;
                        if (bar.getOrientation()== Orientation.VERTICAL) {
                            if (bar.getValue() == 0.0) {
                                prependSmses(SCROLL_ADD_SMS_SIZE);
                            }
                        }

                    }
                }
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
                    Logger.log(e);
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
        Logger.log("updating GUI for: " + contact.getPhoneNumber());
        appendSmses();
    }

    private void prependSmses(final int num) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int n = Math.min(num, topListIndex);
                ArrayList<Sms> smses = conversation.getMessages();
                for (int i = topListIndex - 1; i >= topListIndex-n; i--) {
                    smsViews.add(0, new SmsView(smses.get(i)));
                }
                topListIndex = topListIndex-n;
            }
        });
    }

    private void appendSmses() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ArrayList<Sms> smses = conversation.getMessages();
                for (;bottomListIndex < smses.size(); bottomListIndex++) {
                    smsViews.add(new SmsView(smses.get(bottomListIndex)));
                }
                messageList.scrollTo(messageList.getItems().size());
            }
        });
    }

    @FXML
    public void send() {
        ContactMessage recipient =contact.getContactMsg();
        String textContent = textInput.getText();
        SmsMessage msg = new SmsMessage(me, recipient, textContent, System.currentTimeMillis());
        state.send(msg);
        state.addSms(msg, true);
        textInput.clear();
        textInput.requestFocus();
    }
}
