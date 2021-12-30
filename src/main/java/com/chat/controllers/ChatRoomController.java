package com.chat.controllers;

import com.chat.socket.Client.Client;
import com.chat.utils.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatRoomController implements Initializable {
    @FXML
    private TextField tf_message;

    @FXML
    private VBox vbox_messages;

    @FXML
    private Label labelInfo;

    public Client client;

    public JSONObject data = new JSONObject();

    public void receive() throws IOException {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        data = client.getRev().receive();
                        String message = (String) data.get("message");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                String status = (String) data.get("status");
                                if (status.equals("no connected")) {
                                    Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Đã mất kết nối. Bạn muốn tạo cuộc trò chuyện mới");

                                    Optional<ButtonType> result = alert.showAndWait();
                                    if (!result.isPresent() || result.get() != ButtonType.OK) {
                                        data.put("status", "no accepted");
                                        client.getSend().sendData(data);
                                    } else {
                                        data.put("myName","");
                                        data.put("status", "");
                                        client.getSend().sendData(data);
                                    }
                                }

                                showMessage(message);
                            }
                        });
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
            showMessage(message);
            tf_message.clear();

            data.put("message", message);
            client.getSend().sendData(data);
        }
    }

    public void showMessage(String message) {
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
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (data.get("message") != "") {
            showMessage(data.get("message").toString());
        }
        data.put("status", "accepted");
        labelInfo.setText("Bạn đang trò chuyện với " + data.get("clientNickname").toString());
        try {
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}