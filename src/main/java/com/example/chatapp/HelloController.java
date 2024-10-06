package com.example.chatapp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class HelloController {
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageInput;

    @FXML
    private void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            // Agregar el mensaje al contenedor de mensajes
            messagesContainer.getChildren().add(new Label(message));
            messageInput.clear(); // Limpiar el campo de entrada
        }
    }
}
