package com.chat.controllers;

import com.chat.socket.Client.Client;
import com.chat.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class NicknameFormController implements Initializable {
    @FXML
    private TextField nicknameTextField;

    public Client client = new Client("localhost", 1234);

    public NicknameFormController() throws IOException {
    }

    @FXML
    public void onActionClick() throws IOException {
        String name = nicknameTextField.getText();

        if (name.isEmpty()) {
            AlertUtils.showWarning("Hãy nhập nickname");
            return;
        }
        client.run();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("myNickname", name);
        jsonObject.put("clientNickname", "");
        jsonObject.put("myName", "");
        jsonObject.put("clientName", "");
        jsonObject.put("message", "");
        jsonObject.put("status", "");

        client.getSend().sendData(jsonObject);

        JSONObject receive;
        while (true) {
            receive = client.getRev().receive();
            if (receive.get("status").toString().equals("") && receive.get("clientName") != "") {
                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Chấp nhận kết nối với " + receive.get("clientNickname"));

                Optional<ButtonType> result = alert.showAndWait();
                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    receive.put("status", "no accepted");
                    client.getSend().sendData(receive);
                } else {
                    receive.put("status", "ok");
                    client.getSend().sendData(receive);
                }
            }
            if (receive.get("status").toString().equals("client ok")) {
                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Chấp nhận kết nối với " + receive.get("clientNickname"));

                Optional<ButtonType> result = alert.showAndWait();
                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    receive.put("status", "no accepted");
                    client.getSend().sendData(receive);
                } else {
                    receive.put("status", "accepted");
                    client.getSend().sendData(receive);
                    break;
                }
            }

            if (receive.get("status").toString().equals("accepted")) {
                break;
            }
        }
        System.out.println("chat room");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.chat/fxml/chat_room.fxml"));
        ChatRoomController chatRoomController = new ChatRoomController();
        chatRoomController.client = client;
        chatRoomController.data = receive;
        fxmlLoader.setController(chatRoomController);
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Chat");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}