package com.cinema.controllers.admin.screen;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ScreenManagementController implements Initializable {

    @FXML
    private TableView<?> screenTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: load danh sách phòng chiếu
    }

    @FXML
    private void openAddScreen() {
        // TODO: mở modal thêm phòng chiếu
    }
}
