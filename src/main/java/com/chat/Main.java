package com.chat;

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
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com.chat/fxml/nickname_form.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 314, 109);
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();
    }
}