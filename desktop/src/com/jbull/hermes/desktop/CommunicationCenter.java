package com.jbull.hermes.desktop;

import com.jbull.hermes.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;


public class CommunicationCenter extends BorderPane {
    private final Notification systemNotification;
    @FXML ListView<ContactView> contactsList;
    @FXML AnchorPane messagingPane;
    @FXML TextField contactSearch;
    @FXML Label connectionStatusLabel;
    @FXML Label extraInfoLabel;

    private final String CONNECTED = "Connected";
    private final String DISCONNECTED = "Disconnected";

    private State state;
    private TimeSortedContacts timeSortedContacts = new TimeSortedContacts();
    private Notification messageNotification;

    public CommunicationCenter(Notification messageNotification, Notification systemNotification) throws IOException {
        this.messageNotification = messageNotification;
        this.systemNotification = systemNotification;
        URL resource = getClass().getResource("CommunicationCenter.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        contactsList.setCellFactory(new Callback<ListView<ContactView>, ListCell<ContactView>>() {
            @Override
            public ListCell<ContactView> call(ListView<ContactView> contactViewListView) {
                return new ContactView.ContactListCell();
            }
        });
        contactsList.setItems(timeSortedContacts);

        contactsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ContactView>() {
            @Override
            public void changed(ObservableValue<? extends ContactView> observable, ContactView oldValue, ContactView newValue) {
                if (newValue == null) {
                    return;
                }
                newValue.select();
                ConversationView convo = newValue.getConversationView();
                AnchorPane.setBottomAnchor(convo, 0.0);
                AnchorPane.setTopAnchor(convo, 0.0);
                AnchorPane.setLeftAnchor(convo, 0.0);
                AnchorPane.setRightAnchor(convo, 0.0);
                messagingPane.getChildren().clear();
                messagingPane.getChildren().add(convo);
                Logger.log("Selected item: " + newValue);
                if (oldValue != null) {
                    oldValue.deselect();
                }
            }
        });

        contactSearch.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.ENTER) {
                    if (contactsList.getItems().size() > 0) {
                        contactsList.requestFocus();
                        contactsList.getSelectionModel().select(0);
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            contactsList.getSelectionModel().getSelectedItem().getConversationView().textInput.requestFocus();
                        }
                    }
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.TAB) {
                    keyEvent.consume();
                }
            }
        });
        contactsList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.isShiftDown() && keyEvent.getCode() == KeyCode.TAB) {
                    contactSearch.requestFocus();
                } else if ((keyEvent.getCode() == KeyCode.TAB || keyEvent.getCode() == KeyCode.ENTER) && contactsList.getSelectionModel().getSelectedItem() != null) {
                    contactsList.getSelectionModel().getSelectedItem().getConversationView().textInput.requestFocus();
                    keyEvent.consume();
                }
            }
        });

        contactSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String fromString, String toString) {
                searchTyped();
            }
        });
    }

    public void resetSearch() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                contactSearch.setText("");
                searchTyped();
            }
        });
    }

    public void setConnectionStatusLabel(final boolean connected) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    connectionStatusLabel.setText(CONNECTED);
                } else {
                    connectionStatusLabel.setText(DISCONNECTED);
                }
            }
        });
    }

    public TimeSortedContacts getDefaultContacts() {
        return timeSortedContacts;
    }

    protected void setState(State state) {
        this.state = state;
    }

    public void searchTyped() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final String s = contactSearch.getText();
                contactsList.getSelectionModel().select(-1);
                if (s.equals("")) {
                    contactsList.setItems(timeSortedContacts);
                } else {
                    ObservableList<ContactView> contacts = state.searchContacts(s);
                    contactsList.setItems(contacts);
                }
            }
        });
    }

    public void messageNotify(final String subject, final String body, final byte[] imageData) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageNotification.notify(subject, body, imageData);
            }
        });
    }

    public void systemNotify(final String subject, final String body, final byte[] imageData) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                systemNotification.notify(subject, body, imageData);
            }
        });
    }

    public void setExtraInfo(final String str) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                extraInfoLabel.setText(str);
            }
        });
    }

    public class TimeSortedContacts extends ModifiableObservableListBase<ContactView> {
        private LinkedList<ContactView> orderedContacts = new LinkedList<ContactView>();

        public void insert(ContactView c) {
            long time = c.conversation.mostRecentMsgNum();
            if (orderedContacts.contains(c)) {
                orderedContacts.remove(c);
            }
            int i = 0;
            for (; i < orderedContacts.size(); i++) {
                if (time > orderedContacts.get(i).conversation.mostRecentMsgNum()) {
                    break;
                }
            }
            orderedContacts.add(i, c);
        }

        @Override
        public ContactView get(int i) {
            return orderedContacts.get(i);
        }

        @Override
        public int size() {
            return orderedContacts.size();
        }

        @Override
        protected void doAdd(int i, ContactView contactView) {
            insert(contactView);
        }

        @Override
        protected ContactView doSet(int i, ContactView contactView) {
            return null;
        }

        @Override
        protected ContactView doRemove(int i) {
            return null;
        }
    }

    /* This method specifically for getting the contact search text field for styling
     */
    public TextField getContactSearch() {
        return contactSearch;
    }
}
