package com.chat.controllers;

import com.chat.socket.Client.Client;
import com.chat.utils.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatRoomController implements Initializable {
    public Client client;

    public Stage stageNicknameController;

    public JSONObject data = new JSONObject();

    public NicknameFormController nicknameFormController;

    @FXML
    private TextField tf_message;

    @FXML
    private VBox vbox_messages;

    @FXML
    private Label labelClientName;

    @FXML
    private ScrollPane sp_main;

    @FXML
    private Label labelMyName;

    public void receive() throws IOException {
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        if (client.getSocket().isClosed()) {
                            break;
                        }
                        data = client.getRev().receive();
                        String message = (String) data.get("message");
                        Platform.runLater(() -> {
                            String status = (String) data.get("status");
                            if (status.equals("no connected")) {
                                stageNicknameController.close();
                                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, data.get("clientNickname") + " đã thoát khỏi chat. Bạn muốn tạo kết nối mới");

                                Optional<ButtonType> result = alert.showAndWait();

                                data.put("myNickname", "");
                                data.put("myName", "");
                                data.put("status", "no connected");
                                client.getSend().sendData(data);
                                try {
                                    client.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (!result.isPresent() || result.get() != ButtonType.OK) {
                                    System.exit(0);
                                    return;
                                } else {
                                    Stage stage1 = new Stage();
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.chat/fxml/nickname_form.fxml"));
                                    NicknameFormController controller = null;
                                    try {
                                        controller = new NicknameFormController();
                                        controller.stageMain = stage1;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    fxmlLoader.setController(controller);
                                    Scene scene = null;
                                    try {
                                        scene = new Scene(fxmlLoader.load());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    stage1.setTitle("Nickname");
                                    stage1.setResizable(false);
                                    stage1.setScene(scene);
                                    stage1.show();

                                    stageNicknameController.close();
                                }
                            }
                            showMessageReceive(message, "#e4e6eb", "black");
                        });
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        }.start();
    }

    public void onActionClick() {
        String message = tf_message.getText();

        if (message.isEmpty()) {
            AlertUtils.showWarning("Hãy nhập tin nhắn");
            return;
        } else {
            showMessage(message, "rgb(0, 132, 255)", "white");
            tf_message.clear();

            data.put("message", message);
            client.getSend().sendData(data);
        }
        }

    public void showMessage(String message, String backgroundColor,
                            String textColor) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle("-fx-color: " + textColor + "; -fx-background-color: " + backgroundColor + "; -fx-background-radius: 20px;");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(0.934, 0.935, 0.996));

        text.setFont(Font.font("Segoe UI Historic", 15));

        hBox.getChildren().add(textFlow);
        vbox_messages.getChildren().add(hBox);
        vbox_messages.heightProperty().addListener(observable -> sp_main.setVvalue(1D));
        //sp_main.setVvalue(1D);
    }

    public void showMessageReceive(String message, String backgroundColor,
                                   String textColor) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle("-fx-color: " + textColor + "; -fx-background-color: " + backgroundColor + "; -fx-background-radius: 20px;");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.BLACK);

        text.setFont(Font.font("Segoe UI Historic", 15));


        hBox.getChildren().add(textFlow);
        vbox_messages.getChildren().add(hBox);
        vbox_messages.heightProperty().addListener(observable -> sp_main.setVvalue(1D));
        //sp_main.setVvalue(1D);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (data.get("message") != "") {
            showMessage(data.get("message").toString(), "rgb(0, 132, 255)", "white");
        }
        data.put("status", "accepted");
        labelClientName.setText(data.get("clientNickname").toString());
        labelMyName.setText(data.get("myNickname").toString()+ " (Bạn)");
        try {
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}