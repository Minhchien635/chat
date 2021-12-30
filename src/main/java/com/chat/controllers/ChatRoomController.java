package com.chat.controllers;

import com.chat.socket.Client.Client;
import com.chat.utils.AlertUtils;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatRoomController implements Initializable {
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;

    public Client client;

    public JSONObject data = new JSONObject();

    public void receive() throws IOException {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        data = client.getRev().receive();
                        String message;
                        message = (String) data.get("message");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    public void onActionClick() {
        String message = tf_message.getText();

        if (message.isEmpty()) {
            AlertUtils.showWarning("Hãy nhập tin nhắn");
            return;
        } else {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(message);
            TextFlow textFlow = new TextFlow(text);

            textFlow.setStyle("-fx-color: white;" + "-fx-background-color: rgb(0, 132, 255);" + "-fx-background-radius: 20px;");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.935, 0.996));

            hBox.getChildren().add(textFlow);
            vbox_messages.getChildren().add(hBox);
            tf_message.clear();

            data.put("message", message);
            client.getSend().sendData(data);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        data.put("status", "accepted");
        try {
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}