package client.controller;

import client.ClientSocket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField pseudoField;
    @FXML
    private Button connectButton;

    private ClientSocket clientSocket = new ClientSocket();

    @FXML
    public void onConnect(ActionEvent event) {
        String pseudo = pseudoField.getText();
        if (pseudo == null || pseudo.trim().isEmpty()) {
            showAlert("Erreur", "Le pseudo ne doit pas être vide.");
            return;
        }
        boolean connected = clientSocket.connect("localhost", 1210);
        if (!connected) {
            showAlert("Erreur", "Impossible de se connecter au serveur.");
            return;
        }
        try {
            String welcome = clientSocket.receive();
            if (welcome != null) {
                clientSocket.send(pseudo);
                String response = clientSocket.receive();
                if (response != null && response.contains("réussie")) {
                    openChatWindow(pseudo);
                } else if (response != null) {
                    showAlert("Erreur", response);
                    clientSocket.close();
                } else {
                    showAlert("Erreur", "Aucune réponse du serveur après envoi du pseudo.");
                    clientSocket.close();
                }
            } else {
                showAlert("Erreur", "Pas de message de bienvenue du serveur.");
                clientSocket.close();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Problème lors de la connexion :\n" + e.getMessage());
            clientSocket.close();
            e.printStackTrace();
        }
    }

    private void openChatWindow(String pseudo) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/chat.fxml"));
        Parent root = loader.load();
        ChatController chatController = loader.getController();
        chatController.init(clientSocket, pseudo);
        Stage stage = (Stage) connectButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Chat - " + pseudo);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 