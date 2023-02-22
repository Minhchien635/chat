module com.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires lombok;

    opens com.chat to javafx.fxml;
    exports com.chat;
    exports com.chat.socket.Client;
    opens com.chat.socket.Client to javafx.fxml;
    exports com.chat.socket.Server;
    opens com.chat.socket.Server to javafx.fxml;
    exports com.chat.controllers;
    opens com.chat.controllers to javafx.fxml;
}