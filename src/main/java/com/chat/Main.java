package com.chat;

import com.chat.controllers.NicknameFormController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.chat/fxml/nickname_form.fxml"));
        NicknameFormController controller = new NicknameFormController();
        controller.stageMain = stage;
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Nickname");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}