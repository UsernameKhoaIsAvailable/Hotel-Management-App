package com.hotel_management.controller;

import com.hotel_management.model.Room;
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
import static com.hotel_management.controller.RoomController.roomList;
import static com.hotel_management.controller.RoomController.rooms;
import static com.hotel_management.storage.Database.getConnection;

public class AddRoomController implements Initializable {

    @FXML
    private Button add;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ToggleGroup toggleGroup = new ToggleGroup();
        single.setToggleGroup(toggleGroup);
        _double.setToggleGroup(toggleGroup);
        deluxe.setToggleGroup(toggleGroup);
    }

    public void handleAddAction(javafx.event.ActionEvent actionEvent) {
        if (containsAlphabetic(number.getText())) {
            OptionPane("Enter proper room number", "Error message");
        } else if (isRoomNumberExists(number.getText())) {
            OptionPane("Room number exists", "Error message");

        } else if (containsAlphabetic(price.getText())) {
            OptionPane("Enter proper price", "Error message");
        } else if (getRoomType().isBlank()) {
            OptionPane("Choose room type", "Error message");
        } else {
            String query = "INSERT INTO rooms (roomNumber, roomType, price) VALUES (?,?,?)";
            try {
                pst = connection.prepareStatement(query);
                pst.setString(1, number.getText());
                pst.setString(2, getRoomType());
                pst.setString(3, price.getText());
                roomList.add(new Room(Integer.parseInt(number.getText()), Integer.parseInt(price.getText()),
                        getRoomType(), "Not Booked"));
                rooms.add(new Room(Integer.parseInt(number.getText()), Integer.parseInt(price.getText()),
                        getRoomType(), "Not Booked"));
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            add.getScene().getWindow().hide();
        }
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
