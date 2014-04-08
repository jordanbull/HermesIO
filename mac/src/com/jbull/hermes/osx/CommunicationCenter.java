package com.jbull.hermes.osx;

import com.aquafx_project.AquaFx;
import com.aquafx_project.controls.skin.styles.TextFieldType;
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
    @FXML ListView<ContactView> contactsList;
    @FXML AnchorPane messagingPane;
    @FXML TextField contactSearch;
    @FXML Label connectionStatusLabel;

    private final String CONECTED = "Connected";
    private final String DISCONNECTED = "Disconnected";

    private State state;
    private TimeSortedContacts timeSortedContacts = new TimeSortedContacts();

    public CommunicationCenter() throws IOException {
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
        AquaFx.createTextFieldStyler().setType(TextFieldType.SEARCH).style(contactSearch);
        contactsList.setItems(timeSortedContacts);

        contactsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ContactView>() {
            @Override
            public void changed(ObservableValue<? extends ContactView> observable, ContactView oldValue, ContactView newValue) {
                if (newValue == null) {
                    return;
                }
                ConversationView convo = newValue.getConversationView();
                AnchorPane.setBottomAnchor(convo, 0.0);
                AnchorPane.setTopAnchor(convo, 0.0);
                AnchorPane.setLeftAnchor(convo, 0.0);
                AnchorPane.setRightAnchor(convo, 0.0);
                messagingPane.getChildren().clear();
                messagingPane.getChildren().add(convo);
                System.out.println("Selected item: " + newValue);
                newValue.select();
                if (oldValue != null) {
                    oldValue.deselect();
                }
                //TODO contactsList.set(defaultList)
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
                searchTyped(toString);
            }
        });
    }

    public void setConnectionStatusLabel(final boolean connected) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    connectionStatusLabel.setText(CONECTED);
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

    public void searchTyped(String s) {
        if (s.equals("")) {
            contactsList.setItems(timeSortedContacts);
            contactsList.getSelectionModel().clearSelection();
        } else {
            ObservableList<ContactView> contacts = state.searchContacts(s);
            contactsList.setItems(contacts);
            contactsList.getSelectionModel().clearSelection();
        }
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
}
