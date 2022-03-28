package io.international_chat.guiclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Objects;

public class ChatController {
    @FXML
    private TextField messageInput;

    @FXML
    private ListView<String> chatHistory;

    private static IOCommandes ioc;

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    private void ShowMessagebox(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(Objects.requireNonNull(ChatApplication.class.getResource("icon.png")).toExternalForm()));
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public ChatController() {
        try {
            Socket socket = new Socket(ChatApplication.getHost(), ChatApplication.getPort());

            ioc = new IOCommandes(socket);
            PrintStream ps = new PrintStream(byteArrayOutputStream);
            ioc.setCommandWrite(ps);
            (new Thread(() -> {
                while (!socket.isClosed()) {
                    try {
                        ioc.println(ioc.socketRead());
                        Platform.runLater(() -> {
                            chatHistory.getItems().add(byteArrayOutputStream.toString().trim());
                            byteArrayOutputStream.reset();
                        });
                    } catch (IOException e) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (e.getMessage().equals("Connection reset")) {
                            ioc.println("Connection lost with the server!");
                            Platform.runLater(() -> {
                                chatHistory.getItems().add(byteArrayOutputStream.toString().trim());
                                byteArrayOutputStream.reset();
                                ShowMessagebox("Connection lost", "Connection lost with the server!", "Make sure you have a connection.");
                            });
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            })).start();
        } catch (IOException e) {
            ShowMessagebox("Can't connect to the server", "Can't connect to the server", "Make sure the address and the port are correct.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSendButtonClick() {
        ioc.socketWrite(messageInput.getText());
        chatHistory.getItems().add(messageInput.getText());
        messageInput.clear();
    }
}
