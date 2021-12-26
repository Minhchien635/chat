package com.chat.utils;

import javafx.scene.control.Alert;

public class AlertUtils {
    public static Alert alert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert;
    }

    public static void show(Alert.AlertType alertType, String content) {
        Alert alert = alert(alertType, content);
        alert.showAndWait();
    }

    public static void showWarning(String content) {
        show(Alert.AlertType.WARNING, content);
    }
}
