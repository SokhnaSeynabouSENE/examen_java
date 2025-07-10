package client.controller;

import client.ClientSocket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;

public class ChatController {
    @FXML
    private ListView<String> messageList;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Button quitButton;

    private ClientSocket clientSocket;
    private String pseudo;
    private Thread receiveThread;

    public void init(ClientSocket clientSocket, String pseudo) {
        this.clientSocket = clientSocket;
        this.pseudo = pseudo;
        startReceiving();
    }

    @FXML
    public void onSend() {
        String text = messageField.getText();
        if (text == null || text.trim().isEmpty()) return;
        clientSocket.send(text);
        messageField.clear();
    }

    @FXML
    public void onQuit() {
        clientSocket.send("quit");
        clientSocket.close();
        Platform.exit();
    }

    private void startReceiving() {
        receiveThread = new Thread(() -> {
            try {
                String line;
                while ((line = clientSocket.receive()) != null) {
                    System.out.println("RECU DU SERVEUR : " + line);
                    String msg = line;
                    Platform.runLater(() -> {
                        messageList.getItems().add(msg);
                    });

                    if (line.equals("Vous avez quitté le groupe.") ||
                            line.equals("Vous êtes banni pour message inapproprié.")) {
                        Platform.runLater(() -> {
                            messageField.setDisable(true);
                            sendButton.setDisable(true);
                            emojiButton.setDisable(true);
                            quitButton.setDisable(true);
                        });

                        Thread.sleep(1000);
                        Platform.exit();
                        break;
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> showNotification("Déconnecté", "Connexion perdue."));
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML private Button emojiButton;
    @FXML private TextField messagField;

    private Popup emojiPopup;

    private static final String[][] emojiCategories = {
            // Smiles & People
            {"😀","😁","😂","🤣","😃","😄","😅","😆","😉","😊","😋","😎","😍","😘","🥰","😗","😙","😚","🙂","🤗"},
            // Food & Drink
            {"🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑"},
            // Activities & Objects
            {"⚽","🏀","🏈","⚾","🎾","🏐","🏉","🎱","🏓","🥅","🏒","🏑","🏏","🥍","🏸","🏹","🎣","🥊","🥋","🎽"},
            // Symbols
            {"❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗","💖","💘","💝","💟","☮️"}
    };

    @FXML
    public void onEmoji() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.hide();
            return;
        }
        emojiPopup = new Popup();
        VBox root = new VBox(5);
        root.setStyle("-fx-background-color: #fff; -fx-padding: 10; -fx-border-color: #c1c1c1; -fx-border-width: 1;");

        for (String[] category : emojiCategories) {
            GridPane grid = new GridPane();
            grid.setHgap(5);
            grid.setVgap(5);
            int col = 0, row = 0;
            for (String emoji : category) {
                Button btn = new Button(emoji);
                btn.setMinSize(32, 32);
                btn.setStyle("-fx-font-size: 20;");
                btn.setOnAction(e -> {
                    int pos = messageField.getCaretPosition();
                    String oldText = messageField.getText();
                    messageField.setText(oldText.substring(0, pos) + emoji + oldText.substring(pos));
                    messageField.positionCaret(pos + emoji.length());
                    emojiPopup.hide();
                });
                grid.add(btn, col, row);
                col++;
                if (col == 10) { col = 0; row++; }
            }
            root.getChildren().add(grid);
        }

        emojiPopup.getContent().add(root);
        emojiPopup.setAutoHide(true);
        // Affichage sous le bouton
        emojiPopup.show(emojiButton, emojiButton.localToScreen(0, emojiButton.getHeight()).getX(), emojiButton.localToScreen(0, emojiButton.getHeight()).getY());
    }

} 