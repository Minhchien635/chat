package com.chat.controllers;

import com.chat.socket.Client.Client;
import com.chat.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class NicknameFormController implements Initializable {
    public Stage stageMain;
    public Client client = new Client("localhost", 1234);
    @FXML
    private TextField nicknameTextField;
    @FXML
    private Button okButton;

    public NicknameFormController() throws IOException {
    }

    @FXML
    public void onActionClick() throws IOException {
        String myNickname = nicknameTextField.getText();

        if (myNickname.trim().isEmpty()) {
            AlertUtils.showWarning("Hãy nhập nickname");
            return;
        }

        client.run();

        JSONObject jsonObject = convertDtoToJson(myNickname, "", "", "", "", "");

        client.getSend().sendData(jsonObject);

        okButton.setDisable(true);

        JSONObject receive;
        while (true) {
            receive = client.getRev().receive();
            System.out.println(receive.toString());
            if (receive.get("status").toString().equals("") && receive.get("clientName") != "") {
                System.out.println("trong sdk");
                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Chấp nhận kết nối với " + receive.get("clientNickname"));
                Optional<ButtonType> result = alert.showAndWait();

                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    receive.put("status", "no accepted");
                    client.getSend().sendData(receive);
                    continue;
                }
                receive.put("status", "ok");
                client.getSend().sendData(receive);
                continue;
            }

            if (receive.get("status").toString().equals("nickname existed")) {
                okButton.setDisable(false);

                AlertUtils.showWarning("Nickname đã tồn tại. Hãy nhập nickname khác");
                client.close();
                client = new Client("localhost", 1234);
                return;
            }

            if (receive.get("status").toString().equals("client ok")) {
                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Chấp nhận kết nối với " + receive.get("clientNickname"));

                Optional<ButtonType> result = alert.showAndWait();
                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    receive.put("status", "no accepted");
                    System.out.println(receive);
                    client.getSend().sendData(receive);
                    continue;
                }
                receive.put("status", "accepted");
                client.getSend().sendData(receive);
                break;
            }

            if (receive.get("status").toString().equals("no connected")) {
                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Mất kết nối. Bạn muốn tạo kết nối mới với " + receive.get("clientNickname"));

                Optional<ButtonType> result = alert.showAndWait();
                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    receive.put("status", "no accepted");
                    client.getSend().sendData(receive);
                    continue;
                }
                receive.put("status", "ok");
                client.getSend().sendData(receive);
                break;
            }

            if (receive.get("status").toString().equals("accepted")) {
                break;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.chat/fxml/chat_room.fxml"));
        Stage stage = new Stage();
        ChatRoomController chatRoomController = new ChatRoomController();
        chatRoomController.client = client;
        chatRoomController.data = receive;
        chatRoomController.stageNicknameController = stage;
        chatRoomController.nicknameFormController = this;
        fxmlLoader.setController(chatRoomController);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Chat");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        if (stageMain != null) {
            stageMain.close();
        }

        JSONObject jo = convertDtoToJson("", receive.get("myName").toString(), "", "", "", "no connected");
        stage.setOnCloseRequest(we -> {
            client.getSend().sendData(jo);
            try {
                client.close();
                stage.close();
                if (stageMain != null) {
                    stageMain.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public JSONObject convertDtoToJson(String myNickname, String myName, String clientNickname, String clientName, String message, String status) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("myNickname", myNickname);
        jsonObject.put("myName", myName);
        jsonObject.put("clientNickname", clientNickname);
        jsonObject.put("clientName", clientName);
        jsonObject.put("message", message);
        jsonObject.put("status", status);

        return jsonObject;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}