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
import java.util.concurrent.TimeUnit;

public class ChatRoomController implements Initializable {
    public Client client;

    public Stage stageNicknameController;

    public JSONObject data = new JSONObject();

    public NicknameFormController nicknameFormController;

    @FXML
    private TextField tf_message, timeTextField;

    @FXML
    private VBox vbox_messages;

    @FXML
    private Label labelClientName, labelMyName;

    @FXML
    private ScrollPane sp_main;

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

                                data.put("myNickname", "");
                                data.put("myName", "");
                                data.put("status", "no connected");
                                client.getSend().sendData(data);
                                try {
                                    client.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, data.get("clientNickname") + " đã thoát khỏi chat. Bạn muốn tạo kết nối mới");
                                Optional<ButtonType> result = alert.showAndWait();

                                if (!result.isPresent() || result.get() != ButtonType.OK) {
                                    System.exit(0);
                                    return;
                                } else {
                                    // Mở cửa sổ mới nhập nickname
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
                            showMessageReceive(message);
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
            tf_message.setFocusTraversable(true);
            return;
        }

        showMessage(message);
        tf_message.clear();
        tf_message.setFocusTraversable(true);

        data.put("message", message);
        client.getSend().sendData(data);
    }

    public void showMessage(String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(1, 5, 1, 80));

        Text text = new Text(message);
        text.setFill(Color.color(0.934, 0.935, 0.996));
        text.setFont(Font.font("Segoe UI Historic", 14));

        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("text-flow-show-message");
        textFlow.setPadding(new Insets(8, 12, 8, 12));

        hBox.getChildren().add(textFlow);
        vbox_messages.getChildren().add(hBox);

        int sizeVbox = vbox_messages.getChildren().size();

        if (sizeVbox >= 2) {
            HBox hbox = (HBox) vbox_messages.getChildren().get(sizeVbox - 1);

            if (((HBox) (vbox_messages.getChildren().get(sizeVbox - 2))).getAlignment().equals(Pos.CENTER_RIGHT)) {
                ((HBox) (vbox_messages.getChildren().get(sizeVbox - 2))).getChildren().get(0).getStyleClass().add("text-flow-show-message-pre");
                hbox.getChildren().get(0).getStyleClass().add("text-flow-show-message-curr");
            } else {
                hBox.setPadding(new Insets(10, 5, 1, 80));
                return;
            }

            if (sizeVbox >= 3) {
                for (int i = sizeVbox - 2; i >= 1; i--) {
                    HBox hBox1 = (HBox) vbox_messages.getChildren().get(i);
                    int j = i;
                    if (((HBox) (vbox_messages.getChildren().get(j - 1))).getAlignment().equals(Pos.CENTER_RIGHT)) {
                        ((HBox) (vbox_messages.getChildren().get(j - 1))).getChildren().get(0).getStyleClass().add("text-flow-show-message-pre");
                        hBox1.getChildren().get(0).getStyleClass().add("text-flow-show-message-between");
                        continue;
                    }
                    break;
                }
            }
        }

        vbox_messages.heightProperty().addListener(observable -> sp_main.setVvalue(1D));
    }

    public void showMessageReceive(String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(1, 80, 1, 5));

        Text text = new Text(message);
        text.setFill(Color.BLACK);
        text.setFont(Font.font("Segoe UI Historic", 14));

        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("text-flow-show-message-receive");
        textFlow.setPadding(new Insets(8, 12, 8, 12));

        hBox.getChildren().add(textFlow);
        vbox_messages.getChildren().add(hBox);

        int sizeVbox = vbox_messages.getChildren().size();

        if (sizeVbox >= 2) {
            HBox hbox = (HBox) vbox_messages.getChildren().get(sizeVbox - 1);

            if (((HBox) (vbox_messages.getChildren().get(sizeVbox - 2))).getAlignment().equals(Pos.CENTER_LEFT)) {
                ((HBox) (vbox_messages.getChildren().get(sizeVbox - 2))).getChildren().get(0).getStyleClass().add("text-flow-show-message-receive-pre");
                hbox.getChildren().get(0).getStyleClass().add("text-flow-show-message-receive-curr");
            } else {
                hBox.setPadding(new Insets(10, 80, 1, 5));
                return;
            }

            if (sizeVbox >= 3) {
                for (int i = sizeVbox - 2; i >= 1; i--) {
                    HBox hBox1 = (HBox) vbox_messages.getChildren().get(i);
                    int j = i;
                    if (((HBox) (vbox_messages.getChildren().get(j - 1))).getAlignment().equals(Pos.CENTER_LEFT)) {
                        ((HBox) (vbox_messages.getChildren().get(j - 1))).getChildren().get(0).getStyleClass().add("text-flow-show-message-receive-pre");
                        hBox1.getChildren().get(0).getStyleClass().add("text-flow-show-message-receive-between");
                        continue;
                    }
                    break;
                }
            }
        }

        vbox_messages.heightProperty().addListener(observable -> sp_main.setVvalue(1D));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vbox_messages.setPadding(new Insets(5, 0, 5, 0));
        new Thread(() -> {
            Long startMillis = System.currentTimeMillis();
            while (true) {
                Long millis = System.currentTimeMillis() - startMillis;
                String hms = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                Platform.runLater(() -> {
                    timeTextField.setText(hms);
                });
                try {
                    Thread.sleep(1000); //1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if (data.get("message") != "") {
            showMessage(data.get("message").toString());
        }
        data.put("status", "accepted");
        labelClientName.setText(data.get("clientNickname").toString());
        labelMyName.setText(data.get("myNickname").toString() + " (Bạn)");
        try {
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}