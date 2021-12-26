package com.chat.controllers;

import com.chat.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class NicknameFormController {
    @FXML
    private TextField nicknameTextField;

    @FXML
    public void onActionClick() {
        String name = nicknameTextField.getText();

        if (name.isEmpty()) {
            AlertUtils.showWarning("Hãy nhập nickname");
            return;
        }


    }
}