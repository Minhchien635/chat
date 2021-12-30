package com.chat;

import com.chat.controllers.NicknameFormController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.NetworkInterface;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.chat/fxml/nickname_form.fxml"));
        NicknameFormController controller = new NicknameFormController();
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 479, 432);
        stage.setTitle("Nickname");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}