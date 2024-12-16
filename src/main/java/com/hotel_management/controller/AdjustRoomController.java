package com.hotel_management.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.hotel_management.Utils.Utils.OptionPane;
import static com.hotel_management.Utils.Utils.containsAlphabetic;
import static com.hotel_management.storage.Database.getConnection;

public class AdjustRoomController implements Initializable {

    public static int selectedRoomNumber;

    @FXML
    private Button delete;

    @FXML
    private Button save;

    @FXML
    private TextField number;

    @FXML
    private TextField price;

    @FXML
    private RadioButton single;

    @FXML
    private RadioButton _double;

    @FXML
    private RadioButton deluxe;

    private Connection connection;

    private PreparedStatement pst;

    public static void setSelectedRoomNumber(int selectedRoomNumber) {
        AdjustRoomController.selectedRoomNumber = selectedRoomNumber;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (selectedRoomNumber != 0) {
            String query = """
                    SELECT roomNumber, roomType, price FROM rooms
                    WHERE roomNumber = ?
                    """;
            try {
                pst = connection.prepareStatement(query);
                pst.setString(1, Integer.toString(selectedRoomNumber));
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    price.setText(rs.getString("price"));
                    number.setText(rs.getString("roomNumber"));
                    setRoomType(rs.getString("roomType"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        ToggleGroup toggleGroup = new ToggleGroup();
        single.setToggleGroup(toggleGroup);
        _double.setToggleGroup(toggleGroup);
        deluxe.setToggleGroup(toggleGroup);
    }

    public void handleAdjustAction(javafx.event.ActionEvent actionEvent) {
        if (containsAlphabetic(number.getText())) {
            OptionPane("Enter proper room number", "Error message");
        } else if (isRoomNumberExists(number.getText()) && !number.getText().equals(Integer.toString(selectedRoomNumber))) {
            OptionPane("Room number exists", "Error message");
        } else if (containsAlphabetic(price.getText())) {
            OptionPane("Enter proper price", "Error message");
        } else {
            String query = """
                    UPDATE rooms SET roomNumber = ?, roomType = ?, price = ?
                    WHERE roomNumber = ?
                    """;
            try {
                pst = connection.prepareStatement(query);
                pst.setString(1, number.getText());
                pst.setString(2, getRoomType());
                pst.setString(3, price.getText());
                pst.setInt(4, selectedRoomNumber);
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            RoomController roomController = new RoomController();
            roomController.refreshRoomList(connection);
            save.getScene().getWindow().hide();
        }
    }

    public void handleDeleteAction(ActionEvent actionEvent) {
        String query = "DELETE FROM rooms WHERE roomNumber = ?";
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, Integer.toString(selectedRoomNumber));
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        RoomController roomController = new RoomController();
        roomController.refreshRoomList(connection);
        delete.getScene().getWindow().hide();
    }

    private boolean isRoomNumberExists(String roomNumber) {
        String query = "SELECT roomNumber FROM rooms WHERE roomNumber = ?";
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, roomNumber);
            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setRoomType(String type) {
        if (type.equals("Single")) {
            single.setSelected(true);
        } else if (type.equals("Double")) {
            _double.setSelected(true);
        } else {
            deluxe.setSelected(true);
        }
    }

    private String getRoomType() {
        String type = "";
        if (single.isSelected()) {
            type = "Single";
        } else if (_double.isSelected()) {
            type = "Double";
        } else if (deluxe.isSelected()) {
            type = "Deluxe";
        }
        return type;
    }
}
