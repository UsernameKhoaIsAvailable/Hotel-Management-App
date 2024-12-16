package com.hotel_management.controller;

import com.hotel_management.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.hotel_management.storage.Database.getConnection;

public class RoomController implements Initializable {

    @FXML
    private TableColumn<Room, String> price;

    @FXML
    private TableColumn<Room, String> roomNumber;

    @FXML
    private TableView<Room> roomTable;

    @FXML
    private TableColumn<Room, String> roomType;

    @FXML
    private TextField search;

    @FXML
    private TableColumn<Room, String> status;

    private Connection connection;



    public static final ObservableList<Room> rooms = FXCollections.observableArrayList();

    public static final List<Room> roomList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        roomNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        roomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        status.setCellValueFactory(new PropertyValueFactory<Room, String>("status"));
        initRoomList();
        roomTable.setItems(rooms);
    }

    public void refreshRoomList(Connection connection) {
        this.connection = connection;
        initRoomList();
    }

    public void handleAddAction(javafx.event.ActionEvent actionEvent) throws IOException {
        Stage add = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/addroom.fxml")));
        Scene scene = new Scene(root);
        add.setScene(scene);
        add.show();
    }

    public void clickItem(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            if (roomTable.getSelectionModel().getSelectedItem() != null) {
                if (roomTable.getSelectionModel().getSelectedItem().getStatus().equals("Booked")) {
                    CustomerController.setSelectedRoomNumber(roomTable.getSelectionModel().getSelectedItem().getNumber());
                    Stage add = new Stage();
                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/customerinfo.fxml")));
                    Scene scene = new Scene(root);
                    add.setScene(scene);
                    add.show();
                } else {
                    AdjustRoomController.setSelectedRoomNumber(roomTable.getSelectionModel().getSelectedItem().getNumber());
                    Stage stage = new Stage();
                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/adjustroom.fxml")));
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                }
            }
        }
    }

    private void initRoomList() {
        roomList.clear();
        rooms.clear();
        String query = "SELECT * FROM rooms";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int room_price = Integer.parseInt(rs.getString("price"));
                String room_type = rs.getString("roomType");
                String room_status = rs.getString("status");
                int room_num = Integer.parseInt(rs.getString("roomNumber"));
                roomList.add(new Room(room_num, room_price, room_type, room_status));
                rooms.add(new Room(room_num, room_price, room_type, room_status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void Search(String s) {
        RoomController.rooms.clear();
        for (int i = 0; i < roomList.size(); i++) {
            if (Integer.toString(roomList.get(i).getNumber()).indexOf(s) == 0) {
                RoomController.rooms.add(roomList.get(i));
            }
        }
    }

    public void handleSearchKey(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            String s = search.getText();
            Search(s);
        }
    }
}
